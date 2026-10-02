package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import androidx.compose.foundation.layout.statusBarsPadding
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
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.AddEditCollectionScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionDetailScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionsScreen
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditCollectionViewModel
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.Lodging

/**
 * Navigator interface for Collections feature
 * Defines external navigation actions that the Collections feature can trigger
 */
interface CollectionsNavigator {
    fun navigateToCollectionDetail(collectionId: String, collectionName: String)
    fun navigateToAddCollection()
    fun navigateToEditCollection(collectionId: String)
    fun navigateToAdventure(location: Location)
    /** [openImages] opens the form on its images, for "Add photo". */
    fun navigateToEditAdventure(adventure: Location, openImages: Boolean = false)
    fun navigateToAddTransportation(collectionId: String)
    fun navigateToEditTransportation(transportationId: String, transportationJson: String)
    fun navigateToAddNote(collectionId: String)
    fun navigateToEditNote(collectionId: String, noteId: String, noteJson: String)
    fun navigateToAddChecklist(collectionId: String)
    fun navigateToEditChecklist(collectionId: String, checklistId: String, checklistJson: String)
    fun navigateToAddLodging(collectionId: String)
    fun navigateToEditLodging(collectionId: String, lodgingId: String, lodgingJson: String)
    fun navigateToHome()
    fun navigateBack()
}

/**
 * Extension function to add collections screens to a navigation graph
 */
fun NavGraphBuilder.collectionsScreen(
    navigator: CollectionsNavigator,
    // The two-pane collections screen registers the list itself, so it can wrap it.
    registerListRoute: Boolean = true,
    // Outside the shell - a collection opened from a place's page, on the root graph - no app bar
    // names the collection or offers a way back, and nothing keeps the content off the status bar.
    // The screen then draws its own title row and stays below the status bar.
    standalone: Boolean = false
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    // Collections List Screen
    if (registerListRoute) composable(route = NavigationRoutes.Collections.route) { backStackEntry ->
        val pagingItems = remember { mutableStateOf<LazyPagingItems<UltraSlimCollection>?>(null) }
        
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
        route = NavigationRoutes.Collections.detailRoute,
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
            showTitle = standalone,
            modifier = if (standalone) Modifier.statusBarsPadding() else Modifier,
            onBackClick = { 
                navigator.navigateBack()
            },
            onHomeClick = {
                navigator.navigateToHome()
            },
            onAdventureClick = { adventure ->
                navigator.navigateToAdventure(adventure)
            },
            onEditAdventure = { adventure ->
                navigator.navigateToEditAdventure(adventure)
            },
            onAddPhoto = { adventure ->
                navigator.navigateToEditAdventure(adventure, openImages = true)
            },
            onAddTransportation = {
                navigator.navigateToAddTransportation(collectionId)
            },
            onEditTransportation = { transportation ->
                val transportationJson = json.encodeToString(
                    serializer = Transportation.serializer(),
                    value = transportation
                )
                navigator.navigateToEditTransportation(transportation.id, transportationJson)
            },
            onAddNote = { id -> navigator.navigateToAddNote(id) },
            onAddChecklist = { id -> navigator.navigateToAddChecklist(id) },
            onAddLodging = { id -> navigator.navigateToAddLodging(id) },
            onEditLodging = { id, stay ->
                navigator.navigateToEditLodging(
                    collectionId = id,
                    lodgingId = stay.id,
                    lodgingJson = json.encodeToString(serializer = Lodging.serializer(), value = stay)
                )
            },
            onEditChecklist = { id, list ->
                navigator.navigateToEditChecklist(
                    collectionId = id,
                    checklistId = list.id,
                    checklistJson = json.encodeToString(
                        serializer = Checklist.serializer(),
                        value = list
                    )
                )
            },
            onEditNote = { id, note ->
                navigator.navigateToEditNote(
                    collectionId = id,
                    noteId = note.id,
                    noteJson = json.encodeToString(serializer = Note.serializer(), value = note)
                )
            }
        )
    }
    
    // Add Collection Screen
    composable(route = NavigationRoutes.Collections.add) { backStackEntry ->
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
                    initialData = uiState.initialData,
                    isSaving = uiState.isSaving
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
        route = NavigationRoutes.Collections.editRoute,
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
                    initialData = uiState.initialData,
                    isSaving = uiState.isSaving
                )
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}