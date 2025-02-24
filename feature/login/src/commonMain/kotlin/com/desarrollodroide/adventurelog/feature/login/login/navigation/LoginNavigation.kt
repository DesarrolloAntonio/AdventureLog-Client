package com.desarrollodroide.adventurelog.feature.login.login.navigation

import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.feature.login.LoginViewModel
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import com.desarrollodroide.adventurelog.feature.login.login.LoginScreenRoute

@Serializable
data object Login

@Serializable
internal data object LoginScreen

@Serializable
internal data object HomeScreen

fun NavGraphBuilder.loginGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
) {
    navigation<Login>(
        startDestination = LoginScreen
    ) {
        composable<LoginScreen> { entry ->
            val viewModel = koinViewModel<LoginViewModel>()
            LoginScreenRoute(viewModel = viewModel) { navController.navigate(HomeScreen) }
        }
        //It should not be here
        composable<HomeScreen> { entry ->
            Text("This is home but should not be here")
            // TODO remove HomeScreen from this navigation, it will be in other feature, this will be refactor in the future
            // this is just for development in the beginning
        }
    }
}