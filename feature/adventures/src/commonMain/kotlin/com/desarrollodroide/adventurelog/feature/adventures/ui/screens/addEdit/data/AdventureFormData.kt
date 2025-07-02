package com.desarrollodroide.adventurelog.feature.adventures.ui.screens.addEdit.data

import com.desarrollodroide.adventurelog.core.model.Category

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
    val date: String? = null
)
