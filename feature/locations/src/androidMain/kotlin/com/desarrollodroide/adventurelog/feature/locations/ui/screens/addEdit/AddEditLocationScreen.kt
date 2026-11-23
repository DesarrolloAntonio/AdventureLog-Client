package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit

import com.desarrollodroide.adventurelog.feature.ui.components.DiscardChangesDialog
import com.desarrollodroide.adventurelog.feature.ui.components.FormActionBar
import com.desarrollodroide.adventurelog.feature.ui.components.HideShellChrome
import com.desarrollodroide.adventurelog.feature.ui.components.FormHeader
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.datetime.TimeZone
import com.desarrollodroide.adventurelog.core.model.isAllDayVisit
import com.desarrollodroide.adventurelog.core.model.ContentImage
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import kotlin.math.roundToInt
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
import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.BasicInfoSection
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.DateSection
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.TrailsSection
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
import co.touchlab.kermit.Logger
import androidx.compose.material3.AlertDialog
import com.desarrollodroide.adventurelog.feature.ui.platform.PlatformBackHandler
import com.desarrollodroide.adventurelog.feature.ui.components.ContentColumn

private val logger = Logger.withTag("AddEditLocationScreen")

@Composable
fun AddEditLocationScreen(
    locationId: String?,
    location: Location?,
    onNavigateBack: () -> Unit,
    /** Open on the images section, scrolled to it: the form was reached by "Add photo". */
    openImages: Boolean = false,
    /** A new place started from a trip goes into that trip. */
    intoCollectionId: String? = null
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
            openImages = openImages,
            isEditMode = locationId != null,
            existingLocation = uiState.existingLocation,
            categories = uiState.categories,
            isLoading = uiState.isLoading,
            isSaving = uiState.isSavingLocation,
            loadError = uiState.loadError,
            onRetryLoad = viewModel::retryLoad,
            onNavigateBack = onNavigateBack,
            onSave = { formData ->
                viewModel.saveLocation(formData, intoCollectionId)
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
            },
            defaultCurrency = viewModel.defaultCurrency
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
@Composable
fun AddEditLocationContent(
    openImages: Boolean = false,
    isEditMode: Boolean = false,
    existingLocation: Location? = null,
    categories: List<Category>,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    loadError: String? = null,
    onRetryLoad: () -> Unit = {},
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
    defaultCurrency: String = Currencies.DEFAULT,
    modifier: Modifier = Modifier
) {
    if (isEditMode && existingLocation == null) {
        // Editing needs the place as the server has it. Until it arrives there is no form: an
        // empty one could be saved over the place.
        Box(
            modifier = modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (loadError == null) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = loadError,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onNavigateBack) { Text("Go back") }
                        Button(onClick = onRetryLoad) { Text("Try again") }
                    }
                }
            }
        }
        return
    }

    // Saveable, so a form half filled in survives the process being killed in the background: it
    // came back empty (measured). Keyed by the place, so a different place starts from its own data.
    var formData by rememberSaveable(existingLocation?.id, stateSaver = LocationFormSaver) {
        mutableStateOf(locationFormOf(existingLocation, defaultCurrency))
    }

    // Update location when reverse geocode completes
    LaunchedEffect(reverseGeocodeResult) {
        reverseGeocodeResult?.displayName?.let { displayName ->
            if (formData.location?.isEmpty() == true) {
                formData = formData.copy(location = displayName)
            }
        }
    }



    // A filled-in form is worth something. Back used to throw it away without a word, which is
    // the one thing a form must never do.
    val initialFormData = remember(existingLocation) { locationFormOf(existingLocation, defaultCurrency) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val hasChanges = formData != initialFormData

    fun leave() {
        if (hasChanges) confirmDiscard = true else onNavigateBack()
    }

    PlatformBackHandler(enabled = hasChanges) { confirmDiscard = true }
    // Full screen on a phone, as the canvas draws its forms: Close at the top, no app bars.
    HideShellChrome()

    if (confirmDiscard) {
        // The same question every form asks, in the same Expressive dialog.
        DiscardChangesDialog(
            thing = "place",
            isEditing = isEditMode,
            onDiscard = {
                confirmDiscard = false
                onNavigateBack()
            },
            onKeepEditing = { confirmDiscard = false },
        )
    }

    // A form is the clearest case for the content column: a text field drawn 1200dp wide is a
    // box the length of the screen holding a place name, and the eye loses the line it is on.
    val scrollState = rememberScrollState()
    // Where the images section starts in the scrolled column, once it has been placed.
    var imagesTop by remember { mutableStateOf<Int?>(null) }
    // Once, on arrival: after that the scroll is the user's.
    var scrolledToImages by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(imagesTop) {
        val top = imagesTop
        if (openImages && !scrolledToImages && top != null) {
            scrollState.animateScrollTo(top)
            scrolledToImages = true
        }
    }

    ContentColumn(modifier) {
    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f).fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            FormHeader(
                title = if (isEditMode) "Edit place" else "New place",
                onClose = { leave() },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            BasicInfoSection(
                formData = formData,
                categories = categories,
                onFormDataChange = { formData = it },
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

            TrailsSection(
                formData = formData,
                onFormDataChange = { formData = it }
            )

            Box(Modifier.onPlaced { imagesTop = it.positionInParent().y.roundToInt() }) {
                ImagesSection(
                    images = formData.images,
                    onImagesChange = { updatedImages ->
                        formData = formData.copy(images = updatedImages)
                    },
                    wikipediaImageState = wikipediaImageState,
                    onSearchWikipediaImage = onSearchWikipediaImage,
                    onResetWikipediaState = onResetWikipediaState,
                    initiallyExpanded = openImages
                )
            }

            // Visits are saved after the location, against /api/visits/ - they cannot be nested in
            // the location payload because each one needs a location id that does not exist yet.
            DateSection(
                formData = formData,
                onFormDataChange = { formData = it }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
        // Save and Cancel pinned under the form, as the canvas draws it.
        FormActionBar(
            onClick = { onSave(formData) },
            text = when {
                isSaving -> "Saving…"
                isEditMode -> "Save changes"
                else -> "Create place"
            },
            // Held while a save runs: nothing showed one was under way, and a second tap made a
            // second place.
            enabled = !isSaving,
            onCancel = { leave() }
        )
    }
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