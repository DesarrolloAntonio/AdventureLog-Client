package com.desarrollodroide.adventurelog.core.model

import kotlinx.serialization.Serializable

/**
 * Somewhere the server found near a point, that is not in the account yet.
 *
 * A recommendation is not a [Location]: it has no id of ours, no visits, no images, and it lives
 * in OpenStreetMap rather than in the account. Turning one into a place is a deliberate act - see
 * the create-location path - and until then it is only a suggestion.
 */
@Serializable
data class Recommendation(
    /** The source's own id, e.g. `osm:node:2548425926`. Unique within a set of results. */
    val id: String,
    val name: String,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    /** How far from the point that was searched, in kilometres. */
    val distanceKm: Double?,
    /** The kind of thing it is, in the source's words: "restaurant", "museum", "peak". */
    val primaryType: String?,
    val types: List<String>,
    val website: String?,
    val phoneNumber: String?,
    val openingHours: List<String>
)

/** The three kinds of thing the server will look for. */
enum class RecommendationCategory(val wireName: String, val label: String) {
    TOURISM("tourism", "Things to see"),
    FOOD("food", "Food and drink"),
    LODGING("lodging", "Somewhere to stay")
}
