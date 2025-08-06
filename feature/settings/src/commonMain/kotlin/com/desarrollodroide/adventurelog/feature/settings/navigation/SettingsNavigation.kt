package com.desarrollodroide.adventurelog.feature.settings.navigation

import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsContent
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations
import org.koin.compose.viewmodel.koinViewModel

/**
 * Extension function to add settings screen to a navigation graph
 */
fun NavGraphBuilder.settingsScreen(
    onLogout: () -> Unit,
    onNavigateToSourceCode: () -> Unit = {},
    onNavigateToTermsOfUse: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {},
    onViewLastCrash: () -> Unit = {},
    goToLogin: () -> Unit = {}
) {
    composable(
        route = NavigationRoutes.Settings.route,
        // Settings appears from bottom for a distinctive style
        enterTransition = NavigationAnimations.enterTransitionVertical,
        exitTransition = NavigationAnimations.exitTransitionVertical,
    ) {
        val settingsViewModel = koinViewModel<SettingsViewModel>()
        val compactView by settingsViewModel.compactView.collectAsStateWithLifecycle()
        
        SettingsContent(
            compactView = compactView,
            onCompactViewChanged = settingsViewModel::setCompactView,
            onLogout = onLogout,
            onNavigateToSourceCode = onNavigateToSourceCode,
            onNavigateToTermsOfUse = onNavigateToTermsOfUse,
            onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
            onNavigateToLogs = onNavigateToLogs,
            onViewLastCrash = onViewLastCrash,
            themeMode = settingsViewModel.themeMode,
            onThemeModeChanged = settingsViewModel::setThemeMode,
            useDynamicColors = settingsViewModel.useDynamicColors,
            onDynamicColorsChanged = settingsViewModel::setUseDynamicColors,
            goToLogin = goToLogin,
            serverUrl = settingsViewModel.getServerUrl()
        )
    }
}
