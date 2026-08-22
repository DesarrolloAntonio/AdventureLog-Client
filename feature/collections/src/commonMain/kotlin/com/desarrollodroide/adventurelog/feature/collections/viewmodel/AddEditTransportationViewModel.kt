package com.desarrollodroide.adventurelog.feature.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchWikipediaImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.WikipediaImageResult
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AddEditTransportationUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val existingTransportation: Transportation? = null,
    val transportationTypes: List<String> = TransportationFormData.getDefaultTransportationTypes(),
    val isGeneratingDescription: Boolean = false,
    val locationSearchResults: List<GeocodeSearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false,
    val wikipediaImageState: WikipediaImageResult = WikipediaImageResult.Loading
)

class AddEditTransportationViewModel(
    private val createTransportationUseCase: CreateTransportationUseCase,
    private val updateTransportationUseCase: UpdateTransportationUseCase,
    private val getTransportationUseCase: GetTransportationUseCase,
    private val generateDescriptionUseCase: GenerateDescriptionUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val searchWikipediaImageUseCase: SearchWikipediaImageUseCase,
    private val transportationId: String? = null,
    private val existingTransportation: Transportation? = null,
    // Transportations belong to a collection; created without one they are orphaned and never
    // appear in the collection detail screen.
    private val collectionId: String? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditTransportationUiState())
    val uiState: StateFlow<AddEditTransportationUiState> = _uiState.asStateFlow()
    
    init {
        if (existingTransportation != null) {
            _uiState.value = _uiState.value.copy(existingTransportation = existingTransportation)
        } else if (transportationId != null) {
            loadTransportation(transportationId)
        }
    }
    
    private fun loadTransportation(transportationId: String) {
        if (_uiState.value.existingTransportation != null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getTransportationUseCase(transportationId)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load transportation: ${result.value}"
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        existingTransportation = result.value
                    )
                }
            }
        }
    }
    
    fun saveTransportation(formData: TransportationFormData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            // Convert ImageFormData to List<String> (only URLs)
            val imageUrls = formData.images
                .filter { it.type == ImageType.URL }
                .map { it.uri }
            
            if (formData.images.any { it.type == ImageType.LOCAL_FILE }) {
                // Note: Local images are not yet supported
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Note: Local image uploads are not yet supported. Only URL and Wikipedia images were saved."
                )
            }
            
            val result = if (transportationId != null) {
                updateTransportationUseCase(
                    transportationId = transportationId,
                    name = formData.name,
                    type = formData.type,
                    description = formData.description,
                    rating = formData.rating.toDouble(),
                    link = formData.link,
                    fromLocation = formData.fromLocation,
                    toLocation = formData.toLocation,
                    departureDate = formData.departureDate,
                    arrivalDate = formData.arrivalDate,
                    departureTimezone = formData.departureTimezone,
                    arrivalTimezone = formData.arrivalTimezone,
                    flightNumber = formData.flightNumber,
                    distance = formData.distance,
                    originLatitude = formData.originLatitude,
                    originLongitude = formData.originLongitude,
                    destinationLatitude = formData.destinationLatitude,
                    destinationLongitude = formData.destinationLongitude,
                    isPublic = formData.isPublic,
                    images = imageUrls,
                    attachments = formData.attachments
                )
            } else {
                createTransportationUseCase(
                    name = formData.name,
                    type = formData.type,
                    description = formData.description,
                    rating = formData.rating.toDouble(),
                    link = formData.link,
                    fromLocation = formData.fromLocation,
                    toLocation = formData.toLocation,
                    departureDate = formData.departureDate,
                    arrivalDate = formData.arrivalDate,
                    departureTimezone = formData.departureTimezone,
                    arrivalTimezone = formData.arrivalTimezone,
                    flightNumber = formData.flightNumber,
                    distance = formData.distance,
                    originLatitude = formData.originLatitude,
                    originLongitude = formData.originLongitude,
                    destinationLatitude = formData.destinationLatitude,
                    destinationLongitude = formData.destinationLongitude,
                    isPublic = formData.isPublic,
                    images = imageUrls,
                    attachments = formData.attachments,
                    collectionId = collectionId
                )
            }
            
            when (result) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.value
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
            }
        }
    }
    
    fun generateDescription(name: String, onDescriptionGenerated: (String) -> Unit) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter a transportation name first"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingDescription = true,
                errorMessage = null
            )
            
            when (val result = generateDescriptionUseCase(name)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingDescription = false,
                        errorMessage = "Failed to generate description: ${result.value}"
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingDescription = false
                    )
                    onDescriptionGenerated(result.value)
                }
            }
        }
    }
    
    fun searchLocations(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                locationSearchResults = emptyList()
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchingLocation = true,
                errorMessage = null
            )
            
            when (val result = searchLocationsUseCase(query)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isSearchingLocation = false,
                        locationSearchResults = emptyList(),
                        errorMessage = result.value
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        isSearchingLocation = false,
                        locationSearchResults = result.value
                    )
                }
            }
        }
    }
    
    fun clearLocationSearch() {
        _uiState.value = _uiState.value.copy(
            locationSearchResults = emptyList()
        )
    }
    
    fun searchWikipediaImage(query: String) {
        viewModelScope.launch {
            searchWikipediaImageUseCase(query).collectLatest { result ->
                _uiState.value = _uiState.value.copy(
                    wikipediaImageState = result
                )
            }
        }
    }
    
    fun resetWikipediaImageState() {
        _uiState.value = _uiState.value.copy(
            wikipediaImageState = WikipediaImageResult.Loading
        )
    }
    
    fun clearSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
