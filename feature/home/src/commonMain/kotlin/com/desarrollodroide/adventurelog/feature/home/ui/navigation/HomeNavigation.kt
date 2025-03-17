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
                // Estos callbacks ya no navegan a otra pantalla, sino que cambian el contenido interno
                onAdventuresClick = { },
                onCollectionsClick = { },
                onTravelClick = { },
                onMapClick = { },
                onCalendarClick = { },
                onSettingsClick = { }
            )
        }

        // Estos destinos ya no son necesarios, ya que la navegación es interna
        // Se pueden eliminar o mantener para compatibilidad con versiones anteriores
    }
}