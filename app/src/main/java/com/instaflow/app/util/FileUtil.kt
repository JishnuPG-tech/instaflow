package com.instaflow.app.util

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.annotation.CheckResult
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.instaflow.app.App.Companion.context
import com.instaflow.app.R
import java.io.File
import okhttp3.internal.closeQuietly

const val AUDIO_REGEX = "(mp3|aac|opus|m4a)$"
const val THUMBNAIL_REGEX = "\\.(jpg|png)$"
const val SUBTITLE_REGEX = "\\.(lrc|vtt|srt|ass|json3|srv.|ttml)$"
private const val PRIVATE_DIRECTORY_SUFFIX = ".InstaFlow"

object FileUtil {
    fun openFileFromResult(downloadResult: Result<List<String>>) {
        val filePaths = downloadResult.getOrNull()
        if (filePaths.isNullOrEmpty()) return
        openFile(filePaths.first()) {
            ToastUtil.makeToastSuspend(context.getString(R.string.file_unavailable))
        }
    }

    inline fun openFile(path: String, onFailureCallback: (Throwable) -> Unit) =
        path
            .runCatching {
                createIntentForOpeningFile(this)?.run { context.startActivity(this) }
                    ?: throw Exception("Could not create open intent")
            }
            .onFailure { onFailureCallback(it) }

    private fun createIntentForFile(path: String?): Intent? {
        if (path == null) return null
        Log.d(TAG, "Creating intent for file: $path")

        val file = File(path)
        val uri = if (file.exists()) {
            FileProvider.getUriForFile(
                context,
                context.getFileProvider(),
                file,
            )
        } else {
            path.runCatching {
                DocumentFile.fromSingleUri(context, Uri.parse(path))?.uri
            }.getOrNull()
        } ?: return null

        return Intent().apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            data = uri
        }
    }

    fun createIntentForOpeningFile(path: String?): Intent? =
        createIntentForFile(path)?.let { intent ->
            val targetUri = intent.data ?: return null
            val extension = path?.substringAfterLast('.', "")?.lowercase() ?: ""
            val mimeType = when (extension) {
                "jpg", "jpeg", "png", "webp" -> "image/*"
                "mp4", "mkv", "webm", "mov", "3gp" -> "video/*"
                "mp3", "m4a", "opus", "aac", "flac" -> "audio/*"
                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
            }
            intent.apply {
                action = Intent.ACTION_VIEW
                setDataAndType(targetUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                clipData = ClipData(null, arrayOf(mimeType), ClipData.Item(targetUri))
            }
        }

    fun createIntentForSharingFile(path: String?): Intent? =
        createIntentForFile(path)?.apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, data)
            val extension = path?.substringAfterLast('.', "")?.lowercase() ?: ""
            val mimeType = when (extension) {
                "jpg", "jpeg", "png", "webp" -> "image/*"
                "mp4", "mkv", "webm", "mov" -> "video/*"
                "mp3", "m4a", "opus", "aac" -> "audio/*"
                else -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
            }
            setDataAndType(this.data, mimeType)
            clipData = ClipData(null, arrayOf(mimeType), ClipData.Item(data))
        }

    fun Context.getFileProvider() = "$packageName.provider"

    fun String.getFileSize(): Long =
        this.run {
            val length = File(this).length()
            if (length == 0L) DocumentFile.fromSingleUri(context, Uri.parse(this))?.length() ?: 0L
            else length
        }

    fun String.getFileName(): String =
        this.run {
            File(this).nameWithoutExtension.ifEmpty {
                DocumentFile.fromSingleUri(context, Uri.parse(this))?.name ?: "video"
            }
        }

    fun deleteFile(path: String) =
        path.runCatching {
            if (!File(path).delete()) DocumentFile.fromSingleUri(context, Uri.parse(this))?.delete()
        }

    @CheckResult
    fun scanFileToMediaLibraryPostDownload(title: String, downloadDir: String): List<String> {
        val dir = File(downloadDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        val now = System.currentTimeMillis()
        val validFiles = dir.walkTopDown()
            .filter { it.isFile && it.length() > 0L }
            .filter { file ->
                val name = file.name.lowercase()
                !name.contains(".fdash") &&
                !name.matches(Regex(".*\\.f[0-9]+\\..*")) &&
                !name.endsWith(".part") && 
                !name.endsWith(".ytdl") && 
                !name.endsWith(".tmp") && 
                !name.endsWith(".temp") &&
                !name.startsWith(".") &&
                !name.endsWith(".nomedia") &&
                !name.endsWith(".json")
            }
            .filter { file ->
                val matchesTitle = title.isNotBlank() && file.name.contains(title, ignoreCase = true)
                val isRecent = (now - file.lastModified()) < 60 * 1000 // 60 seconds tight window
                matchesTitle || isRecent
            }
            .sortedByDescending { it.lastModified() }
            .toList()

        val resultPaths = validFiles.map { it.absolutePath }
        if (resultPaths.isNotEmpty()) {
            MediaScannerConnection.scanFile(context, resultPaths.toTypedArray(), null) { path, uri ->
                Log.i("FileUtil", "[Storage] MediaScanner indexed file: $path -> $uri")
            }
        }
        
        val filtered = resultPaths.filter { 
            !it.contains(Regex(THUMBNAIL_REGEX)) && !it.contains(Regex(SUBTITLE_REGEX))
        }

        return if (filtered.isNotEmpty()) {
            val primary = filtered.find { it.endsWith(".mp4", ignoreCase = true) }
                ?: filtered.find { it.endsWith(".jpg", ignoreCase = true) || it.endsWith(".jpeg", ignoreCase = true) || it.endsWith(".png", ignoreCase = true) }
                ?: filtered.first()
            listOf(primary)
        } else {
            emptyList()
        }
    }

    fun scanDownloadDirectoryToMediaLibrary(downloadDir: String) =
        File(downloadDir)
            .walkTopDown()
            .filter { it.isFile }
            .map { it.absolutePath }
            .run {
                MediaScannerConnection.scanFile(context, this.toList().toTypedArray(), null, null)
            }

    @CheckResult
    fun moveFilesToSdcard(tempPath: File, sdcardUri: String): Result<List<String>> {
        val uriList = mutableListOf<String>()
        val destDir =
            Uri.parse(sdcardUri).run {
                DocumentsContract.buildDocumentUriUsingTree(
                    this,
                    DocumentsContract.getTreeDocumentId(this),
                )
            }
        val res =
            tempPath.runCatching {
                walkTopDown().forEach {
                    if (it.isDirectory) return@forEach
                    val mimeType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension) ?: "*/*"

                    val destUri =
                        DocumentsContract.createDocument(
                            context.contentResolver,
                            destDir,
                            mimeType,
                            it.name,
                        ) ?: return@forEach

                    val inputStream = it.inputStream()
                    val outputStream =
                        context.contentResolver.openOutputStream(destUri) ?: return@forEach
                    inputStream.copyTo(outputStream)
                    inputStream.closeQuietly()
                    outputStream.closeQuietly()
                    uriList.add(destUri.toString())
                }
                uriList
            }
        tempPath.deleteRecursively()
        return res
    }

    fun clearTempFiles(downloadDir: File): Int {
        var count = 0
        downloadDir.walkTopDown().forEach {
            if (it.isFile && !it.isHidden) {
                if (it.delete()) count++
            }
        }
        return count
    }

    fun Context.getConfigDirectory(): File = cacheDir

    fun Context.getConfigFile(suffix: String = "") = File(getConfigDirectory(), "config$suffix.txt")

    fun Context.getAccountSessionFile() = File(getConfigDirectory(), "cookies.txt")

    fun getExternalTempDir() =
        File(getExternalDownloadDirectory(), "tmp").apply {
            mkdirs()
            createEmptyFile(".nomedia")
        }

    fun Context.getSdcardTempDir(child: String?): File =
        getExternalTempDir().run { child?.let { resolve(it) } ?: this }

    fun Context.getArchiveFile(): File = filesDir.createEmptyFile("archive.txt").getOrThrow()

    fun Context.getInternalTempDir() = File(filesDir, "tmp")

    internal fun getExternalDownloadDirectory() =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "InstaFlow")
            .also { it.mkdir() }

    internal fun getExternalPrivateDownloadDirectory() =
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            PRIVATE_DIRECTORY_SUFFIX,
        )

    fun File.createEmptyFile(fileName: String): Result<File> =
        this.run {
            runCatching {
                if (!exists()) mkdirs()
                val file = resolve(fileName)
                if (!file.exists()) file.createNewFile()
                file
            }
        }

    fun writeContentToFile(content: String, file: File): File = file.apply { writeText(content) }

    fun getRealPath(treeUri: Uri): String {
        val path: String = treeUri.path.toString()
        Log.d(TAG, path)
        if (!path.contains("primary:")) {
            ToastUtil.makeToast("This directory is not supported")
            return getExternalDownloadDirectory().absolutePath
        }
        val last: String = path.split("primary:").last()
        return Environment.getExternalStorageDirectory().absolutePath + "/$last"
    }

    private const val TAG = "FileUtil"
}
