package com.instaflow.app.util

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.OPEN_READONLY
import android.media.MediaCodecList
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.CookieManager
import androidx.annotation.CheckResult
import com.instaflow.app.App
import com.instaflow.app.App.Companion.audioDownloadDir
import com.instaflow.app.App.Companion.context
import com.instaflow.app.App.Companion.videoDownloadDir
import com.instaflow.app.Downloader
import com.instaflow.app.R
import com.instaflow.app.database.objects.CommandTemplate
import com.instaflow.app.database.objects.DownloadedVideoInfo
import com.instaflow.app.ui.page.settings.network.AccountSession
import com.instaflow.app.util.FileUtil.getAccountSessionFile
import com.instaflow.app.util.FileUtil.getConfigFile
import com.instaflow.app.util.FileUtil.getExternalDownloadDirectory
import com.instaflow.app.util.FileUtil.getExternalTempDir
import com.instaflow.app.util.FileUtil.getFileName
import com.instaflow.app.util.FileUtil.getSdcardTempDir
import com.instaflow.app.util.FileUtil.moveFilesToSdcard
import com.instaflow.app.util.FileUtil.getArchiveFile
import com.instaflow.app.util.PreferenceUtil.ACCOUNT_SESSION_HEADER
import com.instaflow.app.util.PreferenceUtil.getBoolean
import com.instaflow.app.util.PreferenceUtil.getInt
import com.instaflow.app.util.PreferenceUtil.getString
import com.instaflow.app.util.PreferenceUtil.updateBoolean
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

object DownloadUtil {

    object CookieScheme {
        const val NAME = "name"
        const val VALUE = "value"
        const val SECURE = "is_secure"
        const val EXPIRY = "expires_utc"
        const val HOST = "host_key"
        const val PATH = "path"
    }

    private val jsonFormat = Json { ignoreUnknownKeys = true }

    private const val TAG = "DownloadUtil"

    const val BASENAME = "%(title).100B"

    const val EXTENSION = ".%(ext)s"

    private const val ID = "[%(id).30B]"

    private const val CLIP_TIMESTAMP = "%(section_start)d-%(section_end)d"

    const val OUTPUT_TEMPLATE_DEFAULT = BASENAME + EXTENSION

    const val OUTPUT_TEMPLATE_ID = "$BASENAME $ID$EXTENSION"

    private const val OUTPUT_TEMPLATE_CLIPS = "$BASENAME [$CLIP_TIMESTAMP]$EXTENSION"

    private const val OUTPUT_TEMPLATE_CHAPTERS =
        "chapter:$BASENAME/%(section_number)d - %(section_title).200B$EXTENSION"

    private const val OUTPUT_TEMPLATE_SPLIT = "$BASENAME/$OUTPUT_TEMPLATE_DEFAULT"

    private const val PLAYLIST_TITLE_SUBDIRECTORY_PREFIX = "%(playlist)s/"

    private const val CROP_ARTWORK_COMMAND =
        """--ppa "ffmpeg: -c:v mjpeg -vf crop=\"'if(gt(ih,iw),iw,ih)':'if(gt(iw,ih),ih,iw)'\"""""

    @CheckResult
    fun getPlaylistOrVideoInfo(
        playlistURL: String,
        downloadPreferences: DownloadPreferences = DownloadPreferences.createFromPreferences(),
        taskKey: String? = null,
        playlistIndex: Int? = null,
    ): Result<YoutubeDLInfo> =
        YoutubeDL.runCatching {
            ToastUtil.makeToastSuspend(context.getString(R.string.fetching_playlist_info))
            val request = YoutubeDLRequest(playlistURL)
            with(request) {
                val isInstagram = playlistURL.contains("instagram.com") || playlistURL.contains("cdninstagram.com")
                if (!isInstagram && playlistIndex == null) {
                    addOption("--flat-playlist")
                }
                if (playlistIndex != null) {
                    addOption("--playlist-items", playlistIndex.toString())
                }
                addOption("--dump-single-json")
                addOption("-o", BASENAME)
                addOption("-R", "1")
                addOption("--socket-timeout", "15")
                addOption("--no-cache-dir")
                applyInstagramHeaders(playlistURL)

                downloadPreferences.run {
                    if (extractAudio && !isInstagram) {
                        addOption("-x")
                    }
                    if (!isInstagram) {
                        applyFormatSorter(this, toFormatSorter())
                    }
                    if (proxy) {
                        enableProxy(proxyUrl)
                    }
                    if (forceIpv4 || isInstagram) {
                        addOption("-4")
                    }
                    if (accounts) {
                        enableAccountSession(userAgentString)
                    }
                    if (restrictFilenames) {
                        addOption("--restrict-filenames")
                    }
                }
            }
            YoutubeDL.getInstance().execute(request, taskKey ?: playlistURL, null).out.run {
                // Handle possible multi-line output by taking the last non-empty line
                val jsonLine = this.lines().filter { it.isNotBlank() }.lastOrNull() ?: this
                val playlistInfo = jsonFormat.decodeFromString<PlaylistResult>(jsonLine)
                if (playlistInfo.type != "playlist") {
                    jsonFormat.decodeFromString<VideoInfo>(jsonLine)
                } else playlistInfo
            }
        }

