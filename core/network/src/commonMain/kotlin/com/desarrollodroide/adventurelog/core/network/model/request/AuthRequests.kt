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
    val uuid: String? = null,
    val display: String,
    @SerialName("has_usable_password")
    val hasUsablePassword: Boolean? = null,
    val email: String,
    val username: String,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("profile_pic")
    val profilePic: String? = null,
    @SerialName("public_profile")
    val publicProfile: Boolean? = null,
    @SerialName("measurement_system")
    val measurementSystem: String? = null,
    @SerialName("date_joined")
    val dateJoined: String? = null,
    @SerialName("is_staff")
    val isStaff: Boolean? = null,
    @SerialName("disable_password")
    val disablePassword: Boolean? = null
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
