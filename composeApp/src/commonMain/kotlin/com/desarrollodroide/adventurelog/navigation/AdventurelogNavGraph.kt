package com.desarrollodroide.adventurelog.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.adventures.ui.navigation.adventuresNavGraph
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.detailNavGraph
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.homeNavGraph
import com.desarrollodroide.adventurelog.feature.login.ui.navigation.loginNavGraph
import com.desarrollodroide.adventurelog.feature.settings.ui.navigation.settingsNavGraph

/**
 * Main navigation graph of the application
 * Acts as the entry point that connects all feature navigation graphs
 */
@Composable
fun AdventureLogNavGraph(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        startDestination = NavigationRoutes.Login.route,
        navController = navController,
    ) {
        // Include each feature's navigation graph
        loginNavGraph(navController)
        homeNavGraph(navController)
        adventuresNavGraph(navController)
        detailNavGraph(navController)
        settingsNavGraph(navController)
    }
}