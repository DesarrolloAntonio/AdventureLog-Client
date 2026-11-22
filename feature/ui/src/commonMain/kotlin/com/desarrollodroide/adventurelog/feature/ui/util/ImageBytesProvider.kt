package com.desarrollodroide.adventurelog.feature.ui.util

import org.koin.core.scope.Scope

interface ImageBytesProvider {
    fun getImageBytes(uri: String): ByteArray?
    fun getFileName(uri: String): String
    suspend fun downloadImageFromUrl(url: String): ByteArray?
}

/** Built by the Koin module; each platform takes what it needs (the Android `Context`) from the scope. */
expect fun Scope.createImageBytesProvider(): ImageBytesProvider
