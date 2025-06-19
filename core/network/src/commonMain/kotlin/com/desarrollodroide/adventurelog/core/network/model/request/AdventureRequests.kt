package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateAdventureRequest(
    val name: String,
    val description: String,
    val rating: Double,
    val location: String,
    @SerialName("is_public")
    val isPublic: Boolean,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val visits: List<VisitRequest>? = null,
    val category: CategoryRequest,
    val notes: String? = null,
    val link: String? = null
)

@Serializable
data class UpdateAdventureRequest(
    val name: String? = null,
    val description: String? = null,
    val rating: Double? = null,
    val location: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val visits: List<VisitRequest>? = null,
    val category: CategoryRequest? = null,
    val notes: String? = null,
    val link: String? = null
)

@Serializable
data class VisitRequest(
    @SerialName("start_date")
    val startDate: String,
    @SerialName("end_date")
    val endDate: String,
    val timezone: String,
    val notes: String
)

@Serializable
data class CategoryRequest(
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val icon: String
)
