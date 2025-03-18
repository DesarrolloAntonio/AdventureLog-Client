package com.desarrollodroide.adventurelog.feature.home.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.feature.home.ui.HomeScreenRoute

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    onBackClick: () -> Unit,
    onAdventureClick: (String) -> Unit = {}
) {
    navigation<Home>(
        startDestination = HomeScreen
    ) {
        composable<HomeScreen> {
            HomeScreenRoute(
                onAdventuresClick = { },
                onCollectionsClick = { },
                onTravelClick = { },
                onMapClick = { },
                onCalendarClick = { },
                onSettingsClick = { }
            )
        }
    }
}