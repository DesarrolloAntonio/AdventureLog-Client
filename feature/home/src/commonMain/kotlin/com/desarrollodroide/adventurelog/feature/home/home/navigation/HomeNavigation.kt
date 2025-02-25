package com.desarrollodroide.adventurelog.feature.home.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desarrollodroide.adventurelog.feature.home.home.HomeScreenRoute

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
                onAdventuresClick = { navController.navigate(AdventuresScreen) },
                onCollectionsClick = { navController.navigate(CollectionsScreen) },
                onTravelClick = { navController.navigate(TravelScreen) },
                onMapClick = { navController.navigate(MapScreen) },
                onCalendarClick = { navController.navigate(CalendarScreen) },
                onUsersClick = { navController.navigate(UsersScreen) }
            )
        }

        composable<AdventuresScreen> {
            // AdventuresScreenRoute()
        }

        composable<CollectionsScreen> {
            // CollectionsScreenRoute()
        }

    }
}