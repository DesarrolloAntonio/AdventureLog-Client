package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrailDTO(
    @SerialName("id")
    val id: String? = null,
    
    @SerialName("user")
    val user: String? = null,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("location")
    val location: String? = null,
    
    @SerialName("created_at")
    val createdAt: String? = null,
    
    @SerialName("link")
    val link: String? = null,
    
    @SerialName("wanderer_id")
    val wandererId: String? = null,
    
    @SerialName("provider")
    val provider: String? = null,
    
    @SerialName("wanderer_data")
    val wandererData: String? = null,
    
    @SerialName("wanderer_link")
    val wandererLink: String? = null
)
