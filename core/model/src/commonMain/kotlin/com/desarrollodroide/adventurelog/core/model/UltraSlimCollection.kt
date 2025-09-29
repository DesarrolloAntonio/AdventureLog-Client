package com.desarrollodroide.adventurelog.core.model

data class UltraSlimCollection(
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val startDate: String?,
    val endDate: String?,
    val adventureCount: Int,
    val featuredImage: String?,
    val link: String?
)
