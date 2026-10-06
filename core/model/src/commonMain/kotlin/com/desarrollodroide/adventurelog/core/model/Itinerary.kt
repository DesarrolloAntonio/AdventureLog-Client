package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

/**
 * One of a collection's own things, placed on a day of the trip.
 *
 * An itinerary entry holds no content: it is a pointer at a location, a stay, a journey, a note or
 * a checklist the collection already carries, plus where in the trip it goes. The server models it
 * as a generic foreign key - a [kind] and an [itemId] - which is why resolving one means looking
 * it up in the collection rather than reading it off the entry.
 *
 * [isGlobal] is the trip-context bucket: something that belongs to the trip without belonging to
 * any one day of it.
 */
@Serializable
data class ItineraryEntry(
    val id: String,
    val collectionId: String,
    val kind: ItineraryItemKind,
    val itemId: String,
    val date: String?,
    val isGlobal: Boolean,
    /** Manual order within its day. The server keeps it; ties fall back to insertion order. */
    val order: Int
)

/** The six things the server will accept a pointer to. */
@Serializable
enum class ItineraryItemKind(val wireName: String) {
    LOCATION("location"),
    TRANSPORTATION("transportation"),
    LODGING("lodging"),
    NOTE("note"),
    CHECKLIST("checklist"),
    VISIT("visit");

    companion object {
        fun fromWire(name: String?): ItineraryItemKind? =
            entries.firstOrNull { it.wireName == name }
    }
}

/**
 * A name and a description for one day of a trip - "Sacred Valley", say, rather than "Day 3".
 *
 * Separate from the entries because a day can be named before anything is on it, and keeps its
 * name when the last thing is taken off it.
 */
@Serializable
data class ItineraryDayNote(
    val id: String,
    val collectionId: String,
    val date: String,
    val name: String,
    val description: String
)
