package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Lodging(
    val id: String,
    val user: String,
    val name: String,
    val type: String = "other",
    val description: String? = null,
    val rating: Double? = null,
    val link: String? = null,
    val checkIn: String? = null,
    val checkOut: String? = null,
    val reservationNumber: String? = null,
    val price: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val location: String? = null,
    val isPublic: Boolean = false,
    val collection: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val timezone: String? = null,
    val images: List<ContentImage>? = null,
    val attachments: List<Attachment>? = null
)
