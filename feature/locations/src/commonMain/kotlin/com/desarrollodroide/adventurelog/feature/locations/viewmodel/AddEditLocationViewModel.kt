package com.desarrollodroide.adventurelog.feature.locations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ReverseGeocodeUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchWikipediaImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.WikipediaImageResult
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCategoryUseCase
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult

data class AddEditAdventureUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val categories: List<Category> = emptyList(),
    val existingLocation: Location? = null,
    val isGeneratingDescription: Boolean = false,
    val locationSearchResults: List<GeocodeSearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false,
    val reverseGeocodeResult: ReverseGeocodeResult? = null,
    val wikipediaImageState: WikipediaImageResult = WikipediaImageResult.Loading
)

class AddEditAdventureViewModel(
    private val createLocationUseCase: CreateLocationUseCase,
    private val updateLocationUseCase: UpdateLocationUseCase,
    private val getLocationUseCase: GetLocationUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val generateDescriptionUseCase: GenerateDescriptionUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
    private val searchWikipediaImageUseCase: SearchWikipediaImageUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val adventureId: String? = null,
    private val existingLocation: Location? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditAdventureUiState())
    val uiState: StateFlow<AddEditAdventureUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
        if (existingLocation != null) {
            _uiState.value = _uiState.value.copy(existingLocation = existingLocation)
        } else if (adventureId != null) {
            loadAdventure(adventureId)
        }
    }
    
    private fun loadAdventure(adventureId: String) {
        // Only load from server if we don't already have the adventure
        if (_uiState.value.existingLocation != null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getLocationUseCase(adventureId)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load adventure: ${result.value}"
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        existingLocation = result.value
                    )
                }
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = getCategoriesUseCase()) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.value
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        categories = result.value
                    )
                }
            }
        }
    }
    
    fun saveLocation(formData: LocationFormData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = if (adventureId != null) {
                updateLocationUseCase(
                    locationId = adventureId,
                    name = formData.name,
                    description = formData.description,
                    category = formData.category,
                    rating = formData.rating.toDouble(),
                    link = formData.link,
                    location = formData.location,
                    latitude = formData.latitude,
                    longitude = formData.longitude,
                    isPublic = formData.isPublic,
                    tags = formData.tags
                )
            } else {
                val category = formData.category
                if (category == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please select a category"
                    )
                    return@launch
                }
                
                createLocationUseCase(
                    name = formData.name,
                    description = formData.description,
                    category = category,
                    rating = formData.rating.toDouble(),
                    link = formData.link,
                    location = formData.location,
                    latitude = formData.latitude,
                    longitude = formData.longitude,
                    isPublic = formData.isPublic,
                    tags = formData.tags,
                    visits = formData.visits
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
                    // The adventure was created successfully
                    // We can access result.value if we need the created adventure
                }
            }
        }
    }
    
    fun clearSavedState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun generateDescription(name: String, onDescriptionGenerated: (String) -> Unit) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter an adventure name first"
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
        if (query.isBlank() || query.length <= 2) {
            _uiState.value = _uiState.value.copy(
                locationSearchResults = emptyList(),
                isSearchingLocation = false
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchingLocation = true,
                errorMessage = null
            )
            
            try {
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
            } catch (e: CancellationException) {
                _uiState.value = _uiState.value.copy(
                    isSearchingLocation = false,
                    locationSearchResults = emptyList()
                )
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearchingLocation = false,
                    locationSearchResults = emptyList(),
                    errorMessage = e.message ?: "Search failed"
                )
            }
        }
    }
    
    fun clearLocationSearch() {
        _uiState.value = _uiState.value.copy(
            locationSearchResults = emptyList()
        )
    }
    
    fun reverseGeocode(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            when (val result = reverseGeocodeUseCase(latitude, longitude)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.value
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        reverseGeocodeResult = result.value
                    )
                    // If we got a display name from reverse geocoding, we can update the location
                    // This would need to be handled in the UI layer
                }
            }
        }
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
    
    fun createCategory(name: String, icon: String) {
        viewModelScope.launch {
            when (val result = createCategoryUseCase(
                name = name.lowercase().replace(" ", "_"),
                displayName = name,
                icon = icon
            )) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Failed to create category: ${result.value}"
                    )
                }
                is Either.Right -> {
                    loadCategories()
                }
            }
        }
    }
}
