package com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data

data class CollectionFormData(
    val name: String = "",
    val description: String = "",
    val isPublic: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val link: String = ""
)