package com.desarrollodroide.adventurelog.feature.locations.ui.navigation

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
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
    navigator: LocationsNavigator,
    // The list is registered by the caller when it wants to wrap it in something of its own -
    // the two-pane places screen does, and needs to own that route rather than duplicate it.
    registerListRoute: Boolean = true,
    // Outside the shell - the root graph, where a place's page opened from Home or search lives -
    // nothing keeps the form off the status and gesture bars, so the form keeps itself inside them.
    standalone: Boolean = false
) {
    val formModifier = if (standalone) Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding() else Modifier
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    // Locations List Screen
    if (registerListRoute) composable(route = NavigationRoutes.Locations.route) {
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
        Box(formModifier) {
            AddEditLocationScreen(
                locationId = null,
                location = null,
                onNavigateBack = {
                    navigator.navigateBack()
                }
            )
        }
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
        
        Box(formModifier) {
            AddEditLocationScreen(
                locationId = adventureId,
                location = location,
                onNavigateBack = {
                    navigator.navigateBack()
                }
            )
        }
    }
}