package com.desarrollodroide.adventurelog.feature.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import coil3.PlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidAttachmentOpener(
    private val context: Context
) : AttachmentOpener {

    override suspend fun open(bytes: ByteArray, fileName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // One directory, rewritten each time: these are throwaway copies for a viewer,
                // not a download folder the user manages.
                val directory = File(context.cacheDir, ATTACHMENTS_DIR).apply { mkdirs() }
                val file = File(directory, fileName.sanitisedFileName())
                file.writeBytes(bytes)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val view = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeTypeFor(file.extension))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // A chooser rather than a bare VIEW, and no resolveActivity check first: since
                // Android 11 that call is filtered by package visibility and answers null even
                // when a viewer is installed, which made every attachment look unopenable.
                val chooser = Intent.createChooser(view, null).apply {
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
        const val ATTACHMENTS_DIR = "attachments"
    }
}

/** Keeps a server-supplied name from escaping the cache directory. */
private fun String.sanitisedFileName(): String =
    substringAfterLast('/').substringAfterLast('\\').ifBlank { "attachment" }

actual fun createAttachmentOpener(platformContext: PlatformContext): AttachmentOpener =
    AndroidAttachmentOpener(platformContext as Context)
