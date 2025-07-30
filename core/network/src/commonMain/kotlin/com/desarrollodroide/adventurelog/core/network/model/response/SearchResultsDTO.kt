package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultsDTO(
    @SerialName("adventures")
    val adventures: List<AdventureDTO>? = null,
    
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
)

@Serializable
data class CountryDTO(
    @SerialName("id")
    val id: Int,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("country_code")
    val countryCode: String
)

@Serializable
data class RegionDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String
)

@Serializable
data class CityDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String
)

@Serializable
data class VisitedRegionDTO(
    @SerialName("id")
    val id: Int,
    
    @SerialName("region")
    val region: String,
    
    @SerialName("name")
    val name: String
)

@Serializable
data class VisitedCityDTO(
    @SerialName("id")
    val id: Int,
    
    @SerialName("city")
    val city: String,
    
    @SerialName("name")
    val name: String
)
