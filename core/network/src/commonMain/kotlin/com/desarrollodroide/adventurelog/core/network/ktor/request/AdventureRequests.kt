package com.desarrollodroide.adventurelog.core.network.ktor.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateAdventureRequest(
    val name: String,
    val description: String,
    val rating: Double,
    val location: String,
    val is_public: Boolean,
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
    val is_public: Boolean? = null,
    val longitude: Double? = null,
    val latitude: Double? = null,
    val visits: List<VisitRequest>? = null,
    val category: CategoryRequest? = null,
    val notes: String? = null,
    val link: String? = null
)

@Serializable
data class VisitRequest(
    val start_date: String,
    val end_date: String,
    val timezone: String,
    val notes: String
)

@Serializable
data class CategoryRequest(
    val name: String,
    val display_name: String,
    val icon: String
)
