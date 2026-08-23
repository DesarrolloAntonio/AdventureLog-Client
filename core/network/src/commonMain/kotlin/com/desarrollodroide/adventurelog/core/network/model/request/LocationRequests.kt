package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateLocationRequest(
    val name: String,
    val description: String? = null,
    val rating: Double? = null,
    @SerialName("tags")
    val tags: List<String>? = null,
    val location: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean = false,
    val collections: List<String>? = null,
    val link: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val visits: List<VisitRequest>? = null,
    val category: CategoryRequest? = null,
    val price: Double? = null,
    @SerialName("price_currency")
    val priceCurrency: String? = null
)

@Serializable
data class UpdateLocationRequest(
    val name: String,
    val description: String,
    val rating: Double,
    @SerialName("tags")
    val tags: List<String>,
    val location: String,
    @SerialName("is_public")
    val isPublic: Boolean,
    val collections: List<String>,
    val link: String,
    val longitude: String? = null,
    val latitude: String? = null,
    val visits: List<VisitRequest>? = null,
    val category: CategoryRequest? = null,
    val city: Map<String, String> = emptyMap(),
    val country: Map<String, String> = emptyMap(),
    val region: Map<String, String> = emptyMap(),
    // The client serialises nulls, so these are only safe to send because the form round-trips
    // whatever the location already had: sending a bare null here would clear a stored price.
    val price: Double? = null,
    @SerialName("price_currency")
    val priceCurrency: String? = null
)

@Serializable
data class VisitRequest(
    @SerialName("start_date")
    val startDate: String?,
    @SerialName("end_date")
    val endDate: String?,
    val timezone: String?,
    val notes: String?
)

/**
 * Body of `POST /api/visits/`. `object_id` and `location` carry the same id - the server reads
 * `location`, and the web client sends both, so this matches it rather than guessing.
 */
@Serializable
data class CreateVisitRequest(
    @SerialName("object_id")
    val objectId: String,
    val location: String,
    @SerialName("start_date")
    val startDate: String?,
    @SerialName("end_date")
    val endDate: String?,
    val timezone: String?,
    val notes: String?
) {
    companion object {
        fun from(locationId: String, visit: VisitRequest) = CreateVisitRequest(
            objectId = locationId,
            location = locationId,
            startDate = visit.startDate,
            endDate = visit.endDate,
            timezone = visit.timezone,
            notes = visit.notes
        )
    }
}

/**
 * Body of `POST /api/trails/`. The server wants either a link or a Wanderer id; this client only
 * sends plain links, so the Wanderer fields are left out and default to null server-side.
 */
@Serializable
data class TrailRequest(
    val name: String,
    val location: String,
    val link: String?
) {
    companion object {
        fun from(locationId: String, trail: com.desarrollodroide.adventurelog.core.model.TrailFormData) =
            TrailRequest(
                name = trail.name.trim(),
                location = locationId,
                link = trail.link.trim().takeIf { it.isNotBlank() }
            )
    }
}

@Serializable
data class CategoryRequest(
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val icon: String
)

@Serializable
data class CityRequest(
    val id: String? = null,
    @SerialName("region_name")
    val regionName: String? = null,
    @SerialName("country_name")
    val countryName: String? = null,
    val name: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val region: String? = null
)

@Serializable
data class CountryRequest(
    val id: Int? = null,
    @SerialName("flag_url")
    val flagUrl: String? = null,
    @SerialName("num_regions")
    val numRegions: String? = null,
    @SerialName("num_visits")
    val numVisits: String? = null,
    val name: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null,
    val subregion: String? = null,
    val capital: String? = null,
    val longitude: String? = null,
    val latitude: String? = null
)

@Serializable
data class RegionRequest(
    val id: String? = null,
    @SerialName("num_cities")
    val numCities: String? = null,
    @SerialName("country_name")
    val countryName: String? = null,
    val name: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
    val country: Int? = null
)
/** A PATCH that moves a collection in or out of the archive and touches nothing else. */
@Serializable
data class ArchiveCollectionRequest(
    @SerialName("is_archived")
    val isArchived: Boolean
)
