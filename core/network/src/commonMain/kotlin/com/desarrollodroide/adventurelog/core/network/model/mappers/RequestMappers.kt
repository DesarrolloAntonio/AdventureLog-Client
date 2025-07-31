package com.desarrollodroide.adventurelog.core.network.model.mappers

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.model.request.CategoryRequest
import com.desarrollodroide.adventurelog.core.network.model.request.CreateAdventureRequest
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
    // Convert date to ISO format with time and timezone
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
            "${it}T23:59:59Z"
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
): CreateAdventureRequest {
    return CreateAdventureRequest(
        name = name,
        description = description.takeIf { it.isNotBlank() },
        rating = rating.takeIf { it > 0 },
        activityTypes = activityTypes.takeIf { it.isNotEmpty() },
        location = location.takeIf { it.isNotBlank() },
        isPublic = isPublic,
        collections = emptyList(),
        link = link.takeIf { it.isNotBlank() },
        longitude = longitude.toCoordinateString(),
        latitude = latitude.toCoordinateString(),
        visits = visits.map { it.toVisitRequest() },
        category = category.toCategoryRequest()
    )
}
