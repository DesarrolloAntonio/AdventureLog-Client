package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData

data class LocationFormData(
    val name: String = "",
    val description: String = "",
    val category: Category? = null,
    val rating: Int = 0,
    /** Kept as typed text so a half-entered amount is not silently rounded or dropped. */
    val price: String = "",
    val priceCurrency: String = Currencies.DEFAULT,
    val link: String = "",
    val location: String = "",
    val latitude: String? = null,
    val longitude: String? = null,
    val isPublic: Boolean = false,
    val tags: List<String> = emptyList(),
    val visits: List<VisitFormData> = emptyList(),
    val trails: List<TrailFormData> = emptyList(),
    val images: List<ImageFormData> = emptyList()
)
