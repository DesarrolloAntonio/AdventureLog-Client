package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Body for POST/PATCH /api/transportations/.
 *
 * Optional fields are nullable and omitted when null - the server rejects empty strings for
 * `date`, `end_date` and the timezone choices, and treats a missing key as "leave unset".
 */
@Serializable
data class TransportationRequest(
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

    @SerialName("end_date")
    val endDate: String? = null,

    @SerialName("start_timezone")
    val startTimezone: String? = null,

    @SerialName("end_timezone")
    val endTimezone: String? = null,

    @SerialName("flight_number")
    val flightNumber: String? = null,

    @SerialName("from_location")
    val fromLocation: String? = null,

    @SerialName("to_location")
    val toLocation: String? = null,

    @SerialName("origin_latitude")
    val originLatitude: Double? = null,

    @SerialName("origin_longitude")
    val originLongitude: Double? = null,

    @SerialName("destination_latitude")
    val destinationLatitude: Double? = null,

    @SerialName("destination_longitude")
    val destinationLongitude: Double? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collection")
    val collection: String? = null
)
