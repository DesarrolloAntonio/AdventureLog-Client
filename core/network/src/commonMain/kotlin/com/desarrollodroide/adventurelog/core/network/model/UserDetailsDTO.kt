package com.desarrollodroide.adventurelog.core.network.model

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
    val uuid: String? = null,

    @SerialName("public_profile")
    val publicProfile: Boolean? = null,

    @SerialName("username")
    val username: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("first_name")
    val firstName: String? = null,

    @SerialName("last_name")
    val lastName: String? = null,

    @SerialName("date_joined")
    val dateJoined: String? = null,

    @SerialName("is_staff")
    val isStaff: Boolean? = null,

    @SerialName("has_password")
    val hasPassword: String? = null,
    
    @SerialName("session_token")
    val sessionToken: String? = null
)

fun UserDetailsDTO.toDomainModel(serverUrl: String = ""): UserDetails = UserDetails(
    id = id ?: -1,
    profilePic = profilePic,
    uuid = uuid ?: "",
    publicProfile = publicProfile ?: false,
    username = username ?: "",
    email = email ?: "",
    firstName = firstName ?: "",
    lastName = lastName ?: "",
    dateJoined = dateJoined ?: "",
    isStaff = isStaff ?: false,
    hasPassword = hasPassword ?: "",
    sessionToken = sessionToken?: "",
    serverUrl = serverUrl
)