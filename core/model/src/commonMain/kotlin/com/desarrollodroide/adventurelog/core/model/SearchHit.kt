package com.desarrollodroide.adventurelog.core.model

/**
 * One result from searching everything at once.
 *
 * The server ranks locations, collections, the things inside a collection, and the world's
 * countries, regions and cities in a single list, and answers with a descriptor rather than the
 * record - whoever opens a hit fetches the real thing by id.
 */
data class SearchHit(
    val id: String,
    val type: String,
    val title: String,
    val subtitle: String
) {
    companion object {
        const val LOCATION = "location"
        const val COLLECTION = "collection"
    }
}

/** How a type of hit calls itself, and the emoji it wears in a list of mixed things. */
fun searchTypeLabel(type: String): String = when (type) {
    "location" -> "Place"
    "collection" -> "Collection"
    "lodging" -> "Lodging"
    "transportation" -> "Transport"
    "note" -> "Note"
    "checklist" -> "Checklist"
    "activity" -> "Activity"
    "country" -> "Country"
    "region" -> "Region"
    "city" -> "City"
    "user" -> "User"
    else -> type.replaceFirstChar { it.uppercase() }
}

fun searchTypeIcon(type: String): String = when (type) {
    "location" -> "\uD83D\uDCCD"
    "collection" -> "\uD83D\uDCC1"
    "lodging" -> "\uD83C\uDFE8"
    "transportation" -> "\u2708\uFE0F"
    "note" -> "\uD83D\uDCDD"
    "checklist" -> "\u2705"
    "activity" -> "\uD83C\uDFAF"
    "country" -> "\uD83C\uDF0D"
    "region" -> "\uD83D\uDDFA\uFE0F"
    "city" -> "\uD83C\uDFD9\uFE0F"
    "user" -> "\uD83D\uDC64"
    else -> "\uD83D\uDD0E"
}
