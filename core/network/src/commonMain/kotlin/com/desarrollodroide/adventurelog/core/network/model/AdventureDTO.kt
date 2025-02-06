package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventureDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("user_id")
    val userId: Int? = null,

    @SerialName("name")
    val name: String,  // Required (*)

    @SerialName("description")
    val description: String? = null,

    @SerialName("rating")
    val rating: Double? = null,

    @SerialName("activity_types")
    val activityTypes: List<String>? = null,

    @SerialName("location")
    val location: String? = null,

    @SerialName("is_public")
    val isPublic: Boolean = false,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null,

    @SerialName("images")
    val images: List<AdventureImageDTO>? = null,

    @SerialName("link")
    val link: String? = null,

    @SerialName("longitude")
    val longitude: String? = null,

    @SerialName("latitude")
    val latitude: String? = null,

    @SerialName("visits")
    val visits: List<VisitDTO>? = null,

    @SerialName("is_visited")
    val isVisited: String? = null,

    @SerialName("category")
    val category: CategoryDTO? = null,

    @SerialName("attachments")
    val attachments: List<AttachmentDTO>? = null
)

fun AdventureDTO.toDomainModel() = Adventure(
    id = id ?: "",
    userId = userId ?: -1,
    name = name,
    description = description ?: "",
    rating = rating ?: 0.0,
    activityTypes = activityTypes ?: emptyList(),
    location = location ?: "",
    isPublic = isPublic,
    collection = collection ?: "",
    createdAt = createdAt ?: "",
    updatedAt = updatedAt ?: "",
    images = images?.map { it.toDomainModel() } ?: emptyList(),
    link = link ?: "",
    longitude = longitude ?: "",
    latitude = latitude ?: "",
    visits = visits?.map { it.toDomainModel() } ?: emptyList(),
    isVisited = isVisited ?: "",
    category = category?.toDomainModel(),
    attachments = attachments?.map { it.toDomainModel() } ?: emptyList()
)