package com.desarrollodroide.adventurelog.feature.ui.util

import org.koin.core.scope.Scope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController
import platform.posix.memcpy

class IOSPlatformFiles : PlatformFiles {

    /** Everything [open] wrote went into the app's temporary directory, so that is what goes. */
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun delete() {
        val manager = NSFileManager.defaultManager
        val directory = NSTemporaryDirectory()
        manager.contentsOfDirectoryAtPath(directory, error = null)?.forEach { name ->
            manager.removeItemAtPath(directory + name, error = null)
        }
    }

    // Opening and sharing are the same gesture on iOS: the share sheet previews a file and
    // offers the apps that can take it.
    override suspend fun share(bytes: ByteArray, fileName: String): Boolean =
        open(bytes, fileName)

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
                // Present from whatever is on top (a sheet, a pushed screen), and anchor the
                // popover iPad shows instead of a sheet: without a source it raises an exception.
                var top = root
                while (true) top = top.presentedViewController ?: break
                controller.popoverPresentationController?.sourceView = top.view
                top.view.bounds.useContents {
                    controller.popoverPresentationController?.sourceRect =
                        CGRectMake(size.width / 2, size.height / 2, 1.0, 1.0)
                }
                top.presentViewController(controller, animated = true, completion = null)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
}

actual fun Scope.createPlatformFiles(): PlatformFiles =
    IOSPlatformFiles()
