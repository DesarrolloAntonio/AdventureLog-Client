package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreenRoute
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations
import kotlinx.serialization.json.Json
import com.desarrollodroide.adventurelog.core.model.Adventure

fun NavGraphBuilder.detailNavGraph(
    navigator: DetailNavigator
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    navigation(
        startDestination = "detail?adventureJson={adventureJson}",
        route = NavigationRoutes.Detail.route
    ) {
        composable(
            route = "detail?adventureJson={adventureJson}",
            arguments = listOf(
                navArgument("adventureJson") { type = NavType.StringType }
            ),
            enterTransition = NavigationAnimations.enterTransitionVertical,
            exitTransition = NavigationAnimations.exitTransitionFade,
            popEnterTransition = NavigationAnimations.enterTransitionFade,
            popExitTransition = NavigationAnimations.exitTransitionVertical
        ) { backStackEntry ->
            val adventureJson = backStackEntry.savedStateHandle.get<String>("adventureJson") ?: ""
            
            val adventure = json.decodeFromString<Adventure>(adventureJson)
            
            AdventureDetailScreenRoute(
                adventure = adventure,
                onBackClick = { navigator.navigateUp() }
            )
        }
    }
}