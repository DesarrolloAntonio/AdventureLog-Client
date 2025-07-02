package com.desarrollodroide.adventurelog.core.network.model.mappers

import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.network.model.request.CategoryRequest
import com.desarrollodroide.adventurelog.core.network.model.request.CreateAdventureRequest
import com.desarrollodroide.adventurelog.core.network.model.request.VisitRequest
import com.desarrollodroide.adventurelog.core.network.utils.toCoordinateString

fun Category.toCategoryRequest(): CategoryRequest {
    return CategoryRequest(
        id = id,
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
    visitDates: Visit?
): CreateAdventureRequest {
    return CreateAdventureRequest(
        name = name,
        description = description.takeIf { it.isNotBlank() },
        rating = rating.takeIf { it > 0 },
        activityTypes = null,
        location = location.takeIf { it.isNotBlank() },
        isPublic = isPublic,
        collections = emptyList(),
        link = link.takeIf { it.isNotBlank() },
        longitude = longitude.toCoordinateString(),
        latitude = latitude.toCoordinateString(),
        visits = visitDates?.let { listOf(it.toVisitRequest()) } ?: emptyList(),
        category = category.toCategoryRequest()
    )
}
