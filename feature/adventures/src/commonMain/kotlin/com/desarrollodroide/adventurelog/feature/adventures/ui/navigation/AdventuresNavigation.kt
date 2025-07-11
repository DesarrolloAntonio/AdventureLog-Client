package com.desarrollodroide.adventurelog.feature.adventures.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.AddEditAdventureScreen
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.adventuresList.AdventureListScreen
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AddEditAdventureViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.material3.SnackbarHost
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment

/**
 * Extension function to add adventures screens to a navigation graph
 */
fun NavGraphBuilder.adventuresScreen(
    onAdventureClick: (Adventure, List<Collection>) -> Unit,
    onAddAdventureClick: () -> Unit,
    onManageCategoriesClick: () -> Unit,
    navController: NavController,
    collections: List<Collection> = emptyList()
) {
    // Adventures List Screen
    composable(route = NavigationRoutes.Adventures.route) {
        AdventureListScreen(
            onAdventureClick = onAdventureClick,
            onAddAdventureClick = onAddAdventureClick,
            onManageCategoriesClick = onManageCategoriesClick,
            collections = collections
        )
    }
    
    // Add Adventure Screen
    composable(route = "add_adventure") {
        val viewModel = koinViewModel<AddEditAdventureViewModel> { 
            parametersOf(null) // null for new adventure
        }
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Handle navigation when save is successful
        LaunchedEffect(uiState.isSaved) {
            if (uiState.isSaved) {
                navController.navigateUp()
                viewModel.clearSavedState()
            }
        }
        
        // Show error if any
        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let { message ->
                snackbarHostState.showSnackbar(message)
            }
        }
        
        Box(modifier = Modifier.fillMaxSize()) {
            AddEditAdventureScreen(
                categories = uiState.categories,
                onNavigateBack = {
                    navController.navigateUp()
                },
                onSave = { formData ->
                    viewModel.saveAdventure(formData)
                },
                onGenerateDescription = { name, onDescriptionGenerated ->
                    viewModel.generateDescription(name, onDescriptionGenerated)
                },
                isGeneratingDescription = uiState.isGeneratingDescription,
                locationSearchResults = uiState.locationSearchResults,
                isSearchingLocation = uiState.isSearchingLocation,
                onSearchLocation = { query ->
                    viewModel.searchLocations(query)
                },
                onClearLocationSearch = {
                    viewModel.clearLocationSearch()
                },
                onReverseGeocode = { lat, lon ->
                    viewModel.reverseGeocode(lat, lon)
                },
                reverseGeocodeResult = uiState.reverseGeocodeResult,
                wikipediaImageState = uiState.wikipediaImageState,
                onSearchWikipediaImage = { query ->
                    viewModel.searchWikipediaImage(query)
                },
                onResetWikipediaState = {
                    viewModel.resetWikipediaImageState()
                }
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
