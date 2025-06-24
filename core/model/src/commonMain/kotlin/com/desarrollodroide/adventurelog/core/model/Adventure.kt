package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Adventure(
    val id: String,
    val userId: Int,
    val name: String,
    val description: String,
    val rating: Double,
    val activityTypes: List<String>,
    val location: String,
    val isPublic: Boolean,
    val collections: List<String>,
    val createdAt: String,
    val updatedAt: String,
    val images: List<AdventureImage>,
    val link: String,
    val longitude: String,
    val latitude: String,
    val visits: List<Visit>,
    val isVisited: Boolean,
    val category: Category?,
    val attachments: List<Attachment>
)