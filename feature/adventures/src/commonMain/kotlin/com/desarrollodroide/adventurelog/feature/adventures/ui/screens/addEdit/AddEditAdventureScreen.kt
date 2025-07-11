package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.GeocodeSearchResult
import com.desarrollodroide.adventurelog.core.model.ReverseGeocodeResult
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.BasicInfoSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.DateSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.ImagesSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.LocationSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.TagsSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import com.desarrollodroide.adventurelog.feature.ui.components.PrimaryButton
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.WikipediaImageState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AddEditAdventureScreen(
    categories: List<Category>,
    onNavigateBack: () -> Unit,
    onSave: (adventureData: AdventureFormData) -> Unit,
    onGenerateDescription: (name: String, onDescriptionGenerated: (String) -> Unit) -> Unit,
    isGeneratingDescription: Boolean,
    locationSearchResults: List<GeocodeSearchResult> = emptyList(),
    isSearchingLocation: Boolean = false,
    onSearchLocation: (String) -> Unit = {},
    onClearLocationSearch: () -> Unit = {},
    onReverseGeocode: (Double, Double) -> Unit = { _, _ -> },
    reverseGeocodeResult: ReverseGeocodeResult? = null,
    wikipediaImageState: WikipediaImageState = WikipediaImageState.Idle,
    onSearchWikipediaImage: (String) -> Unit = {},
    onResetWikipediaState: () -> Unit = {},
    modifier: Modifier = Modifier,
    initialData: AdventureFormData? = null
) {
    var formData by remember {
        mutableStateOf(
            initialData ?: AdventureFormData(
                category = categories.firstOrNull()
            )
        )
    }
    
    // Update location when reverse geocode completes
    reverseGeocodeResult?.displayName?.let { displayName ->
        if (formData.location.isEmpty()) {
            formData = formData.copy(location = displayName)
        }
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
                isGeneratingDescription = isGeneratingDescription
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
                formData = formData,
                onFormDataChange = { formData = it },
                wikipediaImageState = wikipediaImageState,
                onSearchWikipediaImage = onSearchWikipediaImage,
                onResetWikipediaState = onResetWikipediaState
            )

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
                    text = if (initialData != null) "Update Adventure" else "Create Adventure"
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

@Preview
@Composable
private fun AddEditAdventureScreenPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
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
private fun AddEditAdventureScreenWithDataPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
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
                ),
                initialData = AdventureFormData(
                    name = "Visit to Prado Museum",
                    description = "An incredible experience visiting one of the most important art galleries in the world.",
                    category = Category("3", "museum", "Museum", "🏛️", "0"),
                    rating = 5,
                    link = "https://www.museodelprado.es",
                    location = "Madrid, Spain",
                    latitude = "40.4138",
                    longitude = "-3.6921",
                    isPublic = true,
                    tags = listOf("art", "culture", "madrid"),
                    visits = listOf(
                        VisitFormData(
                            startDate = "2024-01-15",
                            endDate = "2024-01-15",
                            timezone = "Europe/Madrid",
                            notes = "Amazing collection of Velázquez paintings",
                            isAllDay = true
                        )
                    )
                )
            )
        }
    }
}

@Preview
@Composable
private fun AddEditAdventureScreenDarkPreview() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            AddEditAdventureScreen(
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
