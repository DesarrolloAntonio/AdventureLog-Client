package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import com.desarrollodroide.adventurelog.core.network.api.RecommendationApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.response.RecommendationsResponseDTO
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.isSuccess

class KtorRecommendationApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : RecommendationApi {

    private val logger = Logger.withTag("KtorRecommendationApi")

    override suspend fun getRecommendations(
        latitude: Double?,
        longitude: Double?,
        place: String?,
        category: RecommendationCategory,
        radiusMetres: Int
    ): List<Recommendation> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/recommendations/query/"
        logger.d { "Looking for recommendations at: $url" }

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
            // Coordinates win when both are given: they are exact, and the place name is only a
            // way of asking the server to work them out.
            if (latitude != null && longitude != null) {
                parameter("lat", latitude)
                parameter("lon", longitude)
            } else if (!place.isNullOrBlank()) {
                parameter("location", place)
            }
            parameter("category", category.wireName)
            parameter("radius", radiusMetres)
        }

        if (response.status.isSuccess()) {
            return response.body<RecommendationsResponseDTO>().results.mapNotNull {
                it.toDomainModel()
            }
        }
        logger.e { "Failed to get recommendations: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to look around there: ${response.status}"
        )
    }
}
