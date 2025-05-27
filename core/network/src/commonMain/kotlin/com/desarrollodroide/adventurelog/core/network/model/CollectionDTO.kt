package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.Collection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("adventures")
    val adventures: List<AdventureDTO>? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    @SerialName("transportations")
    val transportations: List<TransportationDTO>? = null,

    @SerialName("notes")
    val notes: List<NoteDTO>? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("checklists")
    val checklists: List<ChecklistDTO>? = null,

    @SerialName("is_archived")
    val isArchived: Boolean = false,

    @SerialName("shared_with")
    val sharedWith: List<String>? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("lodging")
    val lodging: List<LodgingDTO>? = null
)

fun CollectionDTO.toDomainModel(): Collection = Collection(
    id = id ?: "",
    description = description ?: "",
    userId = userId ?: "",
    name = name,
    isPublic = isPublic,
    adventures = adventures?.map { it.toDomainModel() } ?: emptyList(),
    createdAt = createdAt ?: "",
    startDate = startDate,
    endDate = endDate,
    transportations = transportations?.map { it.id ?: "" } ?: emptyList(),
    notes = notes?.map { it.id ?: "" } ?: emptyList(),
    updatedAt = updatedAt ?: "",
    checklists = checklists?.map { it.id ?: "" } ?: emptyList(),
    isArchived = isArchived,
    sharedWith = sharedWith ?: emptyList(),
    link = link ?: "",
    lodging = lodging?.map { it.id ?: "" } ?: emptyList()
)
