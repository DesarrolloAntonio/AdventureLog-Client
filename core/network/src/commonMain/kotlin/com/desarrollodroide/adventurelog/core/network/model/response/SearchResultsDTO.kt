package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultsDTO(
    @SerialName("locations")
    val locations: List<AdventureDTO>? = null,
    
    @SerialName("collections")
    val collections: List<CollectionDTO>? = null,
    
    @SerialName("users")
    val users: List<UserDetailsDTO>? = null,
    
    @SerialName("countries")
    val countries: List<CountryDTO>? = null,
    
    @SerialName("regions")
    val regions: List<RegionDTO>? = null,
    
    @SerialName("cities")
    val cities: List<CityDTO>? = null,
    
    @SerialName("visited_regions")
    val visitedRegions: List<VisitedRegionDTO>? = null,
    
    @SerialName("visited_cities")
    val visitedCities: List<VisitedCityDTO>? = null
) {
    fun getLocationsList(): List<AdventureDTO> {
        return locations ?: emptyList()
    }
}