    @CheckResult
    private fun getVideoInfo(
        request: YoutubeDLRequest,
        taskKey: String? = null,
    ): Result<VideoInfo> =
        request.runCatching {
            val response: YoutubeDLResponse =
                YoutubeDL.getInstance().execute(request, taskKey, null)
            
            // Handle multi-line output
            val jsonLine = response.out.lines().filter { it.isNotBlank() }.lastOrNull()
                ?: throw YoutubeDLException("yt-dlp returned no output")
            
            Log.d(TAG, "[Parsing] Decoding Info from line: ${jsonLine.take(100)}...")
            
            // Check if it's a playlist first
            try {
                // If it's a playlist with entries (e.g. from --playlist-items), 
                // we want the first entry as our VideoInfo.
                val playlist = jsonFormat.decodeFromString<PlaylistResult>(jsonLine)
                if (playlist.type == "playlist" && !playlist.entries.isNullOrEmpty()) {
                    Log.d(TAG, "[Parsing] Got PlaylistResult, entries=${playlist.entries.size}")
                    // yt-dlp usually doesn't give full formats in a flat playlist result entry.
                    // But if it's NOT a flat playlist, it might.
                    // For Instagram, we need the ID/URL to proceed.
                    val entry = playlist.entries[0]
                    return@runCatching VideoInfo(
                        id = entry.id ?: "",
                        title = entry.title ?: playlist.title ?: "",
                        webpageUrl = entry.webpageUrl ?: entry.url,
                        originalUrl = entry.originalUrl ?: playlist.originalUrl,
                        extractorKey = entry.ieKey ?: playlist.extractorKey ?: "Instagram",
                        uploader = entry.uploader ?: playlist.uploader,
                        duration = entry.duration,
                        thumbnail = entry.thumbnails?.lastOrNull()?.url,
                        formats = emptyList() // We'll fetch formats in download phase if needed
                    )
                }
            } catch (e: Exception) {
                // Not a playlist or decoding failed, proceed to VideoInfo
            }

            jsonFormat.decodeFromString<VideoInfo>(jsonLine)
        }

    @CheckResult
    fun fetchVideoInfoFromUrl(
        url: String,
        playlistIndex: Int? = null,
        taskKey: String? = null,
        preferences: DownloadPreferences = DownloadPreferences.createFromPreferences(),
    ): Result<VideoInfo> {
        val isInstagram = url.contains("instagram.com") || url.contains("cdninstagram.com")
        with(preferences) {
            val request =
                YoutubeDLRequest(url).apply {
                    addOption("-o", BASENAME)
                    if (restrictFilenames) {
                        addOption("--restrict-filenames")
                    }
                    if (extractAudio && !isInstagram) {
                        addOption("-x")
                    }
                    if (!isInstagram) {
                        applyFormatSorter(this@with, toFormatSorter())
                    }
                    addInstagramFFmpegOptions(url)
                    applyInstagramHeaders(url)
                    if (accounts) {
                        enableAccountSession(userAgentString)
                    }
                    if (proxy) {
                        enableProxy(proxyUrl)
                    }
                    if (forceIpv4 || isInstagram) {
                        addOption("-4")
                    }
                    if (autoSubtitle) {
                        addOption("--write-auto-subs")
                        if (!autoTranslatedSubtitles) {
                            addOption("--extractor-args", "youtube:skip=translated_subs")
                        }
                    }
                    if (playlistIndex != null) {
                        addOption("--playlist-items", playlistIndex)
                    } else {
                        if (!isInstagram) {
                            addOption("--no-playlist")
                        }
                    }
                    addOption("--dump-single-json")
                    addOption("-R", "1")
                    addOption("--socket-timeout", "15")
                    addOption("--no-cache-dir")
                }
            return getVideoInfo(request, taskKey)
        }
    }

