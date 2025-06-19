package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
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
    @SerialName("has_usable_password")
    val hasUsablePassword: Boolean,
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
    @SerialName("is_authenticated")
    val isAuthenticated: Boolean
)
