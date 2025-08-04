package com.desarrollodroide.adventurelog.feature.world.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.world.ui.screen.CountryDetailScreen
import com.desarrollodroide.adventurelog.feature.world.ui.screen.WorldScreen

fun NavGraphBuilder.worldGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = NavigationRoutes.Travel.route,
        route = "world_graph"
    ) {
        composable(NavigationRoutes.Travel.route) {
            WorldScreen(
                onCountryClick = { countryCode ->
                    navController.navigate("${NavigationRoutes.Travel.route}/$countryCode")
                },
                onMapClick = {
                    // TODO: Navigate to map view
                }
            )
        }
        
        composable("${NavigationRoutes.Travel.route}/{countryCode}") { backStackEntry ->
            val countryCode = backStackEntry.savedStateHandle.get<String>("countryCode") ?: ""
            CountryDetailScreen(
                countryCode = countryCode,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
