package com.desarrollodroide.adventurelog.core.model

data class UltraSlimCollection(
    val id: String,
    val name: String,
    val description: String,
    val isPublic: Boolean,
    val isArchived: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val startDate: String?,
    val endDate: String?,
    val adventureCount: Int,
    val featuredImage: String?,
    val link: String?,
    val status: TripStatus = TripStatus.FOLDER,
    val daysUntilStart: Int? = null
)

/**
 * Where a collection sits relative to today. The server derives this from the start and end
 * dates, so the client does not have to parse or compare them - and cannot disagree with the
 * calendar about what "in progress" means.
 */
enum class TripStatus {
    /** No dates set: a plain folder rather than a trip. */
    FOLDER,
    UPCOMING,
    IN_PROGRESS,
    COMPLETED;

    companion object {
        fun fromApi(value: String?): TripStatus = when (value) {
            "upcoming" -> UPCOMING
            "in_progress" -> IN_PROGRESS
            "completed" -> COMPLETED
            else -> FOLDER
        }
    }
}
