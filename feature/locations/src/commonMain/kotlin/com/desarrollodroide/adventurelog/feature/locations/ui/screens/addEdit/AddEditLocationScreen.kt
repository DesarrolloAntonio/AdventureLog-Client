package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit

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
import androidx.compose.material3.darkColorScheme
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
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.City
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.BasicInfoSection
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.DateSection
import com.desarrollodroide.adventurelog.feature.ui.components.ImagesSection
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.LocationSection
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.TagsSection
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.AddEditAdventureViewModel
import com.desarrollodroide.adventurelog.core.domain.usecase.WikipediaImageResult
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private data class SplitDateTime(
    val date: String,
    val time: String?
)

private fun splitIsoDateTime(isoString: String?): SplitDateTime {
    if (isoString.isNullOrBlank()) {
        return SplitDateTime(date = "", time = null)
    }
    
    return try {
        if (isoString.contains('T')) {
            val parts = isoString.split('T')
            val date = parts[0]
            val timePart = parts.getOrNull(1)?.substringBefore('+')?.substringBefore('Z') ?: ""
            val time = if (timePart.isNotEmpty()) {
                timePart.substring(0, minOf(5, timePart.length))
            } else null
            
            SplitDateTime(date = date, time = time)
        } else {
            SplitDateTime(date = isoString, time = null)
        }
    } catch (e: Exception) {
        SplitDateTime(date = isoString, time = null)
    }
}

/**
 * True when this bound carries no meaningful time of day.
 *
 * Midnight is how an all-day visit is stored now; end-of-day is the older form the server still
 * accepts and normalises. Both have to read as all-day, or a visit saved before the app started
 * sending midnight comes back looking like it runs from 00:00 to 23:59.
 */
private fun SplitDateTime.isAllDayBound(): Boolean =
    time == null || time == "00:00" || time == "23:59"

@Composable
fun AddEditLocationScreen(
    locationId: String?,
    location: Location?,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<AddEditAdventureViewModel> {
        parametersOf(locationId, location)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle navigation when save is successful
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
            viewModel.clearSavedState()
        }
    }

    // Show error if any
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AddEditLocationContent(
            isEditMode = locationId != null,
            existingLocation = uiState.existingLocation,
            categories = uiState.categories,
            isLoading = uiState.isLoading,
            onNavigateBack = onNavigateBack,
            onSave = { formData ->
                viewModel.saveLocation(formData)
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
            },
            onAddCategory = { name, icon ->
                viewModel.createCategory(name = name, icon = icon)
            }
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
@Composable
fun AddEditLocationContent(
    isEditMode: Boolean = false,
    existingLocation: Location? = null,
    categories: List<Category>,
    isLoading: Boolean = false,
    onNavigateBack: () -> Unit,
    onSave: (adventureData: LocationFormData) -> Unit,
    onGenerateDescription: (name: String, onDescriptionGenerated: (String) -> Unit) -> Unit,
    isGeneratingDescription: Boolean,
    locationSearchResults: List<GeocodeSearchResult> = emptyList(),
    isSearchingLocation: Boolean = false,
    onSearchLocation: (String) -> Unit = {},
    onClearLocationSearch: () -> Unit = {},
    onReverseGeocode: (Double, Double) -> Unit = { _, _ -> },
    reverseGeocodeResult: ReverseGeocodeResult? = null,
    wikipediaImageState: WikipediaImageResult = WikipediaImageResult.Idle,
    onSearchWikipediaImage: (String) -> Unit = {},
    onResetWikipediaState: () -> Unit = {},
    onAddCategory: (name: String, icon: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var formData by remember(existingLocation) {
        mutableStateOf(
            if (existingLocation != null) {
                println("DEBUG: Loading existing location: ${existingLocation.name}")
                println("DEBUG: Number of visits: ${existingLocation.visits.size}")
                
                val parsedVisits = existingLocation.visits.mapIndexed { index, visit ->
                    println("DEBUG: Visit $index - startDate: ${visit.startDate}, endDate: ${visit.endDate}")
                    
                    val startDateTime = splitIsoDateTime(visit.startDate)
                    val endDateTime = splitIsoDateTime(visit.endDate)
                    
                    println("DEBUG: Parsed visit $index - startDate: ${startDateTime.date}, startTime: ${startDateTime.time}")
                    println("DEBUG: Parsed visit $index - endDate: ${endDateTime.date}, endTime: ${endDateTime.time}")
                    
                    // An all-day visit is stored as midnight on both bounds, so a time of
                    // 00:00 means "no time" rather than "one minute past midnight". Reading it
                    // literally left the All day switch off on every visit ever saved.
                    val allDay = startDateTime.isAllDayBound() && endDateTime.isAllDayBound()

                    VisitFormData(
                        id = visit.id,
                        startDate = startDateTime.date,
                        endDate = endDateTime.date,
                        startTime = startDateTime.time.takeUnless { allDay },
                        endTime = endDateTime.time.takeUnless { allDay },
                        timezone = visit.timezone ?: "Europe/Madrid",
                        notes = visit.notes ?: "",
                        isAllDay = allDay
                    )
                }
                
                println("DEBUG: Total parsed visits: ${parsedVisits.size}")
                
                LocationFormData(
                    name = existingLocation.name,
                    description = existingLocation.description ?: "",
                    category = existingLocation.category,
                    rating = existingLocation.rating?.toInt() ?: 0,
                    link = existingLocation.link ?: "",
                    location = existingLocation.location ?: "",
                    latitude = existingLocation.latitude,
                    longitude = existingLocation.longitude,
                    isPublic = existingLocation.isPublic,
                    tags = existingLocation.tags,
                    visits = parsedVisits,
                    images = existingLocation.images.map { contentImage ->
                        ImageFormData(
                            uri = contentImage.image,
                            type = ImageType.URL,
                            isPrimary = contentImage.isPrimary
                        )
                    }
                )
            } else {
                LocationFormData(
                    category = categories.firstOrNull()
                )
            }
        )
    }

    // Update location when reverse geocode completes
    LaunchedEffect(reverseGeocodeResult) {
        reverseGeocodeResult?.displayName?.let { displayName ->
            if (formData.location?.isEmpty() == true) {
                formData = formData.copy(location = displayName)
            }
        }
    }


    if (isLoading && existingLocation == null && isEditMode) {
        // Show loading state while loading adventure for edit
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

        BasicInfoSection(
            formData = formData,
            categories = categories,
            onFormDataChange = { formData = it },
            onNavigateBack = onNavigateBack,
            onGenerateDescription = {
                onGenerateDescription(formData.name) { generatedDescription ->
                    formData = formData.copy(description = generatedDescription)
                }
            },
            isGeneratingDescription = isGeneratingDescription,
            onAddCategory = onAddCategory
        )

        LocationSection(
            formData = formData,
            onFormDataChange = { formData = it },
            locationSearchResults = locationSearchResults,
            isSearchingLocation = isSearchingLocation,
            onSearchLocation = onSearchLocation,
            onClearLocationSearch = onClearLocationSearch,
            onReverseGeocode = onReverseGeocode
        )

        TagsSection(
            formData = formData,
            onFormDataChange = { formData = it }
        )

        ImagesSection(
            images = formData.images,
            onImagesChange = { updatedImages ->
                formData = formData.copy(images = updatedImages)
            },
            wikipediaImageState = wikipediaImageState,
            onSearchWikipediaImage = onSearchWikipediaImage,
            onResetWikipediaState = onResetWikipediaState
        )

        // Visits are saved after the location, against /api/visits/ - they cannot be nested in
        // the location payload because each one needs a location id that does not exist yet.
        DateSection(
            formData = formData,
            onFormDataChange = { formData = it }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrimaryButton(
                onClick = { onSave(formData) },
                text = if (isEditMode) "Update Location" else "Create Location"
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

private val mockUser = UserDetails(
    uuid = "user123",
    username = "previewUser",
    dateJoined = "2025-01-01T00:00:00Z"
)
private val mockCountry = Country(id = 1, name = "Spain", countryCode = "ES", flagUrl = "", numRegions = 1, numVisits = 1, subregion = "Southern Europe", capital = "Madrid", longitude = -3.703790, latitude = 40.416775)
private val mockRegion = Region(id = "region-madrid", name = "Community of Madrid", countryName = "Spain", numCities = 1, longitude = -3.703790, latitude = 40.416775, countryId = 1)
private val mockCity = City(id = "city-madrid", name = "Madrid", regionName = "Community of Madrid", countryName = "Spain", longitude = -3.703790, latitude = 40.416775, regionId = "region-madrid")


@Preview
@Composable
private fun AddEditLocationScreenPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditLocationContent(
                categories = listOf(
                    Category("1", "general", "General", "🌍", "0"),
                    Category("2", "hotel", "Hotel", "🏨", "0"),
                    Category("3", "museum", "Museum", "🏛️", "0")
                ),
                onNavigateBack = {},
                onSave = {},
                onGenerateDescription = { _, _ -> },
                isGeneratingDescription = false,
                onSearchLocation = {},
                onClearLocationSearch = {},
                onReverseGeocode = { _, _ -> }
            )
        }
    }
}

@Preview
@Composable
private fun AddEditLocationScreenWithDataPreview() {
    val sampleLocation = Location(
        id = "1",
        user = mockUser,
        name = "Visit to Prado Museum",
        description = "An incredible experience visiting one of the most important art galleries in the world.",
        category = Category("3", "museum", "Museum", "🏛️", "0"),
        rating = 5.0,
        link = "https://www.museodelprado.es",
        location = "Madrid, Spain",
        latitude = "40.4138",
        longitude = "-3.6921",
        isPublic = true,
        tags = listOf("art", "culture", "madrid"),
        visits = listOf(
            Visit(
                id = "1",
                location = "1",
                startDate = "2024-01-15",
                endDate = "2024-01-15",
                timezone = "Europe/Madrid",
                notes = "Amazing collection of Velázquez paintings",
                createdAt = "2024-01-15T10:00:00Z",
                updatedAt = "2024-01-15T10:00:00Z"
            )
        ),
        createdAt = "2024-01-10T10:00:00Z",
        updatedAt = "2024-01-11T10:00:00Z",
        images = emptyList(),
        collections = emptyList(),
        isVisited = true,
        attachments = emptyList(),
        city = mockCity,
        country = mockCountry,
        region = mockRegion
    )

    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditLocationContent(
                isEditMode = true,
                existingLocation = sampleLocation,
                categories = listOf(
                    Category("1", "general", "General", "🌍", "0"),
                    Category("2", "hotel", "Hotel", "🏨", "0"),
                    Category("3", "museum", "Museum", "🏛️", "0")
                ),
                onNavigateBack = {},
                onSave = {},
                onGenerateDescription = { _, _ -> },
                isGeneratingDescription = false,
                onSearchLocation = {},
                onClearLocationSearch = {},
                onReverseGeocode = { _, _ -> },
                locationSearchResults = listOf(
                    GeocodeSearchResult(
                        latitude = "40.4138",
                        longitude = "-3.6921",
                        name = "Museo del Prado",
                        displayName = "Museo del Prado, Madrid, España",
                        type = "museum",
                        category = "tourism"
                    )
                )
            )
        }
    }
}

@Preview
@Composable
private fun AddEditLocationScreenDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditLocationContent(
                categories = listOf(
                    Category("1", "general", "General", "🌍", "0"),
                    Category("2", "hotel", "Hotel", "🏨", "0"),
                    Category("3", "museum", "Museum", "🏛️", "0")
                ),
                onNavigateBack = {},
                onSave = {},
                onGenerateDescription = { _, _ -> },
                isGeneratingDescription = false,
                onSearchLocation = {},
                onClearLocationSearch = {},
                onReverseGeocode = { _, _ -> }
            )
        }
    }
}