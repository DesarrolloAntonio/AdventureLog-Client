package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData

data class LocationFormData(
    val name: String = "",
    val description: String = "",
    val category: Category? = null,
    val rating: Int = 0,
    val link: String = "",
    val location: String = "",
    val latitude: String? = null,
    val longitude: String? = null,
    val isPublic: Boolean = false,
    val tags: List<String> = emptyList(),
    val visits: List<VisitFormData> = emptyList(),
    val images: List<ImageFormData> = emptyList()
)
