package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CollectionInviteDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("collection")
    val collection: String,
    
    @SerialName("invited_user")
    val invitedUser: Int,
    
    @SerialName("created_at")
    val createdAt: String,
    
    @SerialName("updated_at")
    val updatedAt: String
)

fun CollectionInviteDTO.toDomainModel(): CollectionInvite = CollectionInvite(
    id = id,
    collection = collection,
    invitedUser = invitedUser,
    createdAt = createdAt,
    updatedAt = updatedAt
)
