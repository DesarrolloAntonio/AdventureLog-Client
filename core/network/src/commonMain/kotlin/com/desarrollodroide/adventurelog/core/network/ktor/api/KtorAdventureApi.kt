package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.network.api.AdventureApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.request.CategoryRequest
import com.desarrollodroide.adventurelog.core.network.model.request.CreateAdventureRequest
import com.desarrollodroide.adventurelog.core.network.model.request.VisitRequest
import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.response.AdventuresDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class KtorAdventureNetworkDataSource(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json = defaultJson
) : AdventureApi {

    private val logger = Logger.withTag("KtorAdventureNetworkDataSource")

    override suspend fun getAdventures(page: Int, pageSize: Int): List<AdventureDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/adventures/"
        
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
                "Failed to fetch adventures with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        val adventuresResponse = json.decodeFromString<AdventuresDTO>(responseText)
        
        logger.d { "Fetched ${adventuresResponse.results?.size ?: 0} adventures" }
        return adventuresResponse.results ?: emptyList()
    }

    override suspend fun getAdventureDetail(objectId: String): AdventureDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/adventures/$objectId/"
        
        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch adventure detail with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<AdventureDTO>(responseText)
    }

    override suspend fun createAdventure(
        name: String,
        description: String,
        categoryId: String,
        rating: Double,
        link: String,
        location: String,
        latitude: Double?,
        longitude: Double?,
        isPublic: Boolean,
        visitDates: Visit?
    ): AdventureDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/adventures/"
        
        val category = mapCategory(categoryId)
        val visits = visitDates?.let { visit ->
            listOf(
                VisitRequest(
                    startDate = visit.startDate,
                    endDate = visit.endDate,
                    timezone = "America/Denver",
                    notes = visit.notes
                )
            )
        }

        val requestBody = CreateAdventureRequest(
            name = name,
            description = description,
            rating = rating,
            location = location,
            isPublic = isPublic,
            longitude = longitude,
            latitude = latitude,
            visits = visits,
            category = category,
            notes = null,
            link = link.takeIf { it.isNotBlank() }
        )

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to create adventure with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<AdventureDTO>(responseText)
    }

    override suspend fun updateAdventure(
        adventureId: String,
        name: String?,
        description: String?,
        categoryId: String?,
        rating: Double?,
        link: String?,
        location: String?,
        latitude: Double?,
        longitude: Double?,
        isPublic: Boolean?,
        visitDates: Visit?
    ): AdventureDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/adventures/$adventureId/"
        
        val updates = buildMap {
            name?.let { put("name", it) }
            description?.let { put("description", it) }
            categoryId?.let { put("category", mapCategory(it)) }
            rating?.let { put("rating", it) }
            link?.let { put("link", it) }
            location?.let { put("location", it) }
            latitude?.let { put("latitude", it) }
            longitude?.let { put("longitude", it) }
            isPublic?.let { put("is_public", it) }
        }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(updates)
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to update adventure with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<AdventureDTO>(responseText)
    }

    override suspend fun deleteAdventure(adventureId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/adventures/$adventureId/"
        
        val response = httpClient.delete(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to delete adventure with status: ${response.status}"
            )
        }
    }

    private fun mapCategory(categoryId: String): CategoryRequest {
        return when (categoryId) {
            "1" -> CategoryRequest("restaurant", "Restaurant", "🍽️")
            "2" -> CategoryRequest("hotel", "Hotel", "🏨")
            "3" -> CategoryRequest("museum", "Museum", "🏛️")
            "4" -> CategoryRequest("park", "Park", "🌳")
            "5" -> CategoryRequest("beach", "Beach", "🏖️")
            else -> CategoryRequest("outdoor", "Outdoor", "🏕️")
        }
    }
}
