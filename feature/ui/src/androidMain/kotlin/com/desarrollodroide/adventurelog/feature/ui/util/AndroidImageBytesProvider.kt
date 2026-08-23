package com.desarrollodroide.adventurelog.feature.ui.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import coil3.PlatformContext
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

class AndroidImageBytesProvider(
    private val context: Context
) : ImageBytesProvider {

    override fun getImageBytes(uri: String): ByteArray? {
        return try {
            val androidUri = uri.toUri()
            context.contentResolver.openInputStream(androidUri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getFileName(uri: String): String {
        val androidUri = uri.toUri()
        var fileName = "image.jpg"
        
        context.contentResolver.query(androidUri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        
        return fileName
    }

    override suspend fun downloadImageFromUrl(url: String): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                URL(url).openStream().use { inputStream ->
                    inputStream.readBytes()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

actual fun createImageBytesProvider(platformContext: PlatformContext): ImageBytesProvider {
    return AndroidImageBytesProvider(platformContext as Context)
}
