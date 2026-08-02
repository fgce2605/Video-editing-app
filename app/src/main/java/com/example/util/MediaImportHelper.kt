package com.example.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class ImportedMediaResult(
    val title: String,
    val durationMs: Long,
    val localFilePath: String,
    val mimeType: String?,
    val isAudio: Boolean
)

object MediaImportHelper {

    suspend fun processImportedUri(
        context: Context,
        uri: Uri
    ): ImportedMediaResult = withContext(Dispatchers.IO) {
        var fileName = "Imported_Media_${System.currentTimeMillis() % 10000}"
        var mimeType: String? = context.contentResolver.getType(uri)

        // Extract display name from ContentResolver
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrEmpty()) {
                        fileName = name
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Clean name display
        if (fileName.contains("/")) {
            fileName = fileName.substringAfterLast("/")
        }

        // Copy file to app's internal working directory
        val mediaDir = File(context.filesDir, "imported_media").apply { mkdirs() }
        val extension = when {
            mimeType?.contains("audio") == true -> "mp3"
            mimeType?.contains("image") == true -> "jpg"
            else -> "mp4"
        }
        val safeFileName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val destinationFile = File(mediaDir, "${System.currentTimeMillis()}_$safeFileName")

        var copySuccess = false
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    copySuccess = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val targetPath = if (copySuccess && destinationFile.exists() && destinationFile.length() > 0) {
            destinationFile.absolutePath
        } else {
            uri.toString()
        }

        // Retrieve duration using MediaMetadataRetriever
        var durationMs = 12000L // Default fallback 12s
        try {
            val retriever = MediaMetadataRetriever()
            if (copySuccess && destinationFile.exists() && destinationFile.length() > 0) {
                retriever.setDataSource(destinationFile.absolutePath)
            } else {
                retriever.setDataSource(context, uri)
            }
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (durStr != null) {
                val parsed = durStr.toLongOrNull()
                if (parsed != null && parsed > 100L) {
                    durationMs = parsed
                }
            }
            val retrievedMime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            if (!retrievedMime.isNullOrEmpty()) {
                mimeType = retrievedMime
            }
            retriever.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val lowerName = fileName.lowercase()
        val isAudio = mimeType?.startsWith("audio") == true ||
                lowerName.endsWith(".mp3") ||
                lowerName.endsWith(".wav") ||
                lowerName.endsWith(".aac") ||
                lowerName.endsWith(".m4a") ||
                lowerName.endsWith(".flac")

        ImportedMediaResult(
            title = fileName,
            durationMs = durationMs,
            localFilePath = targetPath,
            mimeType = mimeType,
            isAudio = isAudio
        )
    }

    fun createCameraVideoUri(context: Context): Pair<Uri, File> {
        val mediaDir = File(context.filesDir, "camera_captures").apply { mkdirs() }
        val file = File(mediaDir, "VID_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp4")
        val authority = "${context.packageName}.fileprovider"
        val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
        return Pair(uri, file)
    }
}
