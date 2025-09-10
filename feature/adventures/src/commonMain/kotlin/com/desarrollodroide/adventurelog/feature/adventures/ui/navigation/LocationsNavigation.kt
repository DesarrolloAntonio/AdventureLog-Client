package com.desarrollodroide.adventurelog.feature.adventures.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Collection as AdventureCollection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.AddEditLocationScreen
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.locationsList.LocationListScreen
import kotlinx.serialization.json.Json

/**
 * Navigator interface for Locations feature
 * Defines external navigation actions that the Locations feature can trigger
 */
interface AdventuresNavigator {
    fun navigateToLocationDetail(location: Location, collections: List<AdventureCollection>)
    fun navigateToAddLocation()
    fun navigateToEditLocation(adventureId: String, adventureJson: String)
    fun navigateBack()
}

/**
 * Extension function to add adventures screens to a navigation graph
 */
fun NavGraphBuilder.adventuresScreen(
    navigator: AdventuresNavigator
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    // Locations List Screen
    composable(route = NavigationRoutes.Adventures.route) {
        LocationListScreen(
            onAdventureClick = { adventure, collections ->
                navigator.navigateToLocationDetail(adventure, collections)
            },
            onAddAdventureClick = {
                navigator.navigateToAddLocation()
            },
            onEditAdventure = { adventure ->
                val adventureJson = json.encodeToString(adventure)
                navigator.navigateToEditLocation(adventure.id, adventureJson)
            }
        )
    }
    
    // Add Adventure Screen
    composable(route = NavigationRoutes.Adventures.add) {
        AddEditLocationScreen(
            adventureId = null,
            location = null,
            onNavigateBack = {
                navigator.navigateBack()
            }
        )
    }
    
    // Edit Adventure Screen
    composable(
        route = NavigationRoutes.Adventures.editRoute,
        arguments = listOf(
            navArgument("adventureId") { 
                type = NavType.StringType 
            },
            navArgument("adventureJson") { 
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val adventureId = backStackEntry.savedStateHandle.get<String>("adventureId") ?: ""
        val adventureJson = backStackEntry.savedStateHandle.get<String>("adventureJson") ?: ""
        
        val location = if (adventureJson.isNotEmpty()) {
            json.decodeFromString<Location>(adventureJson)
        } else {
            null
        }
        
        AddEditLocationScreen(
            adventureId = adventureId,
            location = location,
            onNavigateBack = {
                navigator.navigateBack()
            }
        )
    }
}