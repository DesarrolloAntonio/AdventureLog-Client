package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDetailsDTO(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("profile_pic")
    val profilePic: String? = null,

    @SerialName("uuid")
    val uuid: String,

    @SerialName("public_profile")
    val publicProfile: Boolean = false,

    @SerialName("measurement_system")
    val measurementSystem: String? = null,

    @SerialName("username")
    val username: String,

    @SerialName("email")
    val email: String? = null,

    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    @SerialName("date_joined")
    val dateJoined: String? = null,

    @SerialName("is_staff")
    val isStaff: Boolean = false,

    @SerialName("disable_password")
    val disablePassword: Boolean = false,

    @SerialName("has_password")
    val hasPassword: Boolean = true,
    
    @SerialName("session_token")
    val sessionToken: String? = null
)

fun UserDetailsDTO.toDomainModel(serverUrl: String = ""): UserDetails = UserDetails(
    pk = id,
    profilePic = profilePic,
    uuid = uuid,
    publicProfile = publicProfile,
    measurementSystem = measurementSystem ?: "metric",
    username = username,
    email = email,
    firstName = firstName ?: "",
    lastName = lastName ?: "",
    dateJoined = dateJoined ?: "",
    isStaff = isStaff,
    disablePassword = disablePassword,
    hasPassword = hasPassword,
    sessionToken = sessionToken ?: "",
    serverUrl = serverUrl
)
