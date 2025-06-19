package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.CollectionApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.request.CreateCollectionRequest
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionsDTO
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

internal class KtorCollectionApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json = defaultJson
) : CollectionApi {

    private val logger = Logger.withTag("KtorCollectionApi")

    override suspend fun getCollections(page: Int, pageSize: Int): List<CollectionDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/"
        
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
                "Failed to fetch collections with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        val collectionsResponse = json.decodeFromString<CollectionsDTO>(responseText)
        
        logger.d { "Fetched ${collectionsResponse.results?.size ?: 0} collections" }
        return collectionsResponse.results ?: emptyList()
    }

    override suspend fun getCollectionDetail(collectionId: String): CollectionDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/"
        
        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch collection detail with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<CollectionDTO>(responseText)
    }

    override suspend fun createCollection(
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?
    ): CollectionDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/"
        
        val requestBody = CreateCollectionRequest(
            name = name,
            description = description,
            isPublic = isPublic,
            startDate = startDate,
            endDate = endDate
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
                "Failed to create collection with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<CollectionDTO>(responseText)
    }

    override suspend fun updateCollection(
        collectionId: String,
        name: String?,
        description: String?,
        isPublic: Boolean?,
        startDate: String?,
        endDate: String?
    ): CollectionDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/"
        
        val updates = buildMap {
            name?.let { put("name", it) }
            description?.let { put("description", it) }
            isPublic?.let { put("is_public", it) }
            startDate?.let { put("start_date", it) }
            endDate?.let { put("end_date", it) }
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
                "Failed to update collection with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<CollectionDTO>(responseText)
    }

    override suspend fun deleteCollection(collectionId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/"
        
        val response = httpClient.delete(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to delete collection with status: ${response.status}"
            )
        }
    }

    override suspend fun addAdventureToCollection(collectionId: String, adventureId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/adventures/$adventureId/"
        
        val response = httpClient.post(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to add adventure to collection with status: ${response.status}"
            )
        }
    }

    override suspend fun removeAdventureFromCollection(collectionId: String, adventureId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/adventures/$adventureId/"
        
        val response = httpClient.delete(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to remove adventure from collection with status: ${response.status}"
            )
        }
    }
}
