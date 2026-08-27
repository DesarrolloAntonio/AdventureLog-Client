package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.network.api.AdventureApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.mappers.createAdventureRequest
import com.desarrollodroide.adventurelog.core.network.model.mappers.toVisitRequest
import com.desarrollodroide.adventurelog.core.network.model.request.UpdateLocationRequest
import com.desarrollodroide.adventurelog.core.network.model.request.CategoryRequest
import com.desarrollodroide.adventurelog.core.network.model.response.LocationDTO
import com.desarrollodroide.adventurelog.core.network.model.response.LocationsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.SearchResultsDTO
import com.desarrollodroide.adventurelog.core.network.utils.toCoordinateString
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class KtorAdventureApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json = defaultJson
) : AdventureApi {

    private val logger = Logger.withTag("KtorAdventureNetworkDataSource")

    private fun logJsonError(context: String, jsonContent: String, error: Exception) {
        logger.e(error) { "$context: ${error.message}" }
        
        // Log JSON in chunks to avoid truncation
        val chunkSize = 3000
        val totalChunks = (jsonContent.length + chunkSize - 1) / chunkSize
        
        logger.e { "=== JSON DEBUG START ($context) ===" }
        logger.e { "Total JSON length: ${jsonContent.length} characters" }
        logger.e { "Showing in $totalChunks chunks:" }
        
        for (i in 0 until totalChunks) {
            val start = i * chunkSize
            val end = minOf(start + chunkSize, jsonContent.length)
            val chunk = jsonContent.substring(start, end)
            logger.e { "Chunk ${i + 1}/$totalChunks: $chunk" }
        }
        
        logger.e { "=== JSON DEBUG END ===" }
    }

    override suspend fun getLocations(page: Int, pageSize: Int): List<LocationDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/locations/"

        logger.d { "🌐 API Request - GET $url?page=$page&page_size=$pageSize" }

        val response = httpClient.get(url) {
            parameter("page", page)
            parameter("page_size", pageSize)
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch locations with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        
        try {
            val adventuresResponse = json.decodeFromString<LocationsDTO>(responseText)

            logger.d { "📦 API Response - Fetched ${adventuresResponse.results?.size ?: 0} locations for page $page (requested pageSize: $pageSize)" }
            logger.d { "   Total count: ${adventuresResponse.count}" }

            return adventuresResponse.results ?: emptyList()
        } catch (e: Exception) {
            logJsonError("Locations JSON parse error", responseText, e)
            throw e
        }
    }

    override suspend fun getLocationsFiltered(
        page: Int,
        pageSize: Int,
        categoryIds: List<String>?,
        sortBy: String?,
        sortOrder: String?,
        isVisited: Boolean?,
        searchQuery: String?,
        includeCollections: Boolean
    ): List<LocationDTO> {
        val session = sessionProvider()

        // If there's a search query, use the search endpoint
        if (!searchQuery.isNullOrBlank()) {
            return searchLocations(searchQuery, page = page, pageSize = pageSize)
        }

        // Otherwise use the filtered endpoint
        val url = "${session.baseUrl}/api/locations/filtered/"

        logger.d {
            "🌐 API Request - GET $url with filters: " +
                    "page=$page, pageSize=$pageSize, categories=$categoryIds, " +
                    "sortBy=$sortBy, sortOrder=$sortOrder, isVisited=$isVisited"
        }

        val response = httpClient.get(url) {
            parameter("page", page)
            parameter("page_size", pageSize)

            // Map parameters to match backend expectations
            // types parameter: backend expects comma-separated category names or "all"
            if (!categoryIds.isNullOrEmpty()) {
                parameter("types", categoryIds.joinToString(","))
            } else {
                parameter("types", "all")
            }

            // order_by and order_direction
            sortBy?.let { parameter("order_by", it) }
            sortOrder?.let { parameter("order_direction", it) }

            // is_visited parameter: backend expects "true", "false", or "all"
            when (isVisited) {
                true -> parameter("is_visited", "true")
                false -> parameter("is_visited", "false")
                null -> parameter("is_visited", "all")
            }

            // include_collections parameter
            parameter("include_collections", includeCollections.toString())

            headers {
                commonHeaders(session.sessionToken)
            }
        }

        logger.d { "Response status: ${response.status}" }

        if (!response.status.isSuccess()) {
            val errorBody = try {
                response.body<String>()
            } catch (_: Exception) {
                "Unable to read error body"
            }
            logger.e { "Failed to fetch filtered locations. Error: $errorBody" }
            throw HttpException(
                response.status.value,
                "Failed to fetch filtered locations with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        
        try {
            val adventuresResponse = json.decodeFromString<LocationsDTO>(responseText)

            logger.d {
                "📦 API Response - Fetched ${adventuresResponse.results?.size ?: 0} filtered locations " +
                        "for page $page (total: ${adventuresResponse.count})"
            }

            return adventuresResponse.results ?: emptyList()
        } catch (e: Exception) {
            logJsonError("Filtered locations JSON parse error", responseText, e)
            throw e
        }
    }

    /**
     * Search is global and paged server-side, and returns ranked descriptors rather than whole
     * locations, so the ids of this page are hydrated into full records afterwards. Asking for
     * `types=location` keeps cities, collections and users out of the count, which is what makes
     * `limit`/`offset` line up with the page the list is asking for.
     */
    override suspend fun globalSearch(query: String, limit: Int): SearchResultsDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/search/"

        logger.d { "🔍 API Request - GET $url (everything) with query: '$query'" }

        val response = httpClient.get(url) {
            parameter("query", query)
            parameter("limit", limit)
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Search failed with status: ${response.status}"
            )
        }

        val results = json.decodeFromString<SearchResultsDTO>(response.body<String>())

        logger.d { "📦 API Response - ${results.results.size} hits of ${results.total}" }

        return results
    }

    private suspend fun searchLocations(
        searchQuery: String,
        page: Int,
        pageSize: Int
    ): List<LocationDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/search/"

        logger.d { "🔍 API Request - GET $url with query: '$searchQuery' (page $page)" }

        val response = httpClient.get(url) {
            parameter("query", searchQuery)
            parameter("types", "location")
            parameter("limit", pageSize)
            parameter("offset", (page - 1).coerceAtLeast(0) * pageSize)
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            val errorBody = try {
                response.body<String>()
            } catch (_: Exception) {
                "Unable to read error body"
            }
            logger.e { "Failed to search locations. Error: $errorBody" }
            throw HttpException(
                response.status.value,
                "Failed to search locations with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()

        val ids = try {
            json.decodeFromString<SearchResultsDTO>(responseText).locationIds()
        } catch (e: Exception) {
            logJsonError("Search locations JSON parse error", responseText, e)
            throw e
        }

        // A hit whose location cannot be loaded is dropped rather than failing the page: the
        // search index can outlive the record it points at.
        val locations = ids.mapNotNull { id ->
            try {
                getAdventureDetail(id)
            } catch (e: Exception) {
                logger.w { "Skipping search hit $id: ${e.message}" }
                null
            }
        }

        logger.d { "📦 Search Response - ${locations.size} of ${ids.size} hits loaded for '$searchQuery'" }

        return locations
    }

    override suspend fun duplicateLocation(locationId: String): LocationDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/locations/$locationId/duplicate/"

        logger.d { "🌐 API Request - POST $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            // The body may carry a collection_id to file the copy somewhere; nothing to say here.
            setBody("{}")
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            logger.e { "Failed to duplicate location: ${response.status} $body" }
            throw HttpException(
                response.status.value,
                "Failed to duplicate location: ${response.status}"
            )
        }

        return json.decodeFromString<LocationDTO>(response.body<String>())
    }

    override suspend fun getShareImage(locationId: String, aspect: String): ByteArray {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/locations/$locationId/share-image/$aspect/"

        logger.d { "🌐 API Request - GET $url" }

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to build share image: ${response.status}"
            )
        }

        return response.bodyAsBytes()
    }

    override suspend fun getAdventureDetail(objectId: String): LocationDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/locations/$objectId/"

        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch location detail with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        
        try {
            return json.decodeFromString<LocationDTO>(responseText)
        } catch (e: Exception) {
            logJsonError("Location detail JSON parse error", responseText, e)
            throw e
        }
    }

    override suspend fun createLocation(
        name: String,
        description: String,
        category: Category,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?,
        activityTypes: List<String>
    ): LocationDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/locations/"

        val requestBody = createAdventureRequest(
            name = name,
            description = description,
            category = category,
            rating = rating,
            link = link,
            location = location,
            latitude = latitude,
            longitude = longitude,
            isPublic = isPublic,
            visits = visits,
            price = price,
            priceCurrency = priceCurrency,
            activityTypes = activityTypes
        )

        logger.d { "Creating location with request: name=$name, categoryId=${category.id}, isPublic=$isPublic, visits=${visits.size}" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            val errorBody = try {
                response.body<String>()
            } catch (_: Exception) {
                "Unable to read error body"
            }
            logger.e { "Failed to create location. Status: ${response.status}, Error: $errorBody" }
            throw HttpException(
                response.status.value,
                "Failed to create location with status: ${response.status}. Error: $errorBody"
            )
        }

        val responseText = response.body<String>()
        
        try {
            return json.decodeFromString<LocationDTO>(responseText)
        } catch (e: Exception) {
            logJsonError("Location detail JSON parse error", responseText, e)
            throw e
        }
    }

    override suspend fun updateAdventure(
        adventureId: String,
        name: String,
        description: String,
        category: Category?,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        tags: List<String>,
        collections: List<String>,
        visits: List<VisitFormData>,
        price: Double?,
        priceCurrency: String?
    ): LocationDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/locations/$adventureId/"

        // TODO: Remove visits=null workaround when backend issue #579 is fixed (visits should not be serialized)
        val updateRequest = UpdateLocationRequest(
            name = name,
            description = description,
            rating = rating,
            link = link,
            location = location,
            latitude = latitude.toCoordinateString(),
            longitude = longitude.toCoordinateString(),
            isPublic = isPublic,
            tags = tags,  
            collections = collections,
            visits = null,
            category = category?.let { cat ->
                CategoryRequest(
                    name = cat.name,
                    displayName = cat.displayName,
                    icon = cat.icon
                )
            },
            price = price,
            priceCurrency = priceCurrency
        )

        logger.d { "Updating location $adventureId with ${collections.size} collections" }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(updateRequest)
        }

        if (!response.status.isSuccess()) {
            val errorBody = try {
                response.body<String>()
            } catch (_: Exception) {
                "Unable to read error body"
            }
            logger.e { "Failed to update location. Status: ${response.status}, Error: $errorBody" }
            throw HttpException(
                response.status.value,
                "Failed to update location with status: ${response.status}. Error: $errorBody"
            )
        }

        val responseText = response.body<String>()
        
        try {
            return json.decodeFromString<LocationDTO>(responseText)
        } catch (e: Exception) {
            logJsonError("Location detail JSON parse error", responseText, e)
            throw e
        }
    }

    override suspend fun deleteLocation(adventureId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/locations/$adventureId/"

        val response = httpClient.delete(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to delete location with status: ${response.status}"
            )
        }
    }
}
