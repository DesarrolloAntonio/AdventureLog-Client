package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Transportation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransportationDTO(
    @SerialName("id")
    val id: String,

    @SerialName("user")
    val user: String,

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
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

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
    val endTimezone: String? = null,

    // The server computes this with geodesic() and returns a float (km), not a string.
    @SerialName("distance")
    val distance: Double? = null,

    @SerialName("images")
    val images: List<ContentImageDTO>? = null,

    @SerialName("attachments")
    val attachments: List<AttachmentDTO>? = null
)

fun TransportationDTO.toDomainModel(): Transportation = Transportation(
    id = id,
    user = user,
    type = type,
    name = name,
    description = description,
    rating = rating,
    link = link,
    date = date,
    flightNumber = flightNumber,
    fromLocation = fromLocation,
    toLocation = toLocation,
    isPublic = isPublic,
    collection = collection,
    createdAt = createdAt,
    updatedAt = updatedAt,
    endDate = endDate,
    originLatitude = originLatitude,
    originLongitude = originLongitude,
    destinationLatitude = destinationLatitude,
    destinationLongitude = destinationLongitude,
    startTimezone = startTimezone,
    endTimezone = endTimezone,
    distance = distance?.toString(),
    images = images?.map { it.toDomainModel() },
    attachments = attachments?.map { it.toDomainModel() }
)
