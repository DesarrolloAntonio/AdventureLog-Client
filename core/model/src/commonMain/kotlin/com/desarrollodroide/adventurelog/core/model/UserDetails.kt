package com.desarrollodroide.adventurelog.core.model

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
    val hasPassword: String
)