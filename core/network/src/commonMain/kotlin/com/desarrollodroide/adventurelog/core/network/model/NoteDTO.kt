package com.desarrollodroide.adventurelog.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("content")
    val content: String? = null,

    @SerialName("date")
    val date: String? = null,

    @SerialName("links")
    val links: List<String>? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)
