package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: String,
    val name: String,
    val regionName: String,
    val countryName: String,
    val longitude: Double?,
    val latitude: Double?,
    val regionId: String
)