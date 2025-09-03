package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Activity(
    val id: String,
    val user: Int,
    val visit: String,
    val trail: String? = null,
    val gpxFile: String? = null,
    val name: String,
    val sportType: String = "General",
    val distance: Double? = null,
    val movingTime: String? = null,
    val elapsedTime: String? = null,
    val restTime: String? = null,
    val elevationGain: Double? = null,
    val elevationLoss: Double? = null,
    val elevHigh: Double? = null,
    val elevLow: Double? = null,
    val startDate: String? = null,
    val startDateLocal: String? = null,
    val timezone: String? = null,
    val averageSpeed: Double? = null,
    val maxSpeed: Double? = null,
    val averageCadence: Double? = null,
    val calories: Double? = null,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val endLat: Double? = null,
    val endLng: Double? = null,
    val externalServiceId: String? = null,
    val geojson: String? = null
)
