package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.ContentImage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContentImageDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("image")
    val image: String,
    
    @SerialName("is_primary")
    val isPrimary: Boolean = false,
    
    @SerialName("user")
    val user: String,  // UUID String
    
    @SerialName("immich_id")
    val immichId: String? = null
)

fun ContentImageDTO.toDomainModel(): ContentImage = ContentImage(
    id = id,
    image = image,
    isPrimary = isPrimary,
    user = user,
    immichId = immichId
)
