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
                onAdventuresClick = { onAdventureClick("") },
                onCollectionsClick = { navController.navigate(CollectionsScreen) },
                onTravelClick = { navController.navigate(TravelScreen) },
                onMapClick = { navController.navigate(MapScreen) },
                onCalendarClick = { navController.navigate(CalendarScreen) }
            )
        }

        composable<AdventuresScreen> {

        }

        composable<CollectionsScreen> {
            // CollectionsScreenRoute()
        }

        composable<TravelScreen> {
            // TravelScreenRoute()
        }

        composable<MapScreen> {
            // MapScreenRoute()
        }

        composable<CalendarScreen> {
            // CalendarScreenRoute()
        }
    }
}