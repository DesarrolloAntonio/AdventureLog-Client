package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.network.api.VisitApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.mappers.toVisitRequest
import com.desarrollodroide.adventurelog.core.network.model.request.CreateVisitRequest
import com.desarrollodroide.adventurelog.core.network.model.response.VisitDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class KtorVisitApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json
) : VisitApi {

    private val logger = Logger.withTag("KtorVisitApi")

    override suspend fun createVisit(locationId: String, visit: VisitFormData): VisitDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/visits/"

        logger.d { "🌐 API Request - POST $url for location $locationId" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(CreateVisitRequest.from(locationId, visit.toVisitRequest()))
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            logger.e { "Failed to create visit: ${response.status} $body" }
            throw HttpException(response.status.value, "Failed to create visit: ${response.status}")
        }

        return json.decodeFromString<VisitDTO>(response.body<String>())
    }

    override suspend fun updateVisit(
        visitId: String,
        locationId: String,
        visit: VisitFormData
    ): VisitDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/visits/$visitId/"

        logger.d { "🌐 API Request - PATCH $url" }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            // The location is sent unchanged: the server refuses to move a visit between
            // locations, and omitting it entirely is fine on a PATCH.
            setBody(CreateVisitRequest.from(locationId, visit.toVisitRequest()))
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            logger.e { "Failed to update visit: ${response.status} $body" }
            throw HttpException(response.status.value, "Failed to update visit: ${response.status}")
        }

        return json.decodeFromString<VisitDTO>(response.body<String>())
    }

    override suspend fun deleteVisit(visitId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/visits/$visitId/"

        logger.d { "🌐 API Request - DELETE $url" }

        val response = httpClient.delete(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(response.status.value, "Failed to delete visit: ${response.status}")
        }
    }
}
