package com.desarrollodroide.adventurelog.feature.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.request.crossfade
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.di.LocalSessionTokenManager
import com.desarrollodroide.adventurelog.feature.ui.di.SessionTokenManager

/**
 * Provides mock dependencies for compose previews
 */
@Composable
fun PreviewImageDependencies(
    content: @Composable () -> Unit
) {
    val platformContext = LocalPlatformContext.current

    // Create a simple ImageLoader for preview without network capabilities
    val previewImageLoader = ImageLoader.Builder(platformContext)
        .crossfade(false) // Disable animations in preview
        .build()

    // Create a mock SessionTokenManager
    val previewSessionTokenManager = SessionTokenManager()

    CompositionLocalProvider(
        LocalImageLoader provides previewImageLoader,
        LocalSessionTokenManager provides previewSessionTokenManager,
        content = content
    )
}
