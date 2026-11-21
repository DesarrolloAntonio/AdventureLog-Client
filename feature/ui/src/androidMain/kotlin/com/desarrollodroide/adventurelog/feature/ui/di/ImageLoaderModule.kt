package com.desarrollodroide.adventurelog.feature.ui.di

import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSessionTokenManager = staticCompositionLocalOf<SessionTokenManager> {
    error("LocalSessionTokenManager not initialized")
}

val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("LocalImageLoader not initialized")
}

/**
 * Provides ImageLoader and SessionTokenManager through CompositionLocals
 * Should be called from a top-level composable (e.g. App.kt)
 */
@Composable
fun ProvideImageDependencies(
    sessionTokenManager: SessionTokenManager,
    imageClient: HttpClient,
    content: @Composable () -> Unit
) {
    val platformContext = LocalPlatformContext.current
    
    val imageLoader = remember(platformContext, imageClient) {
        ImageLoader.Builder(platformContext)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = { imageClient }))
            }
            .build()
    }
    
    CompositionLocalProvider(
        LocalImageLoader provides imageLoader,
        LocalSessionTokenManager provides sessionTokenManager,
        content = content
    )
}