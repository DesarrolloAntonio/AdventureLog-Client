package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Country
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CountryDTO(
    @SerialName("id")
    val id: Int,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("country_code")
    val countryCode: String,
    
    @SerialName("flag_url")
    val flagUrl: String,
    
    @SerialName("num_regions")
    val numRegions: String, // API returns as string
    
    @SerialName("num_visits")
    val numVisits: String, // API returns as string
    
    @SerialName("subregion")
    val subregion: String? = null,
    
    @SerialName("capital")
    val capital: String? = null,
    
    @SerialName("longitude")
    val longitude: String? = null,
    
    @SerialName("latitude")
    val latitude: String? = null
)

fun CountryDTO.toDomainModel(): Country {
    return Country(
        id = id,
        name = name,
        countryCode = countryCode,
        flagUrl = flagUrl,
        numRegions = numRegions.toIntOrNull() ?: 0,
        numVisits = numVisits.toIntOrNull() ?: 0,
        subregion = subregion,
        capital = capital,
        longitude = longitude?.toDoubleOrNull(),
        latitude = latitude?.toDoubleOrNull()
    )
}