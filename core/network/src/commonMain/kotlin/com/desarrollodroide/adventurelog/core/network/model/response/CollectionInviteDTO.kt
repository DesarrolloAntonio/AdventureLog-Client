package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.CollectionInvite
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /api/collections/invites/`.
 *
 * Mirrors the server's serializer exactly. An earlier version of this model required an
 * `invited_user` field the server does not send, so decoding the list would have thrown - the
 * invites screen had never been built, so nothing ever called it.
 */
@Serializable
data class CollectionInviteDTO(
    @SerialName("id")
    val id: String,

    @SerialName("collection")
    val collection: String,

    @SerialName("name")
    val name: String = "",

    @SerialName("collection_owner_username")
    val ownerUsername: String = "",

    @SerialName("collection_user_first_name")
    val ownerFirstName: String? = null,

    @SerialName("collection_user_last_name")
    val ownerLastName: String? = null,

    @SerialName("created_at")
    val createdAt: String = ""
)

fun CollectionInviteDTO.toDomainModel(): CollectionInvite = CollectionInvite(
    id = id,
    collectionId = collection,
    collectionName = name,
    ownerUsername = ownerUsername,
    ownerFirstName = ownerFirstName.orEmpty(),
    ownerLastName = ownerLastName.orEmpty(),
    createdAt = createdAt
)
