package com.desarrollodroide.adventurelog.feature.ui.util

import coil3.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.posix.memcpy

class IOSAttachmentOpener : AttachmentOpener {

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun open(bytes: ByteArray, fileName: String): Boolean =
        withContext(Dispatchers.Main) {
            try {
                val path = NSTemporaryDirectory() + fileName.substringAfterLast('/')
                val url = NSURL.fileURLWithPath(path)

                val data = bytes.usePinned { pinned ->
                    NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                }
                if (!data.writeToURL(url, atomically = true)) return@withContext false

                val root = UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: return@withContext false

                // The share sheet is the one presenter that always exists and can preview a file
                // without the app owning a document viewer of its own.
                val controller = UIActivityViewController(
                    activityItems = listOf(url),
                    applicationActivities = null
                )
                root.presentViewController(controller, animated = true, completion = null)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}

actual fun createAttachmentOpener(platformContext: PlatformContext): AttachmentOpener =
    IOSAttachmentOpener()
