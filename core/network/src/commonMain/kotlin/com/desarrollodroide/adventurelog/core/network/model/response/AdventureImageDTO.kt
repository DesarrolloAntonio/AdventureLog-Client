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

    @SerialName("adventure")
    val adventure: String,  // Required (*)

    @SerialName("is_primary")
    val isPrimary: Boolean = false,

    @SerialName("user_id")
    val userId: String? = null,

    @SerialName("immich_id")
    val immichId: String? = null
)

fun AdventureImageDTO.toDomainModel() = AdventureImage(
    id = id ?: "",
    image = image ?: "",
    adventure = adventure,
    isPrimary = isPrimary,
    userId = userId ?: ""
)