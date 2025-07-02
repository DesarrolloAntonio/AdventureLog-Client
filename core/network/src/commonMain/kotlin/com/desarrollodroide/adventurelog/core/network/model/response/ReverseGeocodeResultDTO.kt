package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReverseGeocodeResultDTO(
    @SerialName("region_id")
    val regionId: String? = null,
    @SerialName("region")
    val region: String? = null,
    @SerialName("country")
    val country: String? = null,
    @SerialName("country_id")
    val countryId: String? = null,
    @SerialName("region_visited")
    val regionVisited: Boolean = false,
    @SerialName("display_name")
    val displayName: String? = null,
    @SerialName("city")
    val city: String? = null,
    @SerialName("city_id")
    val cityId: String? = null,
    @SerialName("city_visited")
    val cityVisited: Boolean = false,
    @SerialName("location_name")
    val locationName: String? = null,
    @SerialName("error")
    val error: String? = null
)
