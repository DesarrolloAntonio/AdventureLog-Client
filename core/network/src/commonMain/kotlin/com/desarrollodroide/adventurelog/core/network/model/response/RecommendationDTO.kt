package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Recommendation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecommendationsResponseDTO(
    @SerialName("count")
    val count: Int = 0,

    @SerialName("results")
    val results: List<RecommendationDTO> = emptyList()
)

@Serializable
data class RecommendationDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("distance_km")
    val distanceKm: Double? = null,

    @SerialName("primary_type")
    val primaryType: String? = null,

    /**
     * Nullable, not merely absent. The server sends an explicit `null` for a result with no hours
     * and no types, and a non-null default only covers the missing-key case - which is why this
     * parsed the curl output and threw on the first real search from the app.
     */
    @SerialName("types")
    val types: List<String>? = null,

    @SerialName("website")
    val website: String? = null,

    @SerialName("phone_number")
    val phoneNumber: String? = null,

    @SerialName("opening_hours")
    val openingHours: List<String>? = null
)

/**
 * Null for a result with no name or no coordinates.
 *
 * OpenStreetMap has plenty of both - an unnamed bench, a way with no centroid - and neither can be
 * shown on a list or put on a map, so they are dropped here rather than drawn as a blank row.
 */
fun RecommendationDTO.toDomainModel(): Recommendation? {
    val name = name?.takeIf { it.isNotBlank() } ?: return null
    val lat = latitude ?: return null
    val lon = longitude ?: return null
    return Recommendation(
        id = id,
        name = name,
        description = description?.takeIf { it.isNotBlank() },
        latitude = lat,
        longitude = lon,
        address = address?.takeIf { it.isNotBlank() },
        distanceKm = distanceKm,
        primaryType = primaryType?.takeIf { it.isNotBlank() },
        types = types.orEmpty(),
        website = website?.takeIf { it.isNotBlank() },
        phoneNumber = phoneNumber?.takeIf { it.isNotBlank() },
        openingHours = openingHours.orEmpty()
    )
}
