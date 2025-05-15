package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(
    val id: Int,
    val profilePic: String?,
    val uuid: String,
    val publicProfile: Boolean,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val dateJoined: String,
    val isStaff: Boolean,
    val hasPassword: String,
    val sessionToken: String
)