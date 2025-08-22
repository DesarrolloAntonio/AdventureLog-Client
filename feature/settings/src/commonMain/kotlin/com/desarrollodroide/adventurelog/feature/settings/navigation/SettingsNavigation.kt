package com.desarrollodroide.adventurelog.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsScreen
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations

/**
 * Extension function to add settings screen to a navigation graph
 */
fun NavGraphBuilder.settingsScreen(
    onNavigateToSourceCode: () -> Unit = {},
    onNavigateToTermsOfUse: () -> Unit = {},
    onNavigateToPrivacyPolicy: () -> Unit = {},
) {
    composable(
        route = NavigationRoutes.Settings.route,
        // Settings appears from bottom for a distinctive style
        enterTransition = NavigationAnimations.enterTransitionVertical,
        exitTransition = NavigationAnimations.exitTransitionVertical,
    ) {
        SettingsScreen(
            onNavigateToSourceCode = onNavigateToSourceCode,
            onNavigateToTermsOfUse = onNavigateToTermsOfUse,
            onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
        )
    }
}
