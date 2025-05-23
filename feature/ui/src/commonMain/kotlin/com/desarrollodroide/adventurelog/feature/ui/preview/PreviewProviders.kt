package com.desarrollodroide.adventurelog.feature.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import com.desarrollodroide.adventurelog.feature.ui.di.LocalSessionTokenManager
import com.desarrollodroide.adventurelog.feature.ui.di.SessionTokenManager

/**
 * Provides mock dependencies for previews
 * This is a simplified version that doesn't require network configuration
 */
@Composable
fun ProvidePreviewDependencies(
    content: @Composable () -> Unit
) {
    val platformContext = LocalPlatformContext.current
    
    val mockSessionTokenManager = remember { SessionTokenManager() }
    val mockImageLoader = remember(platformContext) {
        ImageLoader.Builder(platformContext).build()
    }
    
    CompositionLocalProvider(
        LocalImageLoader provides mockImageLoader,
        LocalSessionTokenManager provides mockSessionTokenManager,
        content = content
    )
}