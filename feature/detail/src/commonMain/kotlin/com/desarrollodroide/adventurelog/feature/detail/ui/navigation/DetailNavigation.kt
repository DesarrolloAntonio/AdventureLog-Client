package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreenRoute
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations

/**
 * Detail module navigation graph
 */
fun NavGraphBuilder.detailNavGraph(
    navigator: DetailNavigator
) {
    navigation(
        startDestination = "detail/{adventureId}",
        route = NavigationRoutes.Detail.route
    ) {
        composable(
            route = "detail/{adventureId}",
            arguments = listOf(navArgument("adventureId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "adventurelog://detail/{adventureId}"
                }
            ),
            // Custom detail animation (slide up from bottom for a modal-like effect)
            enterTransition = NavigationAnimations.enterTransitionVertical,
            exitTransition = NavigationAnimations.exitTransitionFade,
            popEnterTransition = NavigationAnimations.enterTransitionFade,
            popExitTransition = NavigationAnimations.exitTransitionVertical
        ) { backStackEntry ->
            val adventureId = backStackEntry.arguments?.getString("adventureId") ?: ""
            AdventureDetailScreenRoute(
                adventureId = adventureId,
                onBackClick = { navigator.navigateUp() }
            )
        }
    }
}