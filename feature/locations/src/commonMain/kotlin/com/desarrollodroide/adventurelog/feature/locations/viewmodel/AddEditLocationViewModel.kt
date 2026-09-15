package com.desarrollodroide.adventurelog.feature.locations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.visitFormOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.Currencies
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
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationTrailsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationVisitsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationImagesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UploadImageUseCase
import com.desarrollodroide.adventurelog.feature.ui.util.ImageBytesProvider
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType

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
    val wikipediaImageState: WikipediaImageResult = WikipediaImageResult.Idle,
    val uploadingImagesCount: Int = 0,
    val totalImagesToUpload: Int = 0,
    val isSavingLocation: Boolean = false,
    /** Editing: the place could not be loaded from the server, so there is no form to show. */
    val loadError: String? = null
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
    private val uploadImageUseCase: UploadImageUseCase,
    private val syncLocationVisitsUseCase: SyncLocationVisitsUseCase,
    private val syncLocationTrailsUseCase: SyncLocationTrailsUseCase,
    private val syncLocationImagesUseCase: SyncLocationImagesUseCase,
    private val imageBytesProvider: ImageBytesProvider,
    private val userRepository: UserRepository,
    private val adventureId: String? = null,
    private val existingLocation: Location? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AddEditAdventureUiState())
    val uiState: StateFlow<AddEditAdventureUiState> = _uiState.asStateFlow()

    /**
     * The account's preferred currency, which the web uses to pre-fill the money field on a new
     * item. Falls back to the shared default before the session has loaded.
     */
    val defaultCurrency: String
        get() = userRepository.activeSession?.defaultCurrency ?: Currencies.DEFAULT
    
    init {
        loadCategories()
        if (adventureId != null) {
            // The form is filled from the server, not from the copy the list handed over: saving
            // that copy put back every field as the list had loaded it, over a description changed
            // on the web in the meantime (measured).
            loadAdventure(adventureId)
        } else if (existingLocation != null) {
            _uiState.value = _uiState.value.copy(existingLocation = existingLocation)
        }
    }

    fun retryLoad() {
        adventureId?.let(::loadAdventure)
    }
    
    private fun loadAdventure(adventureId: String) {
        // Set before the coroutine starts, so the screen never shows an empty form in between.
        _uiState.value = _uiState.value.copy(isLoading = true, loadError = null)
        viewModelScope.launch {
            when (val result = getLocationUseCase(adventureId, fromServer = true)) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadError = result.value
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
    
    /**
     * The place a Create in this form has already made. Set, a later Save updates it: a photo that
     * failed to upload left the form open, and Create again would have posted a second place.
     */
    private var createdLocationId: String? = null
    private val uploadedImageUris = mutableSetOf<String>()

    fun saveLocation(formData: LocationFormData) {
        // One save at a time: a double tap on Create made two places (measured).
        if (_uiState.value.isSavingLocation) return
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            isSavingLocation = true,
            errorMessage = null
        )
        viewModelScope.launch {
            try {
                save(formData)
            } finally {
                _uiState.value = _uiState.value.copy(isSavingLocation = false)
            }
        }
    }

    private suspend fun save(formData: LocationFormData) {
            val targetId = adventureId ?: createdLocationId
            val retryOfCreate = adventureId == null && createdLocationId != null
            val result = if (targetId != null) {
                updateLocationUseCase(
                    locationId = targetId,
                    name = formData.name,
                    description = formData.description,
                    category = formData.category,
                    rating = formData.rating.toDouble(),
                    link = formData.link,
                    location = formData.location,
                    latitude = formData.latitude,
                    longitude = formData.longitude,
                    isPublic = formData.isPublic,
                    tags = formData.tags,
                    price = formData.price.toDoubleOrNull(),
                    priceCurrency = formData.priceCurrency
                )
            } else {
                val category = formData.category
                if (category == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please select a category"
                    )
                    return
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
                    visits = formData.visits,
                    price = formData.price.toDoubleOrNull(),
                    priceCurrency = formData.priceCurrency
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
                    val createdLocation = result.value
                    if (adventureId == null) createdLocationId = createdLocation.id

                    // Visits cannot ride along with the location: each one needs the id of a
                    // location that does not exist yet while it is being created. They are
                    // reconciled here, once there is an id, exactly as the web client does.
                    // A retried Create already sent its visits and trails the first time.
                    val existingVisits = _uiState.value.existingLocation?.visits.orEmpty()
                    val visitsResult = if (retryOfCreate) Either.Right(Unit) else syncLocationVisitsUseCase(
                        locationId = createdLocation.id,
                        existing = existingVisits,
                        edited = formData.visits,
                        original = existingVisits.map(::visitFormOf)
                    )
                    if (visitsResult is Either.Left) {
                        // The location itself saved, so this is a warning rather than a failure -
                        // silently dropping the dates would be worse than saying so.
                        _uiState.value = _uiState.value.copy(errorMessage = visitsResult.value)
                    }

                    // Trails carry a location id too, so they follow the same after-the-fact path.
                    val trailsResult = if (retryOfCreate) Either.Right(Unit) else syncLocationTrailsUseCase(
                        locationId = createdLocation.id,
                        existing = _uiState.value.existingLocation?.trails.orEmpty(),
                        edited = formData.trails
                    )
                    if (trailsResult is Either.Left) {
                        _uiState.value = _uiState.value.copy(errorMessage = trailsResult.value)
                    }

                    if (adventureId != null) {
                        val imagesResult = syncLocationImagesUseCase(
                            existing = _uiState.value.existingLocation?.images.orEmpty(),
                            keptIds = formData.images.mapNotNull { it.serverId },
                            primaryId = formData.images.firstOrNull { it.isPrimary }?.serverId
                        )
                        if (imagesResult is Either.Left) {
                            _uiState.value = _uiState.value.copy(errorMessage = imagesResult.value)
                        }
                    }

                    // Only photos added in this form are uploaded. Uploading the ones already on
                    // the server again doubled every photo of a public place on each save, and on
                    // a private one the download was refused, holding the form open with "Failed to
                    // read image file" after the place had already saved (measured).
                    val newImages = formData.images.filter { it.serverId == null && it.uri !in uploadedImageUris }
                    if (newImages.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            totalImagesToUpload = newImages.size,
                            uploadingImagesCount = 0
                        )
                        
                        uploadImages(
                            locationId = createdLocation.id,
                            images = newImages
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSaved = true
                        )
                    }
                }
            }
    }
    
    private suspend fun uploadImages(
        locationId: String,
        images: List<com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData>
    ) {
        var uploadedCount = 0
        var hasError = false
        
        for (image in images) {
            if (hasError) break
            
            val imageBytes = when (image.type) {
                ImageType.LOCAL_FILE -> {
                    imageBytesProvider.getImageBytes(image.uri)
                }
                ImageType.URL, ImageType.WIKIPEDIA -> {
                    imageBytesProvider.downloadImageFromUrl(image.uri)
                }
            }
            
            if (imageBytes == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to read image file"
                )
                hasError = true
                break
            }
            
            val fileName = when (image.type) {
                ImageType.LOCAL_FILE -> imageBytesProvider.getFileName(image.uri)
                ImageType.URL, ImageType.WIKIPEDIA -> {
                    val url = image.uri
                    val lastSegment = url.substringAfterLast('/')
                    if (lastSegment.contains('.')) {
                        lastSegment
                    } else {
                        "image.jpg"
                    }
                }
            }
            
            when (uploadImageUseCase(
                contentType = "location",
                objectId = locationId,
                imageBytes = imageBytes,
                fileName = fileName
            )) {
                is Either.Left -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to upload image: $fileName"
                    )
                    hasError = true
                }
                is Either.Right -> {
                    uploadedImageUris += image.uri
                    uploadedCount++
                    _uiState.value = _uiState.value.copy(
                        uploadingImagesCount = uploadedCount
                    )
                }
            }
        }
        
        if (!hasError) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isSaved = true,
                uploadingImagesCount = 0,
                totalImagesToUpload = 0
            )
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
                errorMessage = "Please enter a location name first"
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
            wikipediaImageState = WikipediaImageResult.Idle
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
