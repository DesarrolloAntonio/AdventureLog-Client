package com.desarrollodroide.adventurelog.feature.ui.util

import coil3.PlatformContext
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.create
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.UIKit.UIImage
import platform.posix.memcpy

class IOSImageBytesProvider : ImageBytesProvider {

    @OptIn(ExperimentalForeignApi::class)
    override fun getImageBytes(uri: String): ByteArray? = readBytes(uri)

    override fun getFileName(uri: String): String {
        val nsUrl = NSURL.URLWithString(uri)
        return nsUrl?.lastPathComponent ?: "image.jpg"
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun downloadImageFromUrl(url: String): ByteArray? =
        withContext(Dispatchers.IO) { readBytes(url) }

    /**
     * Reads a file or remote URL into bytes.
     *
     * `NSData.dataWithContentsOfURL` is not exposed to Kotlin/Native - the generated binding is
     * the initialiser, `NSData(contentsOfURL:)` - which is why both callers here failed to
     * compile. The whole iOS source set has been broken on this since the image work landed.
     */
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun readBytes(url: String): ByteArray? = try {
        val nsUrl = NSURL.URLWithString(url)
        val nsData = nsUrl?.let { NSData.create(contentsOfURL = it) }

        if (nsData == null || nsData.length.toInt() == 0) {
            null
        } else {
            ByteArray(nsData.length.toInt()).apply {
                usePinned { pinned -> memcpy(pinned.addressOf(0), nsData.bytes, nsData.length) }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

actual fun createImageBytesProvider(platformContext: PlatformContext): ImageBytesProvider {
    return IOSImageBytesProvider()
}
