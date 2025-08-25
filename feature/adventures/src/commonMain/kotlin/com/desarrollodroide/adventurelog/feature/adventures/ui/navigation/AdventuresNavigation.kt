package com.desarrollodroide.adventurelog.feature.adventures.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.AddEditAdventureContent
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.AddEditAdventureScreen
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.adventuresList.AdventureListScreen
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AddEditAdventureViewModel
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Extension function to add adventures screens to a navigation graph
 */
fun NavGraphBuilder.adventuresScreen(
    onAdventureClick: (Adventure, List<Collection>) -> Unit,
    navController: NavController,
    collections: List<Collection> = emptyList()
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    // Adventures List Screen
    composable(route = NavigationRoutes.Adventures.route) {
        AdventureListScreen(
            onAdventureClick = onAdventureClick,
            onAddAdventureClick = {
                navController.navigate(NavigationRoutes.Adventures.add)
            },
            onEditAdventure = { adventure ->
                val adventureJson = json.encodeToString(adventure)
                navController.navigate(NavigationRoutes.Adventures.createEditRoute(adventure.id, adventureJson))
            },
            collections = collections
        )
    }
    
    // Add Adventure Screen
    composable(route = NavigationRoutes.Adventures.add) {
        AddEditAdventureScreen(
            adventureId = null,
            adventure = null,
            navController = navController
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
        
        val adventure = if (adventureJson.isNotEmpty()) {
            json.decodeFromString<Adventure>(adventureJson)
        } else {
            null
        }
        
        AddEditAdventureScreen(
            adventureId = adventureId,
            adventure = adventure,
            navController = navController
        )
    }
}
