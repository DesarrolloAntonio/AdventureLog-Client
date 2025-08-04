package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class VisitedCity(
    val id: Int,
    val userId: Int,
    val cityId: String,
    val name: String,
    val longitude: Double?,
    val latitude: Double?
)