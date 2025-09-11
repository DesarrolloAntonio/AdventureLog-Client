package com.desarrollodroide.adventurelog.core.model

import com.desarrollodroide.adventurelog.core.model.ContentImage
import kotlinx.serialization.Serializable

@Serializable
data class Transportation(
    val id: String,
    val user: String,
    val type: String,
    val name: String,
    val description: String? = null,
    val rating: Double? = null,
    val link: String? = null,
    val date: String? = null,
    val flightNumber: String? = null,
    val fromLocation: String? = null,
    val toLocation: String? = null,
    val isPublic: Boolean = false,
    val collection: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val endDate: String? = null,
    val originLatitude: String? = null,
    val originLongitude: String? = null,
    val destinationLatitude: String? = null,
    val destinationLongitude: String? = null,
    val startTimezone: String? = null,
    val endTimezone: String? = null,
    val distance: String? = null,
    val images: List<ContentImage>? = null,
    val attachments: List<Attachment>? = null
)
