package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion

/**
 * The smallest believable version of each model, for tests that need one to exist rather than to
 * be anything in particular. Anything a test actually asserts on, it should pass in.
 */
val testUser = UserDetails(uuid = "u", username = "claude", dateJoined = "2024-01-01")

fun testCategory(displayName: String) = Category(
    id = displayName,
    name = displayName.lowercase(),
    displayName = displayName,
    icon = "",
    numAdventures = "1"
)

fun testLocation(
    name: String,
    id: String = name,
    latitude: String? = null,
    longitude: String? = null,
    isVisited: Boolean = false,
    tags: List<String> = emptyList(),
    category: String? = null
) = Location(
    id = id,
    name = name,
    createdAt = "2024-01-01",
    updatedAt = "2024-01-01",
    latitude = latitude,
    longitude = longitude,
    isVisited = isVisited,
    tags = tags,
    category = category?.let(::testCategory),
    user = testUser
)

fun testCalendarEvent(
    id: String,
    start: String,
    type: String = "visit",
    title: String = id
) = CalendarEvent(
    id = id,
    type = type,
    title = title,
    start = start,
    end = start,
    allDay = true,
    icon = "",
    category = "",
    locationLabel = "",
    collectionId = null,
    collectionName = null
)

fun testVisitedRegion(name: String) = VisitedRegion(
    id = name.hashCode(), userId = "u", regionId = name, name = name,
    longitude = 0.0, latitude = 0.0
)

fun testVisitedCity(name: String) = VisitedCity(
    id = name.hashCode(), userId = "u", cityId = name, name = name,
    longitude = 0.0, latitude = 0.0
)
