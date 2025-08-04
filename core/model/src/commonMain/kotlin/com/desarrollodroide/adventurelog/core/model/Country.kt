package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Country(
    val id: Int,
    val name: String,
    val countryCode: String,
    val flagUrl: String,
    val numRegions: Int,
    val numVisits: Int,
    val subregion: String?,
    val capital: String?,
    val longitude: Double?,
    val latitude: Double?
)