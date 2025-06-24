package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransportationDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("type")
    val type: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("date")
    val date: String? = null,

    @SerialName("flight_number")
    val flightNumber: String? = null,

    @SerialName("from_location")
    val fromLocation: String? = null,

    @SerialName("to_location")
    val toLocation: String? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    @SerialName("origin_latitude")
    val originLatitude: String? = null,

    @SerialName("origin_longitude")
    val originLongitude: String? = null,

    @SerialName("destination_latitude")
    val destinationLatitude: String? = null,

    @SerialName("destination_longitude")
    val destinationLongitude: String? = null,

    @SerialName("start_timezone")
    val startTimezone: String? = null,

    @SerialName("end_timezone")
    val endTimezone: String? = null
)
