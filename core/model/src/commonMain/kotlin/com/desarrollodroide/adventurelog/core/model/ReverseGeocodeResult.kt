package com.desarrollodroide.adventurelog.core.model

data class ReverseGeocodeResult(
    val regionId: String? = null,
    val region: String? = null,
    val country: String? = null,
    val countryId: String? = null,
    val regionVisited: Boolean = false,
    val displayName: String? = null,
    val city: String? = null,
    val cityId: String? = null,
    val cityVisited: Boolean = false,
    val locationName: String? = null,
    val error: String? = null
)