    @Serializable
    data class DownloadPreferences(
        val extractAudio: Boolean,
        val createThumbnail: Boolean,
        val downloadPlaylist: Boolean,
        val subdirectoryExtractor: Boolean,
        val subdirectoryPlaylistTitle: Boolean,
        val commandDirectory: String,
        val downloadSubtitle: Boolean,
        val embedSubtitle: Boolean,
        val keepSubtitle: Boolean,
        val subtitleLanguage: String,
        val autoSubtitle: Boolean,
        val autoTranslatedSubtitles: Boolean,
        val convertSubtitle: Int,
        val concurrentFragments: Int,
        val accounts: Boolean,
        val aria2c: Boolean,
        val useCustomAudioPreset: Boolean,
        val audioFormat: Int,
        val audioQuality: Int,
        val convertAudio: Boolean,
        val formatSorting: Boolean,
        val sortingFields: String,
        val audioConvertFormat: Int,
        val videoFormat: Int,
        val formatIdString: String,
        val videoResolution: Int,
        val privateMode: Boolean,
        val rateLimit: Boolean,
        val maxDownloadRate: String,
        val privateDirectory: Boolean,
        val cropArtwork: Boolean,
        val sdcard: Boolean,
        val sdcardUri: String,
        val embedThumbnail: Boolean,
        val videoClips: List<VideoClip>,
        val splitByChapter: Boolean,
        val debug: Boolean,
        val proxy: Boolean,
        val proxyUrl: String,
        val newTitle: String,
        val userAgentString: String,
        val outputTemplate: String,
        val useDownloadArchive: Boolean,
        val embedMetadata: Boolean,
        val restrictFilenames: Boolean,
        val supportAv1HardwareDecoding: Boolean,
        val forceIpv4: Boolean,
        val mergeAudioStream: Boolean,
        val mergeToMkv: Boolean,
    ) {
        companion object {
            val EMPTY =
                DownloadPreferences(
                    extractAudio = false,
                    createThumbnail = false,
                    downloadPlaylist = false,
                    subdirectoryExtractor = false,
                    subdirectoryPlaylistTitle = false,
                    commandDirectory = "",
                    downloadSubtitle = false,
                    embedSubtitle = false,
                    keepSubtitle = false,
                    subtitleLanguage = "",
                    autoSubtitle = false,
                    autoTranslatedSubtitles = false,
                    convertSubtitle = 0,
                    concurrentFragments = 0,
                    accounts = false,
                    aria2c = false,
                    audioFormat = 0,
                    audioQuality = 0,
                    convertAudio = false,
                    formatSorting = false,
                    sortingFields = "",
                    audioConvertFormat = 0,
                    videoFormat = 0,
                    formatIdString = "",
                    videoResolution = 0,
                    privateMode = false,
                    rateLimit = false,
                    maxDownloadRate = "",
                    privateDirectory = false,
                    cropArtwork = false,
                    sdcard = false,
                    sdcardUri = "",
                    embedThumbnail = false,
                    videoClips = emptyList(),
                    splitByChapter = false,
                    debug = false,
                    proxy = false,
                    proxyUrl = "",
                    newTitle = "",
                    userAgentString = "",
                    outputTemplate = "",
                    useDownloadArchive = false,
                    embedMetadata = false,
                    restrictFilenames = false,
                    supportAv1HardwareDecoding = false,
                    forceIpv4 = false,
                    mergeAudioStream = false,
                    mergeToMkv = false,
                    useCustomAudioPreset = false,
                )

            fun createFromPreferences(): DownloadPreferences {
                val downloadSubtitle = SUBTITLE.getBoolean()
                val embedSubtitle = EMBED_SUBTITLE.getBoolean()
                return DownloadPreferences(
                    extractAudio = EXTRACT_AUDIO.getBoolean(),
                    createThumbnail = THUMBNAIL.getBoolean(),
                    downloadPlaylist = PLAYLIST.getBoolean(),
                    subdirectoryExtractor = SUBDIRECTORY_EXTRACTOR.getBoolean(),
                    subdirectoryPlaylistTitle = SUBDIRECTORY_PLAYLIST_TITLE.getBoolean(),
                    commandDirectory = COMMAND_DIRECTORY.getString(),
                    downloadSubtitle = downloadSubtitle,
                    embedSubtitle = embedSubtitle,
                    keepSubtitle = KEEP_SUBTITLE_FILES.getBoolean(),
                    subtitleLanguage = SUBTITLE_LANGUAGE.getString(),
                    autoSubtitle = AUTO_SUBTITLE.getBoolean(),
                    autoTranslatedSubtitles = AUTO_TRANSLATED_SUBTITLES.getBoolean(),
                    convertSubtitle = CONVERT_SUBTITLE.getInt(),
                    concurrentFragments = CONCURRENT.getInt(),
                    accounts = ACCOUNTS.getBoolean(),
                    aria2c = ARIA2C.getBoolean(),
                    useCustomAudioPreset = USE_CUSTOM_AUDIO_PRESET.getBoolean(),
                    audioFormat = AUDIO_FORMAT.getInt(),
                    audioQuality = AUDIO_QUALITY.getInt(),
                    convertAudio = AUDIO_CONVERT.getBoolean(),
                    formatSorting = FORMAT_SORTING.getBoolean(),
                    sortingFields = SORTING_FIELDS.getString(),
                    audioConvertFormat = PreferenceUtil.getAudioConvertFormat(),
                    videoFormat = PreferenceUtil.getVideoFormat(),
                    formatIdString = "",
                    videoResolution = PreferenceUtil.getVideoResolution(),
                    privateMode = PRIVATE_MODE.getBoolean(),
                    rateLimit = RATE_LIMIT.getBoolean(),
                    maxDownloadRate = PreferenceUtil.getMaxDownloadRate(),
                    privateDirectory = PRIVATE_DIRECTORY.getBoolean(),
                    cropArtwork = CROP_ARTWORK.getBoolean(),
                    sdcard = SDCARD_DOWNLOAD.getBoolean(),
                    sdcardUri = SDCARD_URI.getString(),
                    embedThumbnail = EMBED_THUMBNAIL.getBoolean(),
                    videoClips = emptyList(),
                    splitByChapter = false,
                    debug = DEBUG.getBoolean(),
                    proxy = PROXY.getBoolean(),
                    proxyUrl = PROXY_URL.getString(),
                    newTitle = "",
                    userAgentString =
                        USER_AGENT_STRING.run { if (USER_AGENT.getBoolean()) getString() else "" },
                    outputTemplate = OUTPUT_TEMPLATE.getString(),
                    useDownloadArchive = DOWNLOAD_ARCHIVE.getBoolean(),
                    embedMetadata = EMBED_METADATA.getBoolean(),
                    restrictFilenames = RESTRICT_FILENAMES.getBoolean(),
                    supportAv1HardwareDecoding = checkIfAv1HardwareAccelerated(),
                    forceIpv4 = FORCE_IPV4.getBoolean(),
                    mergeAudioStream = false,
                    mergeToMkv =
                        (downloadSubtitle && embedSubtitle) || MERGE_OUTPUT_MKV.getBoolean(),
                )
            }
        }
    }

    private fun YoutubeDLRequest.addInstagramFFmpegOptions(url: String): YoutubeDLRequest = apply {
        val isInstagram = url.contains("instagram.com") || url.contains("fbcdn.net") || url.contains("cdninstagram.com")
        if (isInstagram) {
            addOption("--merge-output-format", "mp4")
            addOption("--hls-prefer-ffmpeg")
            addOption("-v") // Enable verbose for Instagram to debug sound issues
            
            // Force audio merge if not explicitly requested otherwise
            addOption("--audio-quality", "0")
            
            // Set explicit FFmpeg location for reliable merging on all Android devices.
            // youtubedl-android-ffmpeg extracts ffmpeg to this specific internal path.
            try {
                // Path used by io.github.junkfood02.youtubedl-android:ffmpeg
                val ffmpegDir = File(context.noBackupFilesDir, "youtubedl-android/packages/ffmpeg")
                if (ffmpegDir.exists()) {
                    addOption("--ffmpeg-location", ffmpegDir.absolutePath)
                    Log.d(TAG, "[Pipeline] Configured FFmpeg location for Instagram: ${ffmpegDir.absolutePath}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "[Pipeline] Could not resolve FFmpeg path: ${e.message}")
            }
        }
    }

    private fun YoutubeDLRequest.applyInstagramHeaders(url: String): YoutubeDLRequest = apply {
        if (url.contains("instagram.com") || url.contains("cdninstagram.com")) {
            // Automatically sync cookies from Android CookieManager if available
            syncWebViewCookiesToFile()

            addOption("--referer", "https://www.instagram.com/")
            addOption("--add-header", "Accept-Language:en-US,en;q=0.9")
            // Modern compatible User-Agent
            addOption("--add-header", "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
            addOption("--add-header", "Sec-Ch-Ua: \"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"")
            addOption("--add-header", "Sec-Ch-Ua-Mobile: ?0")
            addOption("--add-header", "Sec-Ch-Ua-Platform: \"Windows\"")
            addOption("--add-header", "Origin: https://www.instagram.com")
            addOption("--add-header", "X-IG-App-ID:936619743392459")
            addOption("--add-header", "X-ASBD-ID:129477")
            addOption("--add-header", "X-IG-WWW-Claim:0")
            addOption("--add-header", "X-Requested-With:XMLHttpRequest")
            addOption("--extractor-args", "instagram:check_video=False")
            addOption("--no-check-certificates")
            addOption("--allow-unplayable-formats")
            addOption("--ignore-no-formats-error")
            addOption("--no-warning")
            addOption("--geo-bypass")
            addOption("-4")
            
            val sessionFile = context.getAccountSessionFile()
            if (!sessionFile.exists() || sessionFile.length() == 0L) {
                if (!syncWebViewCookiesToFile()) {
                    ensureDefaultCookiesConfigured()
                }
            }
            if (sessionFile.exists() && sessionFile.length() > 0) {
                Log.d(TAG, "[Pipeline] Passing Instagram session cookies file to yt-dlp: ${sessionFile.length()} bytes")
                addOption("--cookies", sessionFile.absolutePath)
            } else {
                Log.w(TAG, "[Pipeline] No Instagram session cookies file found. Downloading in guest mode.")
            }
        }
    }

    fun ensureDefaultCookiesConfigured() {
        // Removed hardcoded stale cookies to prevent account flagging and challenges.
        // Guest mode or a fresh login via the app's browser is more reliable than using expired sessions.
        Log.i(TAG, "[CookieSync] No local cookies found. Proceeding in guest mode.")
    }

    private fun YoutubeDLRequest.enableAccountSession(userAgentString: String): YoutubeDLRequest =
        this.addOption("--cookies", context.getAccountSessionFile().absolutePath).apply {
            if (userAgentString.isNotEmpty()) {
                addOption("--add-header", "User-Agent:$userAgentString")
            }
        }

    private fun YoutubeDLRequest.enableProxy(proxyUrl: String): YoutubeDLRequest =
        this.addOption("--proxy", proxyUrl)

    private fun YoutubeDLRequest.useDownloadArchive(): YoutubeDLRequest =
        this.addOption("--download-archive", context.getArchiveFile().absolutePath)



    /**
     * Directly syncs active cookies from Android [CookieManager] into the Netscape format cookie file.
     * This is 100% reliable across all Android versions (does not rely on fragile SQLite path or encrypted databases).
     */
    fun syncWebViewCookiesToFile(): Boolean {
        return try {
            val cookieManager = CookieManager.getInstance()
            cookieManager.flush()
            
            // Collect cookies from multiple Instagram-related domains to be exhaustive
            val domains = listOf(
                "https://www.instagram.com",
                "https://instagram.com",
                "https://i.instagram.com",
                "https://help.instagram.com"
            )
            
            val allCookies = mutableMapOf<String, String>()
            domains.forEach { domain ->
                cookieManager.getCookie(domain)?.split(";")?.forEach { entry ->
                    val parts = entry.trim().split("=", limit = 2)
                    if (parts.size == 2) {
                        allCookies[parts[0].trim()] = parts[1].trim()
                    }
                }
            }

            if (allCookies.isEmpty()) {
                Log.d(TAG, "[CookieSync] CookieManager returned no Instagram cookies.")
                return false
            }

            val sb = StringBuilder()
            sb.append("# Netscape HTTP Cookie File\n")
            sb.append("# http://curl.haxx.se/rfc/cookie_spec.html\n")

            allCookies.forEach { (name, value) ->
                if (name.isNotEmpty()) {
                    sb.append(".instagram.com\tTRUE\t/\tTRUE\t2147483647\t$name\t$value\n")
                }
            }

            val sessionFile = context.getAccountSessionFile()
            FileUtil.writeContentToFile(sb.toString(), sessionFile)
            Log.i(TAG, "[CookieSync] Successfully exported ${allCookies.size} unique cookies to ${sessionFile.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "[CookieSync] Error syncing cookies: ${e.message}")
            false
        }
    }

    @CheckResult
    fun getAccountListFromDatabase(): Result<List<AccountSession>> = runCatching {
        // Try direct CookieManager sync first
        syncWebViewCookiesToFile()

        CookieManager.getInstance().run {
            if (!hasCookies()) throw Exception("There is no cookies in the database!")
            flush()
        }
        SQLiteDatabase.openDatabase(
                context.dataDir.resolve("app_webview/Default/Cookies").absolutePath,
                null,
                OPEN_READONLY,
            )
            .run {
                val projection =
                    arrayOf(
                        CookieScheme.HOST,
                        CookieScheme.EXPIRY,
                        CookieScheme.PATH,
                        CookieScheme.NAME,
                        CookieScheme.VALUE,
                        CookieScheme.SECURE,
                    )
                val accountList = mutableListOf<AccountSession>()
                query("cookies", projection, null, null, null, null, null).run {
                    while (moveToNext()) {
                        val expiry = getLong(getColumnIndexOrThrow(CookieScheme.EXPIRY))
                        val name = getString(getColumnIndexOrThrow(CookieScheme.NAME))
                        val value = getString(getColumnIndexOrThrow(CookieScheme.VALUE))
                        val path = getString(getColumnIndexOrThrow(CookieScheme.PATH))
                        val secure = getLong(getColumnIndexOrThrow(CookieScheme.SECURE)) == 1L
                        val hostKey = getString(getColumnIndexOrThrow(CookieScheme.HOST))

                        val host = if (hostKey[0] != '.') ".$hostKey" else hostKey
                        accountList.add(
                            AccountSession(
                                domain = host,
                                name = name,
                                value = value,
                                path = path,
                                secure = secure,
                                expiry = expiry,
                            )
                        )
                    }
                    close()
                }
                close()
                accountList
            }
    }

    fun List<AccountSession>.toAccountSessionFileContent(): String =
        this.fold(StringBuilder(ACCOUNT_SESSION_HEADER)) { acc, cookie ->
                acc.append(cookie.toNetscapeCookieString()).append("\n")
            }
            .toString()

    fun getAccountsContentFromDatabase(): Result<String> =
        getAccountListFromDatabase().mapCatching { it.toAccountSessionFileContent() }

    private fun YoutubeDLRequest.enableAria2c(): YoutubeDLRequest =
        this.addOption("--downloader", "libaria2c.so")

    private fun YoutubeDLRequest.addOptionsForVideoDownloads(
        downloadPreferences: DownloadPreferences,
        isInstagram: Boolean = false
    ): YoutubeDLRequest =
        this.apply {
            downloadPreferences.run {
                addOption("--add-metadata")
                addOption("--no-embed-info-json")
                    if (formatIdString.isNotEmpty()) {
                        Log.i(TAG, "[Pipeline] Using explicit format selector: '$formatIdString'")
                        addOption("-f", formatIdString)
                        if (mergeAudioStream) {
                            addOption("--audio-multistreams")
                        }
                    } else {
                        if (isInstagram) {
                            Log.i(TAG, "[Pipeline] Instagram video format resolution — applying bestvideo+bestaudio/best selector for FFmpeg DASH audio merge")
                            addOption("-f", "bestvideo+bestaudio/best")
                        } else {
                            applyFormatSorter(this, toFormatSorter())
                        }
                    }
                if (downloadSubtitle) {
                    if (autoSubtitle) {
                        addOption("--write-auto-subs")
                        if (!autoTranslatedSubtitles) {
                            addOption("--extractor-args", "youtube:skip=translated_subs")
                        }
                    }
                    subtitleLanguage
                        .takeIf { it.isNotEmpty() }
                        ?.let { addOption("--sub-langs", it) }
                    if (embedSubtitle) {
                        addOption("--embed-subs")
                        if (keepSubtitle) {
                            addOption("--write-subs")
                        }
                    } else {
                        addOption("--write-subs")
                    }
                    when (convertSubtitle) {
                        CONVERT_ASS -> addOption("--convert-subs", "ass")
                        CONVERT_SRT -> addOption("--convert-subs", "srt")
                        CONVERT_VTT -> addOption("--convert-subs", "vtt")
                        CONVERT_LRC -> addOption("--convert-subs", "lrc")
                        else -> {}
                    }
                }
                if (mergeToMkv && !isInstagram) {
                    addOption("--remux-video", "mkv")
                    addOption("--merge-output-format", "mkv")
                }
                if (embedThumbnail) {
                    addOption("--embed-thumbnail")
                }
                if (videoClips.isEmpty()) addOption("--embed-chapters")
            }
        }

    @CheckResult
    private fun DownloadPreferences.toAudioFormatSorter(): String =
        this.run {
            if (!useCustomAudioPreset) return@run ""
            val format =
                when (audioFormat) {
                    M4A -> "acodec:aac"
                    OPUS -> "acodec:opus"
                    else -> ""
                }
            val quality =
                when (audioQuality) {
                    HIGH -> "abr~192"
                    MEDIUM -> "abr~128"
                    LOW -> "abr~64"
                    else -> ""
                }
            return@run connectWithDelimiter(format, quality, delimiter = ",")
        }

    @CheckResult
    private fun DownloadPreferences.toVideoFormatSorter(): String =
        this.run {
            val format =
                when (videoFormat) {
                    FORMAT_COMPATIBILITY -> "proto,vcodec:h264,ext"
                    FORMAT_QUALITY ->
                        if (supportAv1HardwareDecoding) {
                            "vcodec:av01"
                        } else {
                            "vcodec:vp9.2"
                        }

                    else -> ""
                }
            val res =
                when (videoResolution) {
                    1 -> "res:2160"
                    2 -> "res:1440"
                    3 -> "res:1080"
                    4 -> "res:720"
                    5 -> "res:480"
                    6 -> "res:360"
                    7 -> "+res"
                    else -> ""
                }
            val sorter = if (videoFormat == FORMAT_COMPATIBILITY) {
                connectWithDelimiter(format, res, delimiter = ",")
            } else {
                connectWithDelimiter(res, format, delimiter = ",")
            }
            return@run sorter
        }

    private fun YoutubeDLRequest.applyFormatSorter(
        preferences: DownloadPreferences,
        sorter: String,
    ) =
        preferences.run {
            if (formatSorting && sortingFields.isNotEmpty()) addOption("-S", sortingFields)
            else if (sorter.isNotEmpty()) addOption("-S", sorter) else {}
        }

    @CheckResult
    fun DownloadPreferences.toFormatSorter(): String =
        connectWithDelimiter(
            this.toVideoFormatSorter(),
            this.toAudioFormatSorter(),
            delimiter = ",",
        )

    private fun YoutubeDLRequest.addOptionsForAudioDownloads(
        id: String,
        preferences: DownloadPreferences,
        playlistUrl: String,
    ): YoutubeDLRequest =
        this.apply {
            with(preferences) {
                addOption("-x")
                if (downloadSubtitle) {
                    addOption("--write-subs")

                    if (autoSubtitle) {
                        addOption("--write-auto-subs")
                        if (!autoTranslatedSubtitles) {
                            addOption("--extractor-args", "youtube:skip=translated_subs")
                        }
                    }
                    subtitleLanguage
                        .takeIf { it.isNotEmpty() }
                        ?.let { addOption("--sub-langs", it) }
                    when (convertSubtitle) {
                        CONVERT_ASS -> addOption("--convert-subs", "ass")
                        CONVERT_SRT -> addOption("--convert-subs", "srt")
                        CONVERT_VTT -> addOption("--convert-subs", "vtt")
                        CONVERT_LRC -> addOption("--convert-subs", "lrc")
                        else -> {}
                    }
                }
                if (formatIdString.isNotEmpty()) {
                    addOption("-f", formatIdString)
                    if (mergeAudioStream) {
                        addOption("--audio-multistreams")
                    }
                } else if (convertAudio) {
                    when (audioConvertFormat) {
                        CONVERT_MP3 -> {
                            addOption("--audio-format", "mp3")
                        }

                        CONVERT_M4A -> {
                            addOption("--audio-format", "m4a")
                        }
                    }
                } else {
                    applyFormatSorter(preferences, toAudioFormatSorter())
                }

                if (embedMetadata) {
                    addOption("--embed-metadata")
                    addOption("--embed-thumbnail")
                    addOption("--convert-thumbnails", "jpg")

                    if (cropArtwork) {
                        val configFile = context.getConfigFile(id)
                        FileUtil.writeContentToFile(CROP_ARTWORK_COMMAND, configFile)
                        addOption("--config", configFile.absolutePath)
                    }
                }
                addOption("--parse-metadata", "%(release_year,upload_date)s:%(meta_date)s")

                if (playlistUrl.isNotEmpty()) {
                    addOption("--parse-metadata", "%(album,playlist,title)s:%(meta_album)s")
                    addOption("--parse-metadata", "%(track_number,playlist_index)d:%(meta_track)s")
                } else {
                    addOption("--parse-metadata", "%(album,title)s:%(meta_album)s")
                }
            }
        }

    private fun insertInfoIntoDownloadHistory(
        videoInfo: VideoInfo,
        filePaths: List<String>,
    ): List<String> =
        filePaths.onEach {
            DatabaseUtil.insertInfo(videoInfo.toDownloadedVideoInfo(videoPath = it))
        }

    private fun VideoInfo.toDownloadedVideoInfo(
        id: Int = 0,
        videoPath: String,
    ): DownloadedVideoInfo =
        this.run {
            val isInstagram = extractorKey.lowercase().contains("instagram")
            var mType = ""
            var igUser: String? = null
            var cap: String? = null

            if (isInstagram) {
                igUser = uploader ?: channel
                cap = title
                
                val rawFormats = formats ?: emptyList()
                val isStory = webpageUrl?.contains("/stories/") == true
                val isReel = webpageUrl?.contains("/reel/") == true
                val isProfile = extractorKey.lowercase().contains("profile")
                val hasVideoCodec = rawFormats.any { it.vcodec != "none" && it.vcodec != null }

                mType = when {
                    isProfile -> "PROFILE_PIC"
                    isReel -> "REEL"
                    isStory -> if (hasVideoCodec) "STORY" else "IMAGE"
                    hasVideoCodec -> "VIDEO"
                    else -> "IMAGE"
                }
            }

            DownloadedVideoInfo(
                id = id,
                videoTitle = title,
                videoAuthor = uploader ?: channel ?: uploaderId.toString(),
                videoUrl = webpageUrl ?: originalUrl.toString(),
                thumbnailUrl = thumbnail.toHttpsUrl(),
                videoPath = videoPath,
                extractor = extractorKey,
                mediaType = mType,
                instagramUsername = igUser,
                captionText = cap,
            )
        }

    private fun insertSplitChapterIntoHistory(videoInfo: VideoInfo, filePaths: List<String>) =
        filePaths.onEach {
            DatabaseUtil.insertInfo(
                videoInfo.toDownloadedVideoInfo(videoPath = it).copy(videoTitle = it.getFileName())
            )
        }

    @CheckResult
    fun downloadVideo(
        videoInfo: VideoInfo? = null,
        playlistUrl: String = "",
        playlistItem: Int = 0,
        taskId: String,
        downloadPreferences: DownloadPreferences,
        progressCallback: ((Float, Long, String) -> Unit)?,
    ): Result<List<String>> {
        if (videoInfo == null)
            return Result.failure(Throwable(context.getString(R.string.fetch_info_error_msg)))

        with(downloadPreferences) {
            val url = playlistUrl.ifBlank {
                videoInfo.webpageUrl?.ifBlank { null }
                    ?: videoInfo.originalUrl?.ifBlank { null }
                    ?: videoInfo.id.takeIf { it.isNotBlank() }?.let { "https://www.instagram.com/p/$it/" }
                    ?: return Result.failure(
                        Throwable("Invalid or empty media URL")
                    )
            }
            Log.i(TAG, "[Pipeline] Executing YoutubeDLRequest for target URL: '$url'")
            val request = YoutubeDLRequest(url)
            val pathBuilder = StringBuilder()
            val outputBuilder = StringBuilder()

            request
                .apply {
                    addInstagramFFmpegOptions(url)
                    addOption("--no-mtime")
                    applyInstagramHeaders(url)
                    if (accounts) {
                        enableAccountSession(userAgentString)
                    }
                    if (restrictFilenames) {
                        addOption("--restrict-filenames")
                    }
                    if (proxy) {
                        enableProxy(proxyUrl)
                    }
                    if (forceIpv4 || url.contains("instagram.com")) {
                        addOption("-4")
                    }
                    if (debug) {
                        addOption("-v")
                    }
                    if (useDownloadArchive) {
                        val archiveFile = context.getArchiveFile()
                        val archiveFileContent = archiveFile.readText()
                        if (archiveFileContent.contains("${videoInfo.extractor} ${videoInfo.id}")) {
                            return Result.failure(
                                YoutubeDLException(
                                    context.getString(R.string.download_archive_error)
                                )
                            )
                        } else {
                            useDownloadArchive()
                        }
                    }

                    if (rateLimit && maxDownloadRate.isNumberInRange(1, 1000000)) {
                        addOption("-r", "${maxDownloadRate}K")
                    }

                    if (playlistItem != 0) {
                        addOption("--playlist-items", playlistItem)
                        if (subdirectoryPlaylistTitle && !videoInfo.playlist.isNullOrEmpty()) {
                            outputBuilder.append(PLAYLIST_TITLE_SUBDIRECTORY_PREFIX)
                        }
                    } else {
                        addOption("--no-playlist")
                    }

                    if (aria2c) {
                        enableAria2c()
                    } else if (concurrentFragments > 1) {
                        addOption("--concurrent-fragments", concurrentFragments)
                    }

                    addOption("--socket-timeout", "15")

                    val isInstagram = url.contains("instagram") || url.contains("fbcdn.net") || videoInfo.extractorKey == "Instagram"
                    val isImage = MediaClassifier.isImageMedia(videoInfo, url)
                    
                    Log.d(TAG, "[Pipeline Telemetry] Single Source of Truth Classification: id=${videoInfo.id}, isImage=$isImage")

                    if (extractAudio || (videoInfo.vcodec == "none" && !isInstagram && !isImage)) {
                        if (privateDirectory) pathBuilder.append(App.privateDownloadDir)
                        else pathBuilder.append(audioDownloadDir)
                        addOptionsForAudioDownloads(
                            id = videoInfo.id,
                            preferences = downloadPreferences,
                            playlistUrl = playlistUrl,
                        )
                    } else {
                        if (privateDirectory) pathBuilder.append(App.privateDownloadDir)
                        else pathBuilder.append(videoDownloadDir)
                        
                        if (isImage) {
                            Log.i(TAG, "[Pipeline] Item detected as Image/Photo — omitting -f flag so yt-dlp auto-downloads photo without format errors")
                            // Do NOT pass -f flag for photo posts
                        } else {
                            addOptionsForVideoDownloads(downloadPreferences, isInstagram)
                        }
                    }

                    if (createThumbnail && !isImage) {
                        addOption("--write-thumbnail")
                        addOption("--convert-thumbnails", "png")
                    }
                    if (subdirectoryExtractor) {
                        pathBuilder.append("/${videoInfo.extractorKey}")
                    }

                    val internalTempDir = File(context.cacheDir, "tmp").apply { mkdirs() }
                    val internalOutDir = File(context.cacheDir, "downloads").apply { mkdirs() }

                    if (sdcard) {
                        addOption("-P", context.getSdcardTempDir(videoInfo.id).absolutePath)
                    } else {
                        // Pass both home and temp as explicit prefixed paths to prevent yt-dlp from creating temp files in public storage
                        addOption("-P", "home:" + internalOutDir.absolutePath)
                        addOption("-P", "temp:" + internalTempDir.absolutePath)
                    }

                    if (splitByChapter) {
                        addOption("-o", OUTPUT_TEMPLATE_CHAPTERS)
                        addOption("--split-chapters")
                    }

                    // For generic URLs (like CDN photos), use a very simple output template to avoid Errno 36
                    val output = when {
                        splitByChapter -> OUTPUT_TEMPLATE_SPLIT
                        !videoClips.isEmpty() -> OUTPUT_TEMPLATE_CLIPS
                        isImage -> "%(title).100s.%(ext)s" // NO ID for images, keep it simple and clean
                        else -> outputTemplate
                    }

                    addOption("-o", outputBuilder.append(output).toString())

                    val fullCmd = request.buildCommand().joinToString(" ")
                    Log.i(TAG, "==========================================================")
                    Log.i(TAG, "[Pipeline Telemetry] Executing yt-dlp Task: $taskId")
                    Log.i(TAG, "[Pipeline Telemetry] Target URL: $url")
                    Log.i(TAG, "[Pipeline Telemetry] Classified isImage: $isImage")
                    Log.i(TAG, "[Pipeline Telemetry] Internal Temp Dir: ${internalTempDir.absolutePath}")
                    Log.i(TAG, "[Pipeline Telemetry] Full Command Args: $fullCmd")
                    Log.i(TAG, "==========================================================")
                }
                .runCatching {
                    YoutubeDL.getInstance()
                        .execute(request = this, processId = taskId, callback = progressCallback)
                }
                .onFailure { th ->
                    Log.e(TAG, "[Pipeline] YoutubeDL execution failed for taskId=$taskId: ${th.message}")
                    return Result.failure(th)
                }

            Log.i(TAG, "[Pipeline] YoutubeDL execution finished for taskId=$taskId. Checking output files.")
            
            val isInsta = url.contains("instagram.com") || url.contains("cdninstagram.com")
            val finishResult = onFinishDownloading(
                preferences = this,
                videoInfo = videoInfo,
                downloadPath = pathBuilder.toString(),
                sdcardUri = sdcardUri,
            )

            return finishResult.onSuccess {
                if (it.isEmpty() && !privateMode) {
                    Log.w(TAG, "[Pipeline] No files were scanned/found after successful download for taskId=$taskId")
                } else {
                    Log.i(TAG, "[Pipeline] Download successful. Resulting files: $it")
                }
            }.run {
                val files = getOrNull()
                if (isInsta && files != null && files.isEmpty() && !privateMode) {
                    val fallbackFiles = FileUtil.scanFileToMediaLibraryPostDownload("", pathBuilder.toString())
                    if (fallbackFiles.isNotEmpty()) {
                        Log.i(TAG, "[Pipeline] Found fallback output files post-download: $fallbackFiles")
                        Result.success(fallbackFiles)
                    } else {
                        Result.failure(YoutubeDLException("Download finished but no file was produced. This content may be private or restricted."))
                    }
                } else {
                    this
                }
            }
        }
    }

    private fun onFinishDownloading(
        preferences: DownloadPreferences,
        videoInfo: VideoInfo,
        downloadPath: String,
        sdcardUri: String,
    ): Result<List<String>> =
        preferences.run {
            val fileName =
                preferences.newTitle.ifEmpty {
                    videoInfo.filename
                        ?: videoInfo.requestedDownloads?.firstOrNull()?.filename
                        ?: videoInfo.title
                }

            Log.d(TAG, "onFinishDownloading: $fileName")
            
            val movedFiles = mutableListOf<String>()
            val internalOutDir = File(context.cacheDir, "downloads")
            if (internalOutDir.exists()) {
                internalOutDir.listFiles()?.forEach { file ->
                    val name = file.name.lowercase()
                    val isFragment = name.contains(".fdash") || name.endsWith(".part") || name.endsWith(".ytdl") || name.endsWith(".tmp") || name.endsWith(".temp") || name.matches(Regex(".*\\.f[0-9]+\\..*"))
                    if (file.isFile && !isFragment && file.length() > 0L) {
                        try {
                            val targetFile = File(downloadPath, file.name)
                            targetFile.parentFile?.mkdirs()
                            file.copyTo(targetFile, overwrite = true)
                            file.delete()
                            movedFiles.add(targetFile.absolutePath)
                            Log.i(TAG, "[Pipeline] Successfully moved validated file from internal cache to public download path: ${targetFile.absolutePath}")
                        } catch (e: Exception) {
                            Log.w(TAG, "[Pipeline] Safe file mover note for ${file.name}: ${e.message}")
                        }
                    }
                }
            }

            if (movedFiles.isNotEmpty()) {
                android.media.MediaScannerConnection.scanFile(context, movedFiles.toTypedArray(), null) { path, uri ->
                    Log.i(TAG, "[Storage] MediaScanner indexed moved file: $path -> $uri")
                }
                if (!privateMode) {
                    if (splitByChapter) {
                        insertSplitChapterIntoHistory(videoInfo, movedFiles)
                    } else {
                        insertInfoIntoDownloadHistory(videoInfo, movedFiles)
                    }
                }
                return Result.success(if (privateMode) emptyList() else movedFiles)
            }

            if (sdcard) {
                moveFilesToSdcard(
                        sdcardUri = sdcardUri,
                        tempPath = context.getSdcardTempDir(videoInfo.id),
                    )
                    .onSuccess {
                        if (privateMode) {
                            return Result.success(emptyList())
                        } else if (splitByChapter) {
                            insertSplitChapterIntoHistory(videoInfo, it)
                        } else {
                            insertInfoIntoDownloadHistory(videoInfo, it)
                        }
                    }
            } else {
                FileUtil.scanFileToMediaLibraryPostDownload(
                        title = fileName,
                        downloadDir = downloadPath,
                    )
                    .run {
                        if (privateMode) Result.success(emptyList())
                        else
                            Result.success(
                                if (splitByChapter) {
                                    insertSplitChapterIntoHistory(videoInfo, this)
                                } else {
                                    insertInfoIntoDownloadHistory(videoInfo, this)
                                }
                            )
                    }
            }
        }

    @CheckResult
    fun executeCustomCommandTask(
        urlString: String,
        taskId: String,
        template: CommandTemplate,
        preferences: DownloadPreferences,
        progressCallback: ((Float, Long, String) -> Unit),
    ): Result<YoutubeDLResponse> {
        val urlList = urlString.split(Regex("[\n ]")).filter { it.isNotBlank() }

        val request =
            with(preferences) {
                YoutubeDLRequest(urlList).apply {
                    val commandDirectory = preferences.commandDirectory
                    commandDirectory.takeIf { it.isNotEmpty() }?.let { addOption("-P", it) }
                    addOption("--newline")
                    if (aria2c) {
                        enableAria2c()
                    }
                    if (useDownloadArchive) {
                        useDownloadArchive()
                    }
                    if (restrictFilenames) {
                        addOption("--restrict-filenames")
                    }
                    addOption(
                        "--config-locations",
                        FileUtil.writeContentToFile(template.template, context.getConfigFile())
                            .absolutePath,
                    )
                    if (accounts) {
                        enableAccountSession(userAgentString)
                    }
                }
            }

        return runCatching {
            YoutubeDL.getInstance()
                .execute(request = request, processId = taskId, callback = progressCallback)
        }
    }

    suspend fun executeCommandInBackground(
        url: String,
        template: CommandTemplate = PreferenceUtil.getTemplate(),
        downloadPreferences: DownloadPreferences = DownloadPreferences.createFromPreferences(),
    ) {
        val taskId = Downloader.makeKey(url = url, templateName = template.name)
        val notificationId = Downloader.run { taskId.toNotificationId() }
        val urlList = url.split(Regex("[\n ]")).filter { it.isNotBlank() }

        ToastUtil.makeToastSuspend(context.getString(R.string.start_execute))
        val request =
            YoutubeDLRequest(urlList).apply {
                val commandDirectory = downloadPreferences.commandDirectory
                commandDirectory.takeIf { it.isNotEmpty() }?.let { addOption("-P", it) }
                addOption("--newline")
                if (downloadPreferences.aria2c) {
                    enableAria2c()
                }
                if (downloadPreferences.useDownloadArchive) {
                    useDownloadArchive()
                }
                if (downloadPreferences.restrictFilenames) {
                    addOption("--restrict-filenames")
                }
                addOption(
                    "--config-locations",
                    FileUtil.writeContentToFile(template.template, context.getConfigFile())
                        .absolutePath,
                )
                if (downloadPreferences.accounts) {
                    enableAccountSession(downloadPreferences.userAgentString)
                }
            }

        Downloader.onProcessStarted()
        withContext(Dispatchers.Main) { Downloader.onTaskStarted(template, url) }
        runCatching {
                val response =
                    YoutubeDL.getInstance().execute(request = request, processId = taskId) {
                        progress,
                        _,
                        text ->
                        NotificationUtil.makeNotificationForCustomCommand(
                            notificationId = notificationId,
                            taskId = taskId,
                            progress = progress.toInt(),
                            templateName = template.name,
                            taskUrl = url,
                            text = text,
                        )
                    }
                Downloader.onTaskEnded(template, url, response.out + "\n" + response.err)
            }
            .onFailure {
                it.printStackTrace()
                if (it is YoutubeDL.CanceledException) return@onFailure
                it.message.run {
                    if (isNullOrEmpty()) Downloader.onTaskEnded(template, url)
                    else Downloader.onTaskError(this, template, url)
                }
            }
        Downloader.onProcessEnded()
    }

    private fun checkIfAv1HardwareAccelerated(): Boolean {
        if (PreferenceUtil.containsKey(AV1_HARDWARE_ACCELERATED)) {
            return AV1_HARDWARE_ACCELERATED.getBoolean()
        } else {
            val res =
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    false
                } else {
                    MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.any { info ->
                        info.supportedTypes.any { it.equals("video/av01", ignoreCase = true) } &&
                            info.isHardwareAccelerated
                    }
                }
            AV1_HARDWARE_ACCELERATED.updateBoolean(res)
            return res
        }
    }
}
