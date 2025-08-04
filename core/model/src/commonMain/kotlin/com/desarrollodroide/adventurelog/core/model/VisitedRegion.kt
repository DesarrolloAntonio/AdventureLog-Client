package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class VisitedRegion(
    val id: Int,
    val userId: Int,
    val regionId: String,
    val name: String,
    val longitude: Double?,
    val latitude: Double?
)