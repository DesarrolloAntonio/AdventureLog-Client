package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: String,
    val name: String,
    val description: String? = null,
    val rating: Double? = null,
    val tags: List<String> = emptyList(),
    val location: String? = null,
    val isPublic: Boolean = false,
    val collections: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String,
    val images: List<ContentImage> = emptyList(),
    val link: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val visits: List<Visit> = emptyList(),
    val isVisited: Boolean = false,
    val category: Category? = null,
    val attachments: List<Attachment> = emptyList(),
    val user: UserDetails,
    val city: City? = null,
    val country: Country? = null,
    val region: Region? = null,
    val trails: List<Trail> = emptyList()
)
