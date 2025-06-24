package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAdventureRequest(
    val name: String,
    val description: String? = null,
    val rating: Double? = null,
    @SerialName("activity_types")
    val activityTypes: List<String>? = null,
    val location: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean = false,
    val collections: List<String>? = null,
    val link: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val visits: List<VisitRequest>? = null,
    val category: CategoryRequest? = null
)

@Serializable
data class UpdateAdventureRequest(
    val name: String? = null,
    val description: String? = null,
    val rating: Double? = null,
    val location: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val visits: List<VisitRequest>? = null,
    val category: CategoryRequest? = null,
    val notes: String? = null,
    val link: String? = null
)

@Serializable
data class VisitRequest(
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null,
    val timezone: String? = null,
    val notes: String? = null
)

@Serializable
data class CategoryRequest(
    val id: String? = null,
    val name: String? = null,
    @SerialName("display_name")
    val displayName: String? = null,
    val icon: String? = null
)

@Serializable
data class CityRequest(
    val id: String? = null,
    @SerialName("region_name")
    val regionName: String? = null,
    @SerialName("country_name")
    val countryName: String? = null,
    val name: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val region: String? = null
)

@Serializable
data class CountryRequest(
    val id: Int? = null,
    @SerialName("flag_url")
    val flagUrl: String? = null,
    @SerialName("num_regions")
    val numRegions: String? = null,
    @SerialName("num_visits")
    val numVisits: String? = null,
    val name: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null,
    val subregion: String? = null,
    val capital: String? = null,
    val longitude: String? = null,
    val latitude: String? = null
)

@Serializable
data class RegionRequest(
    val id: String? = null,
    @SerialName("num_cities")
    val numCities: String? = null,
    @SerialName("country_name")
    val countryName: String? = null,
    val name: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val country: Int? = null
)
