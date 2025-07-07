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
import com.desarrollodroide.adventurelog.core.network.model.request.CreateAdventureRequest
import com.desarrollodroide.adventurelog.core.network.model.response.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.response.AdventuresDTO
import com.desarrollodroide.adventurelog.core.network.utils.toCoordinateString
import com.desarrollodroide.adventurelog.core.model.VisitFormData
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
                "Failed to fetch adventures with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        val adventuresResponse = json.decodeFromString<AdventuresDTO>(responseText)
        
        logger.d { "📦 API Response - Fetched ${adventuresResponse.results?.size ?: 0} adventures for page $page (requested pageSize: $pageSize)" }
        logger.d { "   Total count: ${adventuresResponse.count}" }

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
        category: Category,
        rating: Double,
        link: String,
        location: String,
        latitude: String?,
        longitude: String?,
        isPublic: Boolean,
        visits: List<VisitFormData>,
        activityTypes: List<String>
    ): AdventureDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/adventures/"
        
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
            activityTypes = activityTypes
        )

        logger.d { "Creating adventure with request: name=$name, categoryId=${category.id}, isPublic=$isPublic, visits=${visits.size}" }

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
            } catch (e: Exception) {
                "Unable to read error body"
            }
            logger.e { "Failed to create adventure. Status: ${response.status}, Error: $errorBody" }
            throw HttpException(
                response.status.value,
                "Failed to create adventure with status: ${response.status}. Error: $errorBody"
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
        latitude: String?,
        longitude: String?,
        isPublic: Boolean?,
        visits: List<VisitFormData>?
    ): AdventureDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/adventures/$adventureId/"
        
        val updates = buildMap {
            name?.let { put("name", it) }
            description?.let { put("description", it) }
            categoryId?.let { 
                if (it.isNotBlank()) {
                    // For now, skip category updates until we understand the format
                    logger.d { "Category updates not implemented yet" }
                }
            }
            rating?.let { put("rating", it) }
            link?.let { put("link", it) }
            location?.let { put("location", it) }
            latitude.toCoordinateString()?.let { put("latitude", it) }
            longitude.toCoordinateString()?.let { put("longitude", it) }
            isPublic?.let { put("is_public", it) }
            visits?.let { 
                if (it.isNotEmpty()) {
                    put("visits", it.map { visit -> visit.toVisitRequest() })
                }
            }
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
}
