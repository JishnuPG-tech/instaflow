package com.instasave.app.core.storage

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scoped storage manager writing media directly to Android MediaStore API
 * under Pictures/InstaSave/ (photos) and Movies/InstaSave/ (videos).
 * Enforces the IS_PENDING protocol to prevent incomplete download corruption.
 */
@Singleton
class MediaStoreWriter @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun saveVideo(
        fileName: String,
        mimeType: String = "video/mp4",
        inputStream: InputStream
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/InstaSave")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val uri = context.contentResolver.insert(collection, contentValues)
                ?: return@withContext Result.failure(IllegalStateException("Failed to create MediaStore video entry"))

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                copyStream(inputStream, outputStream)
            } ?: return@withContext Result.failure(IllegalStateException("Failed to open MediaStore output stream"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveImage(
        fileName: String,
        mimeType: String = "image/jpeg",
        inputStream: InputStream
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/InstaSave")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val uri = context.contentResolver.insert(collection, contentValues)
                ?: return@withContext Result.failure(IllegalStateException("Failed to create MediaStore image entry"))

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                copyStream(inputStream, outputStream)
            } ?: return@withContext Result.failure(IllegalStateException("Failed to open MediaStore output stream"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun copyStream(from: InputStream, to: OutputStream) {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (from.read(buffer).also { bytesRead = it } != -1) {
            to.write(buffer, 0, bytesRead)
        }
        to.flush()
    }
}
