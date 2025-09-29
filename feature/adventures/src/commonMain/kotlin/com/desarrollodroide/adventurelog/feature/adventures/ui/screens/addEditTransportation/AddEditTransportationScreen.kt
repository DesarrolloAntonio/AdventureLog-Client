package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.components.BasicInfoTransportationSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.components.DateTransportationSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.components.LocationTransportationSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.components.TransportationImagesSection
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.AddEditTransportationViewModel
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.WikipediaImageState
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddEditTransportationScreen(
    transportationId: String?,
    transportation: Transportation?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel = koinViewModel<AddEditTransportationViewModel> {
        parametersOf(transportationId, transportation)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
            viewModel.clearSavedState()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AddEditTransportationContent(
            isEditMode = transportationId != null,
            existingTransportation = uiState.existingTransportation,
            transportationTypes = uiState.transportationTypes,
            isLoading = uiState.isLoading,
            onNavigateBack = onNavigateBack,
            onSave = { formData ->
                viewModel.saveTransportation(formData)
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

@Composable
fun AddEditTransportationContent(
    isEditMode: Boolean = false,
    existingTransportation: Transportation? = null,
    transportationTypes: List<String>,
    isLoading: Boolean = false,
    onNavigateBack: () -> Unit,
    onSave: (TransportationFormData) -> Unit,
    onGenerateDescription: (name: String, onDescriptionGenerated: (String) -> Unit) -> Unit,
    isGeneratingDescription: Boolean,
    locationSearchResults: List<com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult> = emptyList(),
    isSearchingLocation: Boolean = false,
    onSearchLocation: (String) -> Unit = {},
    onClearLocationSearch: () -> Unit = {},
    wikipediaImageState: WikipediaImageState = WikipediaImageState.Idle,
    onSearchWikipediaImage: (String) -> Unit = {},
    onResetWikipediaState: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var formData by remember(existingTransportation) {
        mutableStateOf(
            if (existingTransportation != null) {
                TransportationFormData.fromTransportation(existingTransportation)
            } else {
                TransportationFormData()
            }
        )
    }

    if (isLoading && existingTransportation == null && isEditMode) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        BasicInfoTransportationSection(
            formData = formData,
            transportationTypes = transportationTypes,
            onFormDataChange = { formData = it },
            onNavigateBack = onNavigateBack,
            onGenerateDescription = {
                onGenerateDescription(formData.name) { generatedDescription ->
                    formData = formData.copy(description = generatedDescription)
                }
            },
            isGeneratingDescription = isGeneratingDescription
        )

        DateTransportationSection(
            formData = formData,
            onFormDataChange = { formData = it }
        )

        LocationTransportationSection(
            formData = formData,
            onFormDataChange = { formData = it },
            locationSearchResults = locationSearchResults,
            isSearchingLocation = isSearchingLocation,
            onSearchLocation = onSearchLocation,
            onClearLocationSearch = onClearLocationSearch
        )

        TransportationImagesSection(
            formData = formData,
            onFormDataChange = { formData = it },
            wikipediaImageState = wikipediaImageState,
            onSearchWikipediaImage = onSearchWikipediaImage,
            onResetWikipediaState = onResetWikipediaState
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                onClick = { onSave(formData) },
                text = if (isEditMode) "Update Transportation" else "Create Transportation"
            )

            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
