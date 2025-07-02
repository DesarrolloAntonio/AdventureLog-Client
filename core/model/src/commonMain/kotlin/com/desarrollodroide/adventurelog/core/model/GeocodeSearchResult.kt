package com.desarrollodroide.adventurelog.core.model

data class GeocodeSearchResult(
    val latitude: String,
    val longitude: String,
    val name: String,
    val displayName: String,
    val type: String? = null,
    val category: String? = null,
    val importance: Double? = null,
    val addressType: String? = null,
    val poweredBy: String? = null
)
