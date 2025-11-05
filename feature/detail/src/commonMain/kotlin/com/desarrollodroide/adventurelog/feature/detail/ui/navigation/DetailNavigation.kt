package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreenRoute
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations

fun NavGraphBuilder.detailNavGraph(
    navigator: DetailNavigator
) {
    navigation(
        startDestination = "detail/{locationId}",
        route = NavigationRoutes.Detail.route
    ) {
        composable(
            route = "detail/{locationId}",
            arguments = listOf(
                navArgument("locationId") { type = NavType.StringType }
            ),
            enterTransition = NavigationAnimations.enterTransitionVertical,
            exitTransition = NavigationAnimations.exitTransitionFade,
            popEnterTransition = NavigationAnimations.enterTransitionFade,
            popExitTransition = NavigationAnimations.exitTransitionVertical
        ) { backStackEntry ->
            val locationId = backStackEntry.savedStateHandle.get<String>("locationId") ?: ""
            
            println("📥 [DetailNav] Received locationId: $locationId")
            
            AdventureDetailScreenRoute(
                locationId = locationId,
                onBackClick = { navigator.navigateUp() }
            )
        }
    }
}