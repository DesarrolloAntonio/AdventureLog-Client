package com.desarrollodroide.adventurelog.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.adventurelog.feature.home.home.navigation.Home
import com.desarrollodroide.adventurelog.feature.login.login.navigation.Login
import com.desarrollodroide.adventurelog.feature.login.login.navigation.loginGraph
import com.desarrollodroide.adventurelog.feature.home.home.navigation.homeGraph

@Composable
fun AdventureLogNavGraph(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    startDestination: Any = Login,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        startDestination = startDestination,
        navController = navController,
    ) {
        loginGraph(
            navController = navController,
            onBackClick = navController::navigateUp,
            onNavigateToHome = {
                navController.navigate(Home) {
                    popUpTo(Login) { inclusive = true }
                }
            }
        )

        homeGraph(
            navController = navController,
            onBackClick = navController::navigateUp,
            onAdventureClick = { adventureId ->
                // navController.navigateToAdventureDetail(adventureId)
            }
        )
    }
}

