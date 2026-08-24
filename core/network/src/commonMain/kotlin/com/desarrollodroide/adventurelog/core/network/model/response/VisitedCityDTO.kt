package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.VisitedCity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /api/visitedcity/`.
 *
 * Mirrors the server exactly. An earlier version required a `user_id` field the server does not
 * send - it sends `user` - so the whole list failed to decode and nothing could ever show a
 * visited city. The coordinates arrive as JSON numbers, not strings.
 */
@Serializable
data class VisitedCityDTO(
    @SerialName("id")
    val id: Int,

    @SerialName("user")
    val user: String = "",

    @SerialName("city")
    val city: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("latitude")
    val latitude: Double? = null
)

fun VisitedCityDTO.toDomainModel(): VisitedCity = VisitedCity(
    id = id,
    userId = user,
    cityId = city,
    name = name,
    longitude = longitude,
    latitude = latitude
)
