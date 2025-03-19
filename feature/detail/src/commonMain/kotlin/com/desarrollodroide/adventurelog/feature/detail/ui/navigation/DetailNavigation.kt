package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreenRoute

fun NavGraphBuilder.detailGraph(
    navController: NavHostController,
    onBackClick: () -> Unit
) {
    navigation(
        startDestination = AdventureDetailScreen.route,
        route = AdventureDetailRoute.route
    ) {
        composable(
            route = AdventureDetailScreen.route,
            arguments = listOf(navArgument("adventureId") { type = NavType.StringType })
        ) { backStackEntry ->
            val adventureId = backStackEntry.arguments?.getString("adventureId") ?: ""
            AdventureDetailScreenRoute(
                adventureId = adventureId,
                onBackClick = onBackClick
            )
        }
    }
}