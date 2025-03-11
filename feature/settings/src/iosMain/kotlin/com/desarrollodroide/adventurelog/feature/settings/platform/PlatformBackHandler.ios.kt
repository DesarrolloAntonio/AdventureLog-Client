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
    // En iOS, normalmente se maneja la navegación "hacia atrás" de forma diferente
    // Esta es una implementación mínima que no hace nada físicamente
    // En una aplicación real, implementarías esto usando el sistema de navegación de iOS
    
    // No hacemos nada aquí porque la navegación hacia atrás en iOS
    // se maneja típicamente a través de gestos o botones en la NavigationBar
}
