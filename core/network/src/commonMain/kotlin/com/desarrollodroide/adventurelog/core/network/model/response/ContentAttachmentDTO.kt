package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Attachment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// This is the same as AttachmentDTO - they represent the same entity
@Serializable
data class ContentAttachmentDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("file")
    val file: String,
    
    @SerialName("extension")
    val extension: String,
    
    @SerialName("name")
    val name: String? = null,
    
    @SerialName("user")
    val user: String,  // UUID String
    
    @SerialName("geojson")
    val geojson: String? = null
)

fun ContentAttachmentDTO.toDomainModel(): Attachment = Attachment(
    id = id,
    file = file,
    extension = extension,
    name = name,
    user = user,
    geojson = geojson
)
