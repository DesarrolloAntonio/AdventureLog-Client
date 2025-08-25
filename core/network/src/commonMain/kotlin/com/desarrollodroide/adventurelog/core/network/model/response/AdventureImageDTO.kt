package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.AdventureImage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdventureImageDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("image")
    val image: String? = null,

    @SerialName("is_primary")
    val isPrimary: Boolean = false,

    @SerialName("user")
    val userId: String? = null,

    @SerialName("immich_id")
    val immichId: String? = null,
    
    val adventure: String? = null
)

fun AdventureImageDTO.toDomainModel() = AdventureImage(
    id = id ?: "",
    image = image ?: "",
    adventure = adventure ?: "",
    isPrimary = isPrimary,
    userId = userId ?: ""
)