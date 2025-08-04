package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Region
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegionDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("country_name")
    val countryName: String,
    
    @SerialName("num_cities")
    val numCities: String, // API returns as string
    
    @SerialName("longitude")
    val longitude: String? = null,
    
    @SerialName("latitude")
    val latitude: String? = null,
    
    @SerialName("country")
    val country: Int
)

fun RegionDTO.toDomainModel(): Region {
    return Region(
        id = id,
        name = name,
        countryName = countryName,
        numCities = numCities.toIntOrNull() ?: 0,
        longitude = longitude?.toDoubleOrNull(),
        latitude = latitude?.toDoubleOrNull(),
        countryId = country
    )
}