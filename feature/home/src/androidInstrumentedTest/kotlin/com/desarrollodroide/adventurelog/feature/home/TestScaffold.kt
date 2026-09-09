package com.desarrollodroide.adventurelog.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader

/**
 * What a screen needs around it before it will compose at all.
 *
 * LocalImageLoader is a staticCompositionLocalOf that throws when nothing provides it, and the
 * app provides it once at the root - so any screen showing a photograph crashes outside that
 * tree. A test has no root, and neither does a @Preview.
 */
@Composable
fun WithAppLocals(content: @Composable () -> Unit) {
    val context = LocalContext.current
    CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) {
        content()
    }
}
