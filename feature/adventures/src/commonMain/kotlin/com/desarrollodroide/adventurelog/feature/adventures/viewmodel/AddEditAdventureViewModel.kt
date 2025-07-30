package com.desarrollodroide.adventurelog.feature.adventures.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.CreateAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.UpdateAdventureUseCase
import com.desarrollodroide.adventurelog.core.domain.GetAdventureUseCase
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.core.domain.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.ReverseGeocodeUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchWikipediaImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.WikipediaImageResult
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult
import kotlinx.coroutines.flow.collectLatest

data class AddEditAdventureUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
    val categories: List<Category> = emptyList(),
    val existingAdventure: Adventure? = null,
    val isGeneratingDescription: Boolean = false,
    val locationSearchResults: List<GeocodeSearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false,
    val reverseGeocodeResult: ReverseGeocodeResult? = null,
    val wikipediaImageState: WikipediaImageState = WikipediaImageState.Idle
)

sealed class WikipediaImageState {
    object Idle : WikipediaImageState()
    object Searching : WikipediaImageState()
    data class Success(val imageUrl: String) : WikipediaImageState()
    data class Error(val message: String) : WikipediaImageState()
    object NotFound : WikipediaImageState()
}

class AddEditAdventureViewModel(
    private val createAdventureUseCase: CreateAdventureUseCase,
    private val updateAdventureUseCase: UpdateAdventureUseCase,
    private val getAdventureUseCase: GetAdventureUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val generateDescriptionUseCase: GenerateDescriptionUseCase,
    private val searchLocationsUseCase: SearchLocationsUseCase,
    private val reverseGeocodeUseCase: ReverseGeocodeUseCase,
    private val searchWikipediaImageUseCase: SearchWikipediaImageUseCase,
    private val adventureId: String? = null,
    private val existingAdventure: Adventure? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditAdventureUiState())
    val uiState: StateFlow<AddEditAdventureUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
        if (existingAdventure != null) {
            _uiState.value = _uiState.value.copy(existingAdventure = existingAdventure)
        } else if (adventureId != null) {
            loadAdventure(adventureId)
        }
    }
    
    private fun loadAdventure(adventureId: String) {
        // Only load from server if we don't already have the adventure
        if (_uiState.value.existingAdventure != null) {
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            when (val result = getAdventureUseCase(adventureId)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load adventure: ${result.value}"
                    )
                }
                is Either.Right -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        existingAdventure = result.value
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
    
    fun saveAdventure(formData: AdventureFormData) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = if (adventureId != null) {
                // Update existing adventure
                updateAdventureUseCase(
                    adventureId = adventureId,
                    name = formData.name,
                    description = formData.description,
                    categoryName = formData.category?.id?:"",
                    rating = formData.rating.toDouble(),
                    link = formData.link,
                    location = formData.location,
                    latitude = formData.latitude,
                    longitude = formData.longitude,
                    isPublic = formData.isPublic,
                    tags = formData.tags
                )
            } else {
                // Create new adventure
                val category = formData.category
                if (category == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please select a category"
                    )
                    return@launch
                }
                
                createAdventureUseCase(
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
                    wikipediaImageState = when (result) {
                        is WikipediaImageResult.Loading -> WikipediaImageState.Searching
                        is WikipediaImageResult.Success -> WikipediaImageState.Success(result.imageUrl)
                        is WikipediaImageResult.NotFound -> WikipediaImageState.NotFound
                        is WikipediaImageResult.Error -> WikipediaImageState.Error(result.message)
                    }
                )
            }
        }
    }
    
    fun resetWikipediaImageState() {
        _uiState.value = _uiState.value.copy(
            wikipediaImageState = WikipediaImageState.Idle
        )
    }
}
