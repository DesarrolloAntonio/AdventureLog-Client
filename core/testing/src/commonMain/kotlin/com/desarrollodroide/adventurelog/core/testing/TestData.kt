package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.model.CalendarEvent
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.model.ItineraryDayNote
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import com.desarrollodroide.adventurelog.core.model.Lodging
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.model.City
import com.desarrollodroide.adventurelog.core.model.ContentImage
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.Visit
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
    category: String? = null,
    visits: List<Visit> = emptyList(),
    images: List<ContentImage> = emptyList(),
    country: String? = null,
    region: String? = null,
    city: String? = null
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
    visits = visits,
    images = images,
    country = country?.let(::testCountry),
    region = region?.let(::testRegion),
    city = city?.let(::testCity),
    user = testUser
)

fun testVisit(id: String, startDate: String?, endDate: String? = startDate) = Visit(
    id = id,
    location = id,
    startDate = startDate,
    endDate = endDate,
    createdAt = "2024-01-01",
    updatedAt = "2024-01-01"
)

fun testImage(id: String) = ContentImage(id = id, image = "https://example/$id.jpg", user = "u")

fun testCountry(name: String) = Country(
    id = name.hashCode(),
    name = name,
    countryCode = name.take(2).uppercase(),
    flagUrl = "",
    numRegions = 0,
    numVisits = 0,
    subregion = null,
    capital = null,
    longitude = null,
    latitude = null
)

fun testRegion(name: String) = Region(
    id = name,
    name = name,
    countryName = "",
    numCities = 0,
    longitude = null,
    latitude = null,
    countryId = 0
)

fun testCity(name: String) = City(
    id = name,
    name = name,
    regionName = "",
    countryName = "",
    longitude = null,
    latitude = null,
    regionId = ""
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

fun testCollection(
    name: String = "Trip",
    id: String = name,
    startDate: String? = null,
    endDate: String? = null,
    locations: List<Location> = emptyList(),
    transportations: List<Transportation> = emptyList(),
    lodging: List<Lodging> = emptyList(),
    notes: List<Note> = emptyList(),
    checklists: List<Checklist> = emptyList(),
    sharedWith: List<String> = emptyList(),
    itinerary: List<ItineraryEntry> = emptyList(),
    itineraryDays: List<ItineraryDayNote> = emptyList()
) = Collection(
    id = id,
    description = "",
    userId = "u",
    name = name,
    isPublic = false,
    locations = locations,
    createdAt = "2024-01-01",
    startDate = startDate,
    endDate = endDate,
    transportations = transportations,
    notes = notes,
    updatedAt = "2024-01-01",
    checklists = checklists,
    isArchived = false,
    sharedWith = sharedWith,
    link = "",
    lodging = lodging,
    itinerary = itinerary,
    itineraryDays = itineraryDays
)

fun testItineraryEntry(
    itemId: String,
    date: String?,
    kind: ItineraryItemKind = ItineraryItemKind.LOCATION,
    id: String = "entry-$itemId-$date",
    order: Int = 0,
    collectionId: String = "Trip"
) = ItineraryEntry(
    id = id,
    collectionId = collectionId,
    kind = kind,
    itemId = itemId,
    date = date,
    isGlobal = date == null,
    order = order
)

fun testItineraryDayNote(
    date: String,
    name: String = "",
    description: String = "",
    id: String = "day-$date",
    collectionId: String = "Trip"
) = ItineraryDayNote(
    id = id,
    collectionId = collectionId,
    date = date,
    name = name,
    description = description
)

fun testLodging(
    name: String,
    checkIn: String? = null,
    checkOut: String? = null,
    id: String = name
) = Lodging(
    id = id,
    user = "u",
    name = name,
    checkIn = checkIn,
    checkOut = checkOut,
    createdAt = "2024-01-01",
    updatedAt = "2024-01-01"
)

fun testTransportation(
    name: String,
    date: String? = null,
    endDate: String? = null,
    id: String = name,
    fromLocation: String? = null,
    toLocation: String? = null
) = Transportation(
    id = id,
    user = "u",
    type = "plane",
    name = name,
    date = date,
    endDate = endDate,
    fromLocation = fromLocation,
    toLocation = toLocation,
    createdAt = "2024-01-01",
    updatedAt = "2024-01-01"
)

fun testNote(name: String, id: String = name) = Note(
    id = id,
    user = "u",
    name = name,
    content = "",
    date = null,
    links = emptyList(),
    isPublic = false,
    collection = null,
    createdAt = "2024-01-01",
    updatedAt = "2024-01-01"
)

fun testChecklist(name: String, id: String = name) = Checklist(
    id = id,
    user = "u",
    name = name,
    items = emptyList(),
    date = null,
    isPublic = false,
    collection = null,
    createdAt = "2024-01-01",
    updatedAt = "2024-01-01"
)
