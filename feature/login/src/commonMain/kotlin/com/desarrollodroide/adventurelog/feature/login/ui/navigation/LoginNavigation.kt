package com.desarrollodroide.adventurelog.feature.login.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.login.ui.screen.LoginScreenRoute
import com.desarrollodroide.adventurelog.feature.login.viewmodel.LoginViewModel
import org.koin.compose.viewmodel.koinViewModel

interface LoginNavigator {
    fun goToHome()
}

fun NavGraphBuilder.loginNavGraph(
    navigator: LoginNavigator
) {
    navigation(
        route = NavigationRoutes.Login.graph,
        startDestination = NavigationRoutes.Login.screen
    ) {
        composable(route = NavigationRoutes.Login.screen) {
            val viewModel = koinViewModel<LoginViewModel>()
            LoginScreenRoute(
                viewModel = viewModel,
                navigator = navigator
            )
        }
    }
}