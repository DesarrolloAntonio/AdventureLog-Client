package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStatsDTO(
    @SerialName("adventure_count")
    val adventureCount: Int? = null,
    
    @SerialName("trips_count")
    val tripsCount: Int? = null,
    
    @SerialName("visited_city_count")
    val visitedCityCount: Int? = null,
    
    @SerialName("total_cities")
    val totalCities: Int? = null,
    
    @SerialName("visited_region_count")
    val visitedRegionCount: Int? = null,
    
    @SerialName("total_regions")
    val totalRegions: Int? = null,
    
    @SerialName("visited_country_count")
    val visitedCountryCount: Int? = null,
    
    @SerialName("total_countries")
    val totalCountries: Int? = null
)
