package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LodgingDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("check_in")
    val checkIn: String? = null,

    @SerialName("check_out")
    val checkOut: String? = null,

    @SerialName("reservation_number")
    val reservationNumber: String? = null,

    @SerialName("price")
    val price: String? = null,

    @SerialName("latitude")
    val latitude: String? = null,

    @SerialName("longitude")
    val longitude: String? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("timezone")
    val timezone: String? = null
)
