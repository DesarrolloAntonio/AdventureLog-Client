package com.desarrollodroide.adventurelog.feature.login.login.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.feature.login.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.desarrollodroide.adventurelog.feature.login.login.LoginScreenRoute

fun NavGraphBuilder.loginGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    navigation<Login>(
        startDestination = LoginScreen
    ) {
        composable<LoginScreen> { entry ->
            val viewModel = koinViewModel<LoginViewModel>()
            LoginScreenRoute(
                viewModel = viewModel,
                onNavigateToCollection = {
                    onNavigateToHome()
                }
            )
        }
    }
}