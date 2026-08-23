package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response of `GET /api/search/`.
 *
 * The endpoint returns ranked hits across every entity type - locations, cities, collections,
 * users and so on - as light descriptors rather than full objects. Callers that need a whole
 * record fetch it by id afterwards.
 *
 * This shape replaced an older one that returned `{"locations": [...], "collections": [...]}`.
 * Because every field of that older model was optional, the client kept decoding the new
 * response without error and simply found nothing, so search silently returned no results.
 */
@Serializable
data class SearchResultsDTO(
    @SerialName("query")
    val query: String = "",

    @SerialName("total")
    val total: Int = 0,

    @SerialName("results")
    val results: List<SearchHitDTO> = emptyList()
) {
    /** Ids of the location hits, in the order the server ranked them. */
    fun locationIds(): List<String> =
        results.filter { it.type == TYPE_LOCATION }.map { it.id }

    companion object {
        const val TYPE_LOCATION = "location"
    }
}

@Serializable
data class SearchHitDTO(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String = "",

    @SerialName("title")
    val title: String = "",

    @SerialName("subtitle")
    val subtitle: String = "",

    @SerialName("url")
    val url: String = ""
)
