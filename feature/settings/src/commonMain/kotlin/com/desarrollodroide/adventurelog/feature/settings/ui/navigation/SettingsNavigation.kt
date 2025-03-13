package com.desarrollodroide.adventurelog.feature.settings.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsScreenRoute
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.settingsGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    navigation<Settings>(
        startDestination = SettingsScreen
    ) {
        composable<SettingsScreen> { entry ->
            val viewModel = koinViewModel<SettingsViewModel>()
            SettingsScreenRoute(
                settingsViewModel = viewModel,
                onNavigateToTermsOfUse = {
                    //navController.navigate(TermsOfUse)
                },
                onNavigateToPrivacyPolicy = {
                    //navController.navigate(PrivacyPolicy)
                },
                onNavigateToSourceCode = {
                    //navController.navigate(SourceCode)
                },
                onNavigateToLogs = {
                    //navController.navigate(Logs)
                },
                onViewLastCrash = {
                    //navController.navigate(LastCrash)
                },
                goToLogin = {  },
                onBack = {  }
            )
        }
    }
}