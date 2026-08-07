package com.instaflow.app.util

import android.content.Context
import android.media.MediaScannerConnection
import android.util.Log
import com.yausername.youtubedl_android.mapper.VideoInfo
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object RemoteProcessingEngine {
    private const val TAG = "RemoteEngine"
    
    // Default server address: 10.0.2.2 for Android Emulator, or localhost / custom server IP
    var serverBaseUrl: String = "http://10.0.2.2:8000"

    fun isServerAvailable(): Boolean {
        return try {
            val url = URL("$serverBaseUrl/health")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 1500
                readTimeout = 1500
                requestMethod = "GET"
            }
            val code = conn.responseCode
            conn.disconnect()
            code == 200
        } catch (e: Exception) {
            false
        }
    }

    fun downloadMedia(
        context: Context,
        urlStr: String,
        downloadDir: File,
        itemIndex: Int = 0,
        videoInfo: Any? = null,
        progressCallback: ((Float, Long, String) -> Unit)?
    ): Result<List<String>> {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        return try {
            val encodedUrl = URLEncoder.encode(urlStr, "UTF-8")
            val queryStr = if (itemIndex > 0) "$serverBaseUrl/api/v1/download?url=$encodedUrl&item=$itemIndex"
            else "$serverBaseUrl/api/v1/download?url=$encodedUrl"

            Log.i(TAG, "[RemoteEngine] Connecting to processing server: $queryStr")
            val url = URL(queryStr)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 60000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "InstaFlow-AndroidClient/2.0.0")
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
                val ext = if (mediaType.contains("image")) ".jpg" else ".mp4"
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
