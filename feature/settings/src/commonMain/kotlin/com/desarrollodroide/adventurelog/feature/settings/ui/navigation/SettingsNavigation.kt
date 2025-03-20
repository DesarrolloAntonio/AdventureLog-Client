package com.desarrollodroide.adventurelog.feature.settings.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsScreenRoute
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings module navigation graph
 */
fun NavGraphBuilder.settingsNavGraph(
    navController: NavHostController
) {
    navigation(
        route = Settings.route,
        startDestination = SettingsScreen.route
    ) {
        composable(route = SettingsScreen.route) {
            val viewModel = koinViewModel<SettingsViewModel>()
            SettingsScreenRoute(
                settingsViewModel = viewModel,
                onNavigateToTermsOfUse = {
                    // Implement when this route exists
                },
                onNavigateToPrivacyPolicy = {
                    // Implement when this route exists
                },
                onNavigateToSourceCode = {
                    // Implement when this route exists
                },
                onNavigateToLogs = {
                    // Implement when this route exists
                },
                onViewLastCrash = {
                    // Implement when this route exists
                },
                goToLogin = {
                    navController.navigate(NavigationRoutes.Login.route) {
                        popUpTo(Settings.route) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }
    }
}