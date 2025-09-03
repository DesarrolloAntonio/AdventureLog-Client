package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import app.cash.paging.compose.LazyPagingItems
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.AddEditCollectionScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionDetailScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionsScreen
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditCollectionViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Navigator interface for Collections feature
 * Defines external navigation actions that the Collections feature can trigger
 */
interface CollectionsNavigator {
    fun navigateToCollectionDetail(collectionId: String, collectionName: String)
    fun navigateToAddCollection()
    fun navigateToEditCollection(collectionId: String)
    fun navigateToAdventure(location: Location)
    fun navigateToHome()
    fun navigateBack()
}

/**
 * Extension function to add collections screens to a navigation graph
 */
fun NavGraphBuilder.collectionsScreen(
    navigator: CollectionsNavigator
) {
    // Collections List Screen
    composable(route = NavigationRoutes.Collections.route) { backStackEntry ->
        val pagingItems = remember { mutableStateOf<LazyPagingItems<Collection>?>(null) }
        
        // Listen for refresh flag
        val refresh = backStackEntry.savedStateHandle.get<Boolean>("refresh") ?: false
        LaunchedEffect(refresh) {
            if (refresh) {
                pagingItems.value?.refresh()
                backStackEntry.savedStateHandle["refresh"] = false
            }
        }
        
        CollectionsScreen(
            onCollectionClick = { collectionId, collectionName ->
                navigator.navigateToCollectionDetail(collectionId, collectionName)
            },
            onAddCollectionClick = {
                navigator.navigateToAddCollection()
            },
            onEditCollection = { collection ->
                navigator.navigateToEditCollection(collection.id)
            },
            onPagingItemsReady = { items ->
                pagingItems.value = items
            }
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
        val collectionId = backStackEntry.savedStateHandle.get<String>("collectionId") ?: ""
        CollectionDetailScreen(
            collectionId = collectionId,
            onBackClick = { 
                navigator.navigateBack()
            },
            onHomeClick = {
                navigator.navigateToHome()
            },
            onAdventureClick = { adventure ->
                navigator.navigateToAdventure(adventure)
            }
        )
    }
    
    // Add Collection Screen
    composable(route = "add_collection") { backStackEntry ->
        val viewModel = koinViewModel<AddEditCollectionViewModel> {
            parametersOf(null) // null for new collection
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Handle navigation when save is successful
        LaunchedEffect(uiState.isSaved) {
            if (uiState.isSaved) {
                navigator.navigateBack()
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                AddEditCollectionScreen(
                    onNavigateBack = {
                        navigator.navigateBack()
                    },
                    onSave = { formData ->
                        viewModel.saveCollection(formData)
                    },
                    initialData = uiState.initialData
                )
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
    
    // Edit Collection Screen
    composable(
        route = "edit_collection/{collectionId}",
        arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val collectionId = backStackEntry.savedStateHandle.get<String>("collectionId") ?: ""
        val viewModel = koinViewModel<AddEditCollectionViewModel> {
            parametersOf(collectionId)
        }
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
        
        // Handle navigation when save is successful
        LaunchedEffect(uiState.isSaved) {
            if (uiState.isSaved) {
                // Set a flag to refresh the collections list
                backStackEntry.savedStateHandle["refresh"] = true
                navigator.navigateBack()
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
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                AddEditCollectionScreen(
                    onNavigateBack = {
                        navigator.navigateBack()
                    },
                    onSave = { formData ->
                        viewModel.saveCollection(formData)
                    },
                    initialData = uiState.initialData
                )
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}