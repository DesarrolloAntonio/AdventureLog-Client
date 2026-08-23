package com.desarrollodroide.adventurelog.core.network.model.mappers

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.model.request.CategoryRequest
import com.desarrollodroide.adventurelog.core.network.model.request.CreateLocationRequest
import com.desarrollodroide.adventurelog.core.network.model.request.VisitRequest
import com.desarrollodroide.adventurelog.core.network.utils.toCoordinateString

fun Category.toCategoryRequest(): CategoryRequest {
    return CategoryRequest(
        name = name,
        displayName = displayName,
        icon = icon
    )
}

fun Visit.toVisitRequest(): VisitRequest {
    return VisitRequest(
        startDate = startDate,
        endDate = endDate,
        timezone = timezone,
        notes = notes
    )
}

fun VisitFormData.toVisitRequest(): VisitRequest {
    val formattedStartDate = if (startDate.isNotEmpty()) {
        if (isAllDay) {
            "${startDate}T00:00:00Z"
        } else {
            val time = startTime ?: "12:00"
            "${startDate}T${time}:00Z"
        }
    } else {
        startDate
    }
    
    val formattedEndDate = endDate?.takeIf { it.isNotEmpty() }?.let {
        if (isAllDay) {
            // Midnight on both bounds, which is how the server stores an all-day visit and what
            // the web sends. An end of 23:59:59 misses the server's end-of-day test - that
            // compares against 23:59:59.999999 - so it is kept verbatim and the visit comes back
            // looking like it has times on it.
            "${it}T00:00:00Z"
        } else {
            val time = endTime ?: "12:00"
            "${it}T${time}:00Z"
        }
    } ?: formattedStartDate
    
    return VisitRequest(
        startDate = formattedStartDate,
        endDate = formattedEndDate,
        timezone = timezone,
        notes = notes
    )
}

fun createAdventureRequest(
    name: String,
    description: String,
    category: Category,
    rating: Double,
    link: String,
    location: String,
    latitude: String?,
    longitude: String?,
    isPublic: Boolean,
    visits: List<VisitFormData>,
    activityTypes: List<String> = emptyList()
): CreateLocationRequest {
    return CreateLocationRequest(
        name = name,
        description = description.takeIf { it.isNotBlank() },
        rating = rating.takeIf { it > 0 },
        tags = activityTypes.takeIf { it.isNotEmpty() },
        location = location.takeIf { it.isNotBlank() },
        isPublic = isPublic,
        collections = emptyList(),
        link = link.takeIf { it.isNotBlank() },
        longitude = longitude.toCoordinateString(),
        latitude = latitude.toCoordinateString(),
        // Visits are never nested here. Each one needs the id of a location that does not exist
        // yet at this point, so they are posted to /api/visits/ once the location has been
        // created - see SyncLocationVisitsUseCase.
        visits = null,
        category = category.toCategoryRequest()
    )
}