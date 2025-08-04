package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisitedRegionDTO(
    @SerialName("id")
    val id: Int,
    
    @SerialName("user_id")
    val userId: Int,
    
    @SerialName("region")
    val region: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("longitude")
    val longitude: String? = null,
    
    @SerialName("latitude")
    val latitude: String? = null
)

fun VisitedRegionDTO.toDomainModel(): VisitedRegion {
    return VisitedRegion(
        id = id,
        userId = userId,
        regionId = region,
        name = name,
        longitude = longitude?.toDoubleOrNull(),
        latitude = latitude?.toDoubleOrNull()
    )
}