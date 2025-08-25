package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("uuid")
    val uuid: String? = null,
    
    @SerialName("username")
    val username: String? = null,
    
    @SerialName("first_name")
    val firstName: String? = null,
    
    @SerialName("last_name")
    val lastName: String? = null,
    
    @SerialName("profile_pic")
    val profilePic: String? = null,
    
    @SerialName("public_profile")
    val publicProfile: Boolean = false,
    
    @SerialName("measurement_system")
    val measurementSystem: String? = null,
    
    @SerialName("date_joined")
    val dateJoined: String? = null,
    
    @SerialName("is_staff")
    val isStaff: Boolean = false,
    
    @SerialName("disable_password")
    val disablePassword: Boolean = false,
    
    @SerialName("has_password")
    val hasPassword: Boolean = false
)
