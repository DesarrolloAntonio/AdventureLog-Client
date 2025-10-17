package com.desarrollodroide.adventurelog.feature.locations.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.AddEditLocationScreen
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.LocationListScreen
import kotlinx.serialization.json.Json

/**
 * Navigator interface for Locations feature
 * Defines external navigation actions that the Locations feature can trigger
 */
interface LocationsNavigator {
    fun navigateToLocationDetail(location: Location)
    fun navigateToAddLocation()
    fun navigateToEditLocation(locationId: String, locationJson: String)
    fun navigateBack()
}

/**
 * Extension function to add location screens to a navigation graph
 */
fun NavGraphBuilder.locationsScreen(
    navigator: LocationsNavigator
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    // Locations List Screen
    composable(route = NavigationRoutes.Locations.route) {
        LocationListScreen(
            onAdventureClick = { adventure ->
                navigator.navigateToLocationDetail(adventure)
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
    composable(route = NavigationRoutes.Locations.add) {
        AddEditLocationScreen(
            locationId = null,
            location = null,
            onNavigateBack = {
                navigator.navigateBack()
            }
        )
    }
    
    // Edit Adventure Screen
    composable(
        route = NavigationRoutes.Locations.editRoute,
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
            locationId = adventureId,
            location = location,
            onNavigateBack = {
                navigator.navigateBack()
            }
        )
    }
}