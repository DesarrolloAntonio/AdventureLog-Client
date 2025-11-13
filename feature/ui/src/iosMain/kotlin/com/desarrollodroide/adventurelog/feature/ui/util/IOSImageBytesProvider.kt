package com.desarrollodroide.adventurelog.feature.ui.util

import coil3.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.posix.memcpy

class IOSImageBytesProvider : ImageBytesProvider {

    @OptIn(ExperimentalForeignApi::class)
    override fun getImageBytes(uri: String): ByteArray? {
        return try {
            val nsUrl = NSURL.URLWithString(uri) ?: return null
            val nsData = NSData.dataWithContentsOfURL(nsUrl) ?: return null
            
            val byteArray = ByteArray(nsData.length.toInt())
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
            }
            byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun getFileName(uri: String): String {
        val nsUrl = NSURL.URLWithString(uri)
        return nsUrl?.lastPathComponent ?: "image.jpg"
    }
}

actual fun createImageBytesProvider(platformContext: PlatformContext): ImageBytesProvider {
    return IOSImageBytesProvider()
}
