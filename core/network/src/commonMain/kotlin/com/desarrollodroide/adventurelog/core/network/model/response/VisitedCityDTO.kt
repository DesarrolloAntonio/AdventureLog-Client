package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.VisitedCity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisitedCityDTO(
    @SerialName("id")
    val id: Int,
    
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("city")
    val city: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("longitude")
    val longitude: String? = null,
    
    @SerialName("latitude")
    val latitude: String? = null
)

fun VisitedCityDTO.toDomainModel(): VisitedCity {
    return VisitedCity(
        id = id,
        userId = userId,
        cityId = city,
        name = name,
        longitude = longitude?.toDoubleOrNull(),
        latitude = latitude?.toDoubleOrNull()
    )
}