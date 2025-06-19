package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionDetailScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionsScreen
import com.desarrollodroide.adventurelog.core.model.Adventure
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment

/**
 * Extension function to add collections screen to a navigation graph
 * Meant to be used internally by the Home feature
 */
fun NavGraphBuilder.collectionsScreen(
    onCollectionClick: (String, String) -> Unit, // Now passes both ID and name
    onHomeClick: () -> Unit,
    onAdventureClick: (Adventure) -> Unit,
    onAddCollectionClick: () -> Unit,
    navController: NavController
) {
    // Collections List Screen
    composable(route = NavigationRoutes.Collections.route) {
        CollectionsScreen(
            onCollectionClick = onCollectionClick,
            onAddCollectionClick = onAddCollectionClick
        )
    }
    
    // Collection Detail Screen
    composable(
        route = "collection/{collectionId}/{collectionName}",
        arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
            },
            navArgument("collectionName") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val collectionId = backStackEntry.arguments?.getString("collectionId") ?: ""
        CollectionDetailScreen(
            collectionId = collectionId,
            onBackClick = { 
                // Navigate back to collections list
                navController.navigateUp()
            },
            onHomeClick = onHomeClick,
            onAdventureClick = onAdventureClick
        )
    }
    
    // Add Collection Screen
    composable(route = "add_collection") {
        val viewModel = koinViewModel<com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditCollectionViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
        
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
            com.desarrollodroide.adventurelog.feature.collections.ui.screens.AddEditCollectionScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onSave = { formData ->
                    viewModel.saveCollection(formData)
                }
            )
            
            androidx.compose.material3.SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}