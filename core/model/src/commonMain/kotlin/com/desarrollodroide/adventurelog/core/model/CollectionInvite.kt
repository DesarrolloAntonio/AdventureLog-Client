package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

/**
 * An invitation to join someone else's collection.
 *
 * Carries the collection's name and its owner, because an invite showing only ids would give the
 * user nothing to decide on.
 */
@Serializable
data class CollectionInvite(
    val id: String,
    val collectionId: String,
    val collectionName: String,
    val ownerUsername: String,
    val ownerFirstName: String = "",
    val ownerLastName: String = "",
    val createdAt: String
) {
    /** The owner's real name when they have one, otherwise the username they signed up with. */
    val ownerDisplayName: String
        get() = listOf(ownerFirstName, ownerLastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { ownerUsername }
}
