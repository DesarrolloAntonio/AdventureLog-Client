package com.desarrollodroide.adventurelog.feature.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import coil3.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidPlatformFiles(
    private val context: Context
) : PlatformFiles {

    override suspend fun open(bytes: ByteArray, fileName: String): Boolean =
        hand(bytes, fileName) { uri, mimeType ->
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

    override suspend fun share(bytes: ByteArray, fileName: String): Boolean =
        hand(bytes, fileName) { uri, mimeType ->
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

    /** Writes the bytes where another app can read them, then starts [buildIntent] in a chooser. */
    private suspend fun hand(
        bytes: ByteArray,
        fileName: String,
        buildIntent: (uri: Uri, mimeType: String) -> Intent
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // One directory, rewritten each time: these are throwaway copies for another app to
            // read, not a download folder the user manages.
            val directory = File(context.cacheDir, SHARED_DIR).apply { mkdirs() }
            val file = File(directory, fileName.sanitisedFileName())
            file.writeBytes(bytes)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            // A chooser, and no resolveActivity check first: since Android 11 that call is
            // filtered by package visibility and answers null even when a handler is installed,
            // which made every attachment look unopenable.
            val chooser = Intent.createChooser(
                buildIntent(uri, mimeTypeFor(file.extension)),
                null
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private companion object {
        const val SHARED_DIR = "attachments"
    }
}

/** Keeps a server-supplied name from escaping the cache directory. */
private fun String.sanitisedFileName(): String =
    substringAfterLast('/').substringAfterLast('\\').ifBlank { "attachment" }

actual fun createPlatformFiles(platformContext: PlatformContext): PlatformFiles =
    AndroidPlatformFiles(platformContext as Context)
