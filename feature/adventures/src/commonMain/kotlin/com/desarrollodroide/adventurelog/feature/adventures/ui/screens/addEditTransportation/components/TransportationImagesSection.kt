package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.components.ImagesSection
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data.AdventureFormData
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.adventures.viewmodel.WikipediaImageState

@Composable
fun TransportationImagesSection(
    formData: TransportationFormData,
    onFormDataChange: (TransportationFormData) -> Unit,
    wikipediaImageState: WikipediaImageState,
    onSearchWikipediaImage: (String) -> Unit,
    onResetWikipediaState: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert TransportationFormData to AdventureFormData for compatibility
    val adventureFormData = AdventureFormData(
        name = formData.name,
        description = formData.description,
        rating = formData.rating,
        link = formData.link,
        isPublic = formData.isPublic,
        images = formData.images
    )

    ImagesSection(
        formData = adventureFormData,
        onFormDataChange = { updatedData ->
            // Update only the images in TransportationFormData
            onFormDataChange(formData.copy(images = updatedData.images))
        },
        wikipediaImageState = wikipediaImageState,
        onSearchWikipediaImage = onSearchWikipediaImage,
        onResetWikipediaState = onResetWikipediaState
    )
}
