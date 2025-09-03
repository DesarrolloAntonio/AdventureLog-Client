package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.Lodging
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LodgingDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("user")
    val user: Int,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("type")
    val type: String = "other",
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("rating")
    val rating: Double? = null,
    
    @SerialName("link")
    val link: String? = null,
    
    @SerialName("check_in")
    val checkIn: String? = null,
    
    @SerialName("check_out")
    val checkOut: String? = null,
    
    @SerialName("reservation_number")
    val reservationNumber: String? = null,
    
    @SerialName("price")
    val price: String? = null,
    
    @SerialName("latitude")
    val latitude: String? = null,
    
    @SerialName("longitude")
    val longitude: String? = null,
    
    @SerialName("location")
    val location: String? = null,
    
    @SerialName("is_public")
    val isPublic: Boolean = false,
    
    @SerialName("collection")
    val collection: String? = null,
    
    @SerialName("created_at")
    val createdAt: String,
    
    @SerialName("updated_at")
    val updatedAt: String,
    
    @SerialName("timezone")
    val timezone: String? = null,
    
    @SerialName("images")
    val images: String? = null,
    
    @SerialName("attachments")
    val attachments: String? = null
)

fun LodgingDTO.toDomainModel(): Lodging = Lodging(
    id = id,
    user = user,
    name = name,
    type = type,
    description = description,
    rating = rating,
    link = link,
    checkIn = checkIn,
    checkOut = checkOut,
    reservationNumber = reservationNumber,
    price = price,
    latitude = latitude,
    longitude = longitude,
    location = location,
    isPublic = isPublic,
    collection = collection,
    createdAt = createdAt,
    updatedAt = updatedAt,
    timezone = timezone,
    images = images,
    attachments = attachments
)
