package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import com.desarrollodroide.adventurelog.core.network.api.ItineraryApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.request.AutoGenerateItineraryRequest
import com.desarrollodroide.adventurelog.core.network.model.request.ItineraryEntryRequest
import com.desarrollodroide.adventurelog.core.network.model.response.ItineraryEntryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class KtorItineraryApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : ItineraryApi {

    private val logger = Logger.withTag("KtorItineraryApi")

    @Serializable
    private data class AutoGenerateResponse(
        @SerialName("message") val message: String? = null,
        @SerialName("items") val items: List<ItineraryEntryDTO> = emptyList()
    )

    override suspend fun autoGenerateItinerary(collectionId: String): List<ItineraryEntry> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/itineraries/auto-generate/"
        logger.d { "Auto-generating an itinerary at: $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(AutoGenerateItineraryRequest(collectionId))
        }

        if (response.status.isSuccess()) {
            return response.body<AutoGenerateResponse>().items.mapNotNull { it.toDomainModel() }
        }
        logger.e { "Failed to auto-generate the itinerary: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to build the itinerary: ${response.status}"
        )
    }

    override suspend fun addItineraryEntry(
        collectionId: String,
        kind: ItineraryItemKind,
        itemId: String,
        date: String?,
        order: Int
    ): ItineraryEntry {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/itineraries/"
        logger.d { "Adding an itinerary entry at: $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                ItineraryEntryRequest(
                    collection = collectionId,
                    contentType = kind.wireName,
                    objectId = itemId,
                    date = date,
                    isGlobal = date == null,
                    order = order
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body<ItineraryEntryDTO>().toDomainModel()
                ?: throw HttpException(
                    response.status.value,
                    "The server placed the item but described it in a way this app does not know"
                )
        }
        logger.e { "Failed to add the itinerary entry: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to add it to the day: ${response.status}"
        )
    }

    override suspend fun deleteItineraryEntry(entryId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/itineraries/$entryId/"
        logger.d { "Deleting an itinerary entry at: $url" }

        val response = httpClient.delete(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            logger.e { "Failed to delete the itinerary entry: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to take it off the day: ${response.status}"
            )
        }
    }
}
