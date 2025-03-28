package com.desarrollodroide.adventurelog.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.homeNavGraph
import com.desarrollodroide.adventurelog.feature.login.ui.navigation.LoginNavigator
import com.desarrollodroide.adventurelog.feature.login.ui.navigation.loginNavGraph
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.DetailNavigator
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.detailNavGraph
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.HomeNavigator
import com.desarrollodroide.adventurelog.feature.ui.navigation.AnimatedNavHost

/**
 * Main navigation graph of the application
 * Acts as the entry point that connects all feature navigation graphs
 */
@Composable
fun AdventureLogNavGraph(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController = rememberNavController()
) {
    // Define navigators for each module
    val loginNavigator = object : LoginNavigator {
        override fun goToHome() {
            navController.navigate(NavigationRoutes.Home.graph) {
                popUpTo(NavigationRoutes.Login.graph) { inclusive = true }
            }
        }
    }
    
    val homeNavigator = object : HomeNavigator {
        override fun goToDetail(adventureId: String) {
            navController.navigate(NavigationRoutes.Detail.createDetailRoute(adventureId))
        }
    }
    
    val detailNavigator = object : DetailNavigator {
        override fun navigateUp() {
            navController.navigateUp()
        }
    }
    
    // Use the enhanced AnimatedNavHost instead of the standard NavHost
    AnimatedNavHost(
        modifier = modifier,
        startDestination = NavigationRoutes.Login.graph,
        navController = navController
    ) {
        loginNavGraph(navigator = loginNavigator)
        homeNavGraph(navigator = homeNavigator)
        detailNavGraph(navigator = detailNavigator)
    }
}