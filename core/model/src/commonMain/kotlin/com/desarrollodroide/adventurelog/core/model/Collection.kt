package com.desarrollodroide.adventurelog.core.model

data class Collection(
    val id: String,
    val description: String,
    val userId: String,
    val name: String,
    val isPublic: Boolean,
    val locations: List<Location>,
    val createdAt: String,
    val startDate: String?,
    val endDate: String?,
    val transportations: List<Transportation>,
    val notes: List<String>,
    val updatedAt: String,
    val checklists: List<String>,
    val isArchived: Boolean,
    val sharedWith: List<String>,
    val link: String,
    val lodging: List<String>
)

/**
 * Extension function to convert a full Collection to UltraSlimCollection
 * Used when creating or updating collections to maintain the slim collection cache
 */
fun Collection.toUltraSlimCollection(): UltraSlimCollection = UltraSlimCollection(
    id = id,
    name = name,
    description = description,
    isPublic = isPublic,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt,
    startDate = startDate,
    endDate = endDate,
    adventureCount = locations.size,
    featuredImage = extractFeaturedImage(),
    link = link
)

/**
 * Extracts the featured image from a collection
 * Priority: 1) Primary image, 2) First available image
 */
private fun Collection.extractFeaturedImage(): String? {
    // First try to find a primary image from any location
    locations.forEach { location ->
        location.images.find { it.isPrimary }?.let { return it.image }
    }
    
    // If no primary image, return the first available image
    return locations.firstOrNull()?.images?.firstOrNull()?.image
}