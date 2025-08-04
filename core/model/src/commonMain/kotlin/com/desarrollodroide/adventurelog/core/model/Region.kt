package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Region(
    val id: String,
    val name: String,
    val countryName: String,
    val numCities: Int,
    val longitude: Double?,
    val latitude: Double?,
    val countryId: Int
)