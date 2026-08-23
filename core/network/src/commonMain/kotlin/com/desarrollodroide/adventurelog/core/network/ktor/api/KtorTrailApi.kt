package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.TrailFormData
import com.desarrollodroide.adventurelog.core.network.api.TrailApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.request.TrailRequest
import com.desarrollodroide.adventurelog.core.network.model.response.TrailDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class KtorTrailApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json
) : TrailApi {

    private val logger = Logger.withTag("KtorTrailApi")

    override suspend fun createTrail(locationId: String, trail: TrailFormData): TrailDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/trails/"

        logger.d { "🌐 API Request - POST $url for location $locationId" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(TrailRequest.from(locationId, trail))
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            logger.e { "Failed to create trail: ${response.status} $body" }
            throw HttpException(response.status.value, "Failed to create trail: ${response.status}")
        }

        return json.decodeFromString<TrailDTO>(response.body<String>())
    }

    override suspend fun updateTrail(
        trailId: String,
        locationId: String,
        trail: TrailFormData
    ): TrailDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/trails/$trailId/"

        logger.d { "🌐 API Request - PATCH $url" }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(TrailRequest.from(locationId, trail))
        }

        if (!response.status.isSuccess()) {
            val body = runCatching { response.body<String>() }.getOrDefault("")
            logger.e { "Failed to update trail: ${response.status} $body" }
            throw HttpException(response.status.value, "Failed to update trail: ${response.status}")
        }

        return json.decodeFromString<TrailDTO>(response.body<String>())
    }

    override suspend fun deleteTrail(trailId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/trails/$trailId/"

        logger.d { "🌐 API Request - DELETE $url" }

        val response = httpClient.delete(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(response.status.value, "Failed to delete trail: ${response.status}")
        }
    }
}
