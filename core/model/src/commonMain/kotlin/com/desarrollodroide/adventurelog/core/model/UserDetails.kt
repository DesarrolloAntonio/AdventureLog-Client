package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserDetails(
    val pk: Int? = null,
    val profilePic: String? = null,
    val uuid: String,
    val publicProfile: Boolean = false,
    val measurementSystem: String = "metric",
    val username: String,
    val email: String? = null,
    val firstName: String = "",
    val lastName: String = "",
    val dateJoined: String,
    val isStaff: Boolean = false,
    val disablePassword: Boolean = false,
    val hasPassword: Boolean = true,
    val sessionToken: String? = null,
    val serverUrl: String? = null
)
