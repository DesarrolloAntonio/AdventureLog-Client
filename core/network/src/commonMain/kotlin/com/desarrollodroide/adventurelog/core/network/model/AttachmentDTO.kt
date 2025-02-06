package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.Attachment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttachmentDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("file")
    val file: String? = null,

    @SerialName("adventure")
    val adventure: String,  // Required (*)

    @SerialName("extension")
    val extension: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("user_id")
    val userId: Int? = null
)

fun AttachmentDTO.toDomainModel() = Attachment(
    id = id ?: "",
    file = file ?: "",
    adventure = adventure,
    extension = extension ?: "",
    name = name ?: "",
    userId = userId ?: -1
)