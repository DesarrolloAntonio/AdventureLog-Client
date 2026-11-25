package com.desarrollodroide.adventurelog.feature.collections.ui.components

/**
 * The ways of looking at one collection.
 *
 * This is a different axis from [CollectionTab], and the web draws it as a separate row for that
 * reason: the tabs choose *which of a collection's things* to list, and these choose *how to look
 * at all of them* - as a list, along a timeline, on a map, by date, or as figures. Folding the two
 * into one row would put "Notes" and "Map" beside each other as if they were alternatives.
 */
enum class CollectionView(val title: String) {
    ITEMS("Items"),
    ITINERARY("Itinerary"),
    MAP("Map"),
    CALENDAR("Calendar"),
    RECOMMENDATIONS("Nearby"),
    STATS("Stats")
}
