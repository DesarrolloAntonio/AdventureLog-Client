package com.desarrollodroide.adventurelog.feature.settings.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

/**
 * iOS implementation of BackHandler
 * 
 * Note: This is a stub implementation as iOS handles back navigation differently.
 * In a real app, you would implement this using UIKit's navigation mechanisms
 * or SwiftUI's presentation mode.
 */
@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // In iOS, back navigation is typically handled differently
    // This is a minimal implementation that doesn't do anything physically
    // In a real app, you would implement this using iOS navigation system
    
    // We do nothing here because back navigation in iOS
    // is typically handled through gestures or buttons in the NavigationBar
}
