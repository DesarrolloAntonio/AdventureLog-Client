package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Note
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NoteDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("user")
    val user: Int,
    
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
    val createdAt: String,
    
    @SerialName("updated_at")
    val updatedAt: String
)

fun NoteDTO.toDomainModel(): Note = Note(
    id = id,
    user = user,
    name = name,
    content = content,
    date = date,
    links = links,
    isPublic = isPublic,
    collection = collection,
    createdAt = createdAt,
    updatedAt = updatedAt
)
