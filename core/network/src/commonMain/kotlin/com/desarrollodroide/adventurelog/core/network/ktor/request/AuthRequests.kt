package com.desarrollodroide.adventurelog.core.network.ktor.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String, 
    val password: String
)

@Serializable
data class LoginResponse(
    val status: Int,
    val data: LoginData,
    val meta: LoginMeta
)

@Serializable
data class LoginData(
    val user: LoginUserData,
    val methods: List<LoginMethod>
)

@Serializable
data class LoginUserData(
    val id: Int,
    val display: String,
    val has_usable_password: Boolean,
    val email: String,
    val username: String
)

@Serializable
data class LoginMethod(
    val method: String,
    val at: Double,
    val username: String
)

@Serializable
data class LoginMeta(
    val is_authenticated: Boolean
)
