package com.desarrollodroide.adventurelog.feature.login.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.login.ui.screen.LoginScreenRoute
import com.desarrollodroide.adventurelog.feature.login.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Login module navigation graph
 */
fun NavGraphBuilder.loginNavGraph(
    navController: NavHostController
) {
    navigation(
        route = Login.route,
        startDestination = LoginScreen.route
    ) {
        composable(route = LoginScreen.route) {
            val viewModel = koinViewModel<LoginViewModel>()
            LoginScreenRoute(
                viewModel = viewModel,
                onNavigateToCollection = {
                    navController.navigate(NavigationRoutes.Home.route) {
                        popUpTo(Login.route) { inclusive = true }
                    }
                }
            )
        }
    }
}