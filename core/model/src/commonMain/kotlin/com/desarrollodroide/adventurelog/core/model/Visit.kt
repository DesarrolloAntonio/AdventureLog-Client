package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Visit(
    val id: String,
    val location: String,  // UUID de location
    val startDate: String? = null,
    val endDate: String? = null,
    val timezone: String? = null,
    val notes: String? = null,
    val activities: List<Activity> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)
