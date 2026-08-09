package com.instaflow.app.util

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import com.instaflow.app.model.DownloadType
import com.instaflow.app.util.PlaylistResult
import com.instaflow.app.util.VideoInfo
import com.instaflow.app.util.YoutubeDLInfo
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object RemoteProcessingEngine {
    private const val TAG = "RemoteEngine"
    
    // Live Hugging Face Space Base URL
    var serverBaseUrl: String = "https://jishnupg-opencode-cli.hf.space/instaflow"

    private val json = Json { ignoreUnknownKeys = true }

    fun isServerAvailable(): Boolean {
        for (attempt in 1..3) {
            try {
                val url = URL("$serverBaseUrl/health")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                }
                val code = conn.responseCode
                conn.disconnect()
                if (code == 200) {
                    Log.i(TAG, "[RemoteEngine] Health check passed on attempt $attempt")
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "[RemoteEngine] Health check attempt $attempt failed: ${e.message}")
            }
            if (attempt < 3) {
                try { Thread.sleep(1500) } catch (_: Exception) {}
            }
        }
        return false
    }

    fun analyzeUrl(urlStr: String, downloadType: DownloadType? = null): Result<YoutubeDLInfo> {
        var connection: HttpURLConnection? = null
        return try {
            val endpointPath = when (downloadType) {
                DownloadType.AUDIO -> "/api/v1/audio/analyze"
                DownloadType.VIDEO -> "/api/v1/video/analyze"
                DownloadType.POST -> "/api/v1/post/analyze"
                else -> "/api/v1/analyze"
            }
            val endpoint = "$serverBaseUrl$endpointPath"
            Log.i(TAG, "[RemoteEngine] Analyzing URL via server endpoint ($endpointPath): $urlStr")
            
            val url = URL(endpoint)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 45000
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("User-Agent", "InstaFlow-AndroidClient/2.1.0")
                doOutput = true
            }

            val typeStr = downloadType?.name ?: "POST"
            val jsonReq = "{\"url\": \"$urlStr\", \"download_type\": \"$typeStr\"}"
            connection.outputStream.use { it.write(jsonReq.toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val err = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                return Result.failure(Exception("Server analysis failed ($responseCode): $err"))
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val root = json.parseToJsonElement(responseBody) as JsonObject
            
            val rawMetadata = root["raw_metadata"]?.toString() 
                ?: root["data"]?.toString() 
                ?: responseBody
            
            try {
                val playlist = json.decodeFromString<PlaylistResult>(rawMetadata)
                if (playlist.type == "playlist") {
                    return Result.success(playlist)
                }
            } catch (_: Exception) {}

            Result.success(json.decodeFromString<VideoInfo>(rawMetadata))
        } catch (e: Exception) {
            Log.e(TAG, "[RemoteEngine] Analysis failure: ${e.message}")
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    fun downloadMedia(
        context: Context,
        urlStr: String,
        downloadDir: File,
        itemIndex: Int = 0,
        quality: Int? = null,
        formatId: String? = null,
        audioOnly: Boolean = false,
        mergePhotoAudio: Boolean = false,
        downloadType: DownloadType? = null,
        videoInfo: Any? = null,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result<List<String>> {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        return try {
            val endpointPath = when {
                downloadType == DownloadType.AUDIO || audioOnly -> "/api/v1/audio/download"
                downloadType == DownloadType.VIDEO -> "/api/v1/video/download"
                downloadType == DownloadType.POST -> "/api/v1/post/download"
                else -> "/api/v1/download"
            }

            val encodedUrl = URLEncoder.encode(urlStr, "UTF-8")
            val queryBuilder = StringBuilder("$serverBaseUrl$endpointPath?url=$encodedUrl")
            if (itemIndex > 0) {
                queryBuilder.append("&item=$itemIndex")
            }
            if (quality != null && quality > 0) {
                queryBuilder.append("&quality=$quality")
            }
            if (!formatId.isNullOrEmpty()) {
                queryBuilder.append("&format=${URLEncoder.encode(formatId, "UTF-8")}")
            }
            if (audioOnly || downloadType == DownloadType.AUDIO) {
                queryBuilder.append("&audio_only=true")
            }
            if (mergePhotoAudio) {
                queryBuilder.append("&merge_photo_audio=true")
            }
            if (downloadType != null) {
                queryBuilder.append("&download_type=${downloadType.name}")
            }
            val queryStr = queryBuilder.toString()

            Log.i(TAG, "[RemoteEngine] Connecting to server router: $queryStr")
            val url = URL(queryStr)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 60000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "InstaFlow-AndroidClient/2.1.0")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errStream = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Log.e(TAG, "[RemoteEngine] Server returned error $responseCode: $errStream")
                return Result.failure(Exception("Remote Processing Engine error ($responseCode): $errStream"))
            }

            val disposition = connection.getHeaderField("Content-Disposition")
            var fileName = "InstaFlow_${System.currentTimeMillis()}"
            if (!disposition.isNullOrBlank() && disposition.contains("filename=")) {
                val extracted = disposition.substringAfter("filename=").replace("\"", "").trim()
                if (extracted.isNotBlank()) fileName = extracted
            } else {
                val mediaType = connection.contentType ?: ""
                val ext = when {
                    mediaType.contains("image") -> ".jpg"
                    mediaType.contains("audio") || mediaType.contains("m4a") -> ".m4a"
                    else -> ".mp4"
                }
                fileName += ext
            }

            if (!downloadDir.exists()) downloadDir.mkdirs()
            val targetFile = File(downloadDir, fileName)
            
            val totalSize = connection.contentLengthLong
            inputStream = connection.inputStream
            outputStream = FileOutputStream(targetFile)

            val buffer = ByteArray(128 * 1024)
            var bytesRead: Int
            var totalBytesRead = 0L

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                if (totalSize > 0 && progressCallback != null) {
                    val progress = (totalBytesRead.toFloat() / totalSize.toFloat()) * 100f
                    progressCallback(progress, totalSize, "Downloading from InstaFlow Server...")
                }
            }

            outputStream.flush()
            Log.i(TAG, "[RemoteEngine] Successfully received ${targetFile.length()} bytes: ${targetFile.absolutePath}")

            MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), null) { path, uri ->
                Log.i(TAG, "[RemoteEngine] MediaScanner indexed file: $path -> $uri")
            }

            Result.success(listOf(targetFile.absolutePath))
        } catch (e: Exception) {
            Log.e(TAG, "[RemoteEngine] Connection or download failure: ${e.message}", e)
            Result.failure(e)
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
            connection?.disconnect()
        }
    }
}
