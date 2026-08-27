package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.CollectionApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.request.CreateCollectionRequest
import com.desarrollodroide.adventurelog.core.network.model.request.UpdateCollectionRequest
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import com.desarrollodroide.adventurelog.core.network.model.response.CollectionInviteDTO
import com.desarrollodroide.adventurelog.core.network.model.request.ArchiveCollectionRequest
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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

internal class KtorCollectionApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json = defaultJson
) : CollectionApi {

    private val logger = Logger.withTag("KtorCollectionApi")

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

    override suspend fun getCollections(page: Int, pageSize: Int): List<UltraSlimCollectionDTO> {
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
        
        try {
            val collectionsResponse = json.decodeFromString<UltraSlimCollectionsDTO>(responseText)
            logger.d { "Fetched ${collectionsResponse.results?.size ?: 0} collections" }
            return collectionsResponse.results ?: emptyList()
        } catch (e: Exception) {
            logJsonError("Collections JSON parse error", responseText, e)
            throw e
        }
    }
    
    override suspend fun getAllCollections(): List<UltraSlimCollectionDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/all/"
        
        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch all collections with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        
        try {
            val collections = json.decodeFromString<List<UltraSlimCollectionDTO>>(responseText)
            logger.d { "Fetched ${collections.size} collections from /all endpoint" }
            return collections
        } catch (e: Exception) {
            logJsonError("All collections JSON parse error", responseText, e)
            throw e
        }
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
        
        try {
            return json.decodeFromString<CollectionDTO>(responseText)
        } catch (e: Exception) {
            logJsonError("Collection detail JSON parse error", responseText, e)
            throw e
        }
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
        
        try {
            return json.decodeFromString<CollectionDTO>(responseText)
        } catch (e: Exception) {
            logJsonError("Create collection JSON parse error", responseText, e)
            throw e
        }
    }

    override suspend fun updateCollection(
        collectionId: String,
        name: String,
        description: String,
        isPublic: Boolean,
        startDate: String?,
        endDate: String?,
        link: String?
    ): CollectionDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/"
        
        val requestBody = UpdateCollectionRequest(
            name = name,
            description = description,
            isPublic = isPublic,
            startDate = startDate,
            endDate = endDate,
            link = link
        )

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to update collection with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        
        try {
            return json.decodeFromString<CollectionDTO>(responseText)
        } catch (e: Exception) {
            logJsonError("Update collection JSON parse error", responseText, e)
            throw e
        }
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

    override suspend fun duplicateCollection(collectionId: String): CollectionDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/duplicate/"

        logger.d { "🌐 API Request - POST $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody("{}")
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            logger.e { "Failed to duplicate collection: ${response.status} $body" }
            throw HttpException(
                response.status.value,
                "Failed to duplicate collection: ${response.status}"
            )
        }

        return json.decodeFromString<CollectionDTO>(response.body<String>())
    }

    override suspend fun setArchived(collectionId: String, archived: Boolean): CollectionDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$collectionId/"

        logger.d { "🌐 API Request - PATCH $url (archived=$archived)" }

        // A PATCH carrying only this field: everything else the collection holds is left alone.
        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(ArchiveCollectionRequest(isArchived = archived))
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to archive collection: ${response.status}"
            )
        }

        return json.decodeFromString<CollectionDTO>(response.body<String>())
    }

    override suspend fun getShareImage(collectionId: String, aspect: String): ByteArray =
        download("${sessionProvider().baseUrl}/api/collections/$collectionId/share-image/$aspect/", "share image")

    override suspend fun exportPdf(collectionId: String): ByteArray =
        download("${sessionProvider().baseUrl}/api/collections/$collectionId/export-pdf/", "PDF")

    override suspend fun exportZip(collectionId: String): ByteArray =
        download("${sessionProvider().baseUrl}/api/collections/$collectionId/export/", "export")

    override suspend fun getArchivedCollections(): List<UltraSlimCollectionDTO> =
        listOf("archived")

    override suspend fun getSharedCollections(): List<UltraSlimCollectionDTO> =
        listOf("shared")

    /** Both of these answer a bare array rather than a paged envelope. */
    private suspend fun listOf(action: String): List<UltraSlimCollectionDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$action/"

        logger.d { "🌐 API Request - GET $url" }

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch $action collections: ${response.status}"
            )
        }

        return json.decodeFromString<List<UltraSlimCollectionDTO>>(response.body<String>())
    }

    override suspend fun getInvites(): List<CollectionInviteDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/invites/"

        logger.d { "🌐 API Request - GET $url" }

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch invites: ${response.status}"
            )
        }

        return json.decodeFromString<List<CollectionInviteDTO>>(response.body<String>())
    }

    override suspend fun acceptInvite(collectionId: String) = respondToInvite(collectionId, true)

    override suspend fun declineInvite(collectionId: String) = respondToInvite(collectionId, false)

    override suspend fun shareCollection(collectionId: String, userUuid: String) =
        post("$collectionId/share/$userUuid", "share the collection")

    override suspend fun unshareCollection(collectionId: String, userUuid: String) =
        post("$collectionId/unshare/$userUuid", "stop sharing the collection")

    override suspend fun revokeInvite(collectionId: String, userUuid: String) =
        post("$collectionId/revoke-invite/$userUuid", "revoke the invitation")

    /**
     * The server answers these with its own message - "Invite already sent to this user", "Cannot
     * share with yourself" - which is more use than anything this layer could invent, so it is
     * carried out rather than replaced.
     */
    private suspend fun post(path: String, what: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/collections/$path/"

        logger.d { "🌐 API Request - POST $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody("{}")
        }

        if (!response.status.isSuccess()) {
            val body = try { response.body<String>() } catch (_: Exception) { "" }
            throw HttpException(response.status.value, serverError(body) ?: "Could not $what.")
        }
    }

    /** The `{"error": "..."}` these endpoints answer with, when they answer with one. */
    private fun serverError(body: String): String? = try {
        json.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.contentOrNull
    } catch (_: Exception) {
        null
    }

    private suspend fun respondToInvite(collectionId: String, accept: Boolean) {
        val session = sessionProvider()
        val verb = if (accept) "accept-invite" else "decline-invite"
        val url = "${session.baseUrl}/api/collections/$collectionId/$verb/"

        logger.d { "🌐 API Request - POST $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody("{}")
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to $verb: ${response.status}"
            )
        }
    }

    private suspend fun download(url: String, what: String): ByteArray {
        val session = sessionProvider()

        logger.d { "🌐 API Request - GET $url" }

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to build $what: ${response.status}"
            )
        }

        return response.bodyAsBytes()
    }
}
