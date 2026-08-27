package com.desarrollodroide.adventurelog.core.model

/**
 * Someone else on this server who has chosen to be findable.
 *
 * Only a public profile can be invited to a collection - the server refuses to share with anyone
 * else - so this is the whole population of people a collection can reach.
 */
data class PublicUser(
    val uuid: String,
    val username: String,
    val firstName: String = "",
    val lastName: String = "",
    val profilePic: String? = null
) {
    val displayName: String
        get() = listOf(firstName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { username }
}
