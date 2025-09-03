package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Visit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisitDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("location")
    val location: String,
    
    @SerialName("start_date")
    val startDate: String? = null,
    
    @SerialName("end_date")
    val endDate: String? = null,
    
    @SerialName("timezone")
    val timezone: String? = null,
    
    @SerialName("notes")
    val notes: String? = null,
    
    @SerialName("activities")
    val activities: List<ActivityDTO> = emptyList(),
    
    @SerialName("created_at")
    val createdAt: String,
    
    @SerialName("updated_at")
    val updatedAt: String
)

fun VisitDTO.toDomainModel(): Visit = Visit(
    id = id,
    location = location,
    startDate = startDate,
    endDate = endDate,
    timezone = timezone,
    notes = notes,
    activities = activities.map { it.toDomainModel() },
    createdAt = createdAt,
    updatedAt = updatedAt
)
