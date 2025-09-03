package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.ChecklistItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChecklistDTO(
    @SerialName("id")
    val id: String,

    @SerialName("user")
    val user: Int,

    @SerialName("name")
    val name: String,

    @SerialName("date")
    val date: String? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("items")
    val items: List<ChecklistItemDTO>
)

@Serializable
data class ChecklistItemDTO(
    @SerialName("id")
    val id: String,

    @SerialName("user")
    val user: Int,

    @SerialName("name")
    val name: String,

    @SerialName("is_checked")
    val isChecked: Boolean = false,

    @SerialName("checklist")
    val checklist: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)

fun ChecklistDTO.toDomainModel(): Checklist = Checklist(
    id = id,
    user = user,
    name = name,
    date = date,
    isPublic = isPublic,
    collection = collection,
    createdAt = createdAt,
    updatedAt = updatedAt,
    items = items.map { it.toDomainModel() }
)

fun ChecklistItemDTO.toDomainModel(): ChecklistItem = ChecklistItem(
    id = id,
    user = user,
    name = name,
    isChecked = isChecked,
    checklist = checklist,
    createdAt = createdAt,
    updatedAt = updatedAt
)
