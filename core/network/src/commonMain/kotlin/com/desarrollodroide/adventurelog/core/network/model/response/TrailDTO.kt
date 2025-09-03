package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Trail
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrailDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("user")
    val user: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("location")
    val location: String,
    
    @SerialName("created_at")
    val createdAt: String,
    
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

fun TrailDTO.toDomainModel(): Trail = Trail(
    id = id,
    user = user,
    name = name,
    location = location,
    createdAt = createdAt,
    link = link,
    wandererId = wandererId,
    provider = provider,
    wandererData = wandererData,
    wandererLink = wandererLink
)
