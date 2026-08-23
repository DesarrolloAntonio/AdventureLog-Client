package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.TripStatus
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationImageDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("image")
    val image: String,
    
    @SerialName("is_primary")
    val isPrimary: Boolean,
    
    @SerialName("user")
    val user: String? = null,
    
    @SerialName("immich_id")
    val immichId: String? = null
)

@Serializable
data class UltraSlimCollectionDTO(
    @SerialName("id")
    val id: String,
    
    @SerialName("user")
    val user: String? = null,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("description")
    val description: String? = null,
    
    @SerialName("is_public")
    val isPublic: Boolean = false,
    
    @SerialName("is_archived")
    val isArchived: Boolean = false,
    
    @SerialName("created_at")
    val createdAt: String,
    
    @SerialName("updated_at")
    val updatedAt: String,
    
    @SerialName("start_date")
    val startDate: String? = null,
    
    @SerialName("end_date")
    val endDate: String? = null,
    
    @SerialName("link")
    val link: String? = null,
    
    // Backend sends these fields instead of featured_image and adventure_count
    @SerialName("location_images")
    val locationImages: List<LocationImageDTO> = emptyList(),
    
    @SerialName("location_count")
    val locationCount: Int = 0,
    
    @SerialName("shared_with")
    val sharedWith: List<String> = emptyList(),

    // Derived server-side from start_date/end_date, so the client never has to compare dates
    // itself and cannot drift from what the calendar considers an in-progress trip.
    @SerialName("status")
    val status: String? = null,

    @SerialName("days_until_start")
    val daysUntilStart: Int? = null
)

fun UltraSlimCollectionDTO.toDomainModel(): UltraSlimCollection {
    // Extract primary image or first available
    val featuredImage = locationImages
        .firstOrNull { it.isPrimary }?.image
        ?: locationImages.firstOrNull()?.image
    
    return UltraSlimCollection(
        id = id,
        name = name,
        description = description ?: "",
        isPublic = isPublic,
        isArchived = isArchived,
        createdAt = createdAt,
        updatedAt = updatedAt,
        startDate = startDate,
        endDate = endDate,
        adventureCount = locationCount,
        featuredImage = featuredImage,
        link = link,
        status = TripStatus.fromApi(status),
        daysUntilStart = daysUntilStart
    )
}
