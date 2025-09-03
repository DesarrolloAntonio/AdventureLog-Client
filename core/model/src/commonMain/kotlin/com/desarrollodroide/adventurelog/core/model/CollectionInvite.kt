package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class CollectionInvite(
    val id: String,
    val collection: String,
    val invitedUser: Int,
    val createdAt: String,
    val updatedAt: String
)
