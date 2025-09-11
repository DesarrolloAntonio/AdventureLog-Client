package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val user: String,
    val name: String,
    val content: String? = null,
    val date: String? = null,
    val links: List<String>? = null,
    val isPublic: Boolean = false,
    val collection: String? = null,
    val createdAt: String,
    val updatedAt: String
)
