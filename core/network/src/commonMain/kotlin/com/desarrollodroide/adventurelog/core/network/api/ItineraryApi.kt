package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind

/** Putting a collection's own things on the days of the trip. */
interface ItineraryApi {
    /**
     * Builds a whole itinerary from the dates already on the collection's records.
     *
     * The server refuses when the collection already has entries, so this is an offer to make on
     * an empty itinerary and nowhere else.
     */
    suspend fun autoGenerateItinerary(collectionId: String): List<ItineraryEntry>

    /** Puts one of the collection's items on [date], or in the trip-context bucket when null. */
    suspend fun addItineraryEntry(
        collectionId: String,
        kind: ItineraryItemKind,
        itemId: String,
        date: String?,
        order: Int
    ): ItineraryEntry

    /** Takes an entry off its day. The item itself is untouched - only the placing is removed. */
    suspend fun deleteItineraryEntry(entryId: String)
}
