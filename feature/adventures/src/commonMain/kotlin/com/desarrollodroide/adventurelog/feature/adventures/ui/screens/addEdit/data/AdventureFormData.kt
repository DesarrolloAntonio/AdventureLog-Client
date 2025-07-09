package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.VisitFormData

data class AdventureFormData(
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

data class ImageFormData(
    val uri: String,
    val type: ImageType,
    val isPrimary: Boolean = false
)

enum class ImageType {
    LOCAL_FILE,
    URL,
    WIKIPEDIA
}
