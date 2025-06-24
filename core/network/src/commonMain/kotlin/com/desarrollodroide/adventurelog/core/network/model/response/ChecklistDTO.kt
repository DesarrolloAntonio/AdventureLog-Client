package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChecklistDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: Int? = null,

    @SerialName("name")
    val name: String,

    @SerialName("date")
    val date: String? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("items")
    val items: List<ChecklistItemDTO>
)

@Serializable
data class ChecklistItemDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: Int? = null,

    @SerialName("name")
    val name: String,

    @SerialName("is_checked")
    val isChecked: Boolean = false,

    @SerialName("checklist")
    val checklist: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)
