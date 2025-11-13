package com.desarrollodroide.adventurelog.feature.ui.util

import coil3.PlatformContext

interface ImageBytesProvider {
    fun getImageBytes(uri: String): ByteArray?
    fun getFileName(uri: String): String
}

expect fun createImageBytesProvider(platformContext: PlatformContext): ImageBytesProvider
