package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.City
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CityDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("region_name")
    val regionName: String,
    
    @SerialName("country_name")
    val countryName: String,
    
    @SerialName("longitude")
    val longitude: String? = null,
    
    @SerialName("latitude")
    val latitude: String? = null,
    
    @SerialName("region")
    val region: String
)

fun CityDTO.toDomainModel(): City {
    return City(
        id = id,
        name = name,
        regionName = regionName,
        countryName = countryName,
        longitude = longitude?.toDoubleOrNull(),
        latitude = latitude?.toDoubleOrNull(),
        regionId = region
    )
}