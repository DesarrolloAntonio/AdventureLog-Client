package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Lodging
import com.desarrollodroide.adventurelog.core.network.api.LodgingApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.model.request.LodgingRequest
import com.desarrollodroide.adventurelog.core.network.model.response.LodgingDTO
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
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

class KtorLodgingApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : LodgingApi {

    private val logger = Logger.withTag("KtorLodgingApi")

    private fun body(
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean,
        collectionId: String?
    ) = LodgingRequest(
        name = name,
        type = type,
        description = description,
        checkIn = checkIn?.takeIf { it.isNotBlank() },
        checkOut = checkOut?.takeIf { it.isNotBlank() },
        timezone = timezone?.takeIf { it.isNotBlank() },
        reservationNumber = reservationNumber,
        price = price?.takeIf { it.isNotBlank() },
        priceCurrency = priceCurrency?.takeIf { it.isNotBlank() },
        link = link,
        location = location,
        isPublic = isPublic,
        collection = collectionId
    )

    override suspend fun createLodging(
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean,
        collectionId: String
    ): Lodging {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/lodging/"
        logger.d { "Creating lodging at: $url" }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                body(
                    name, type, description, checkIn, checkOut, timezone,
                    reservationNumber, price, priceCurrency, link, location, isPublic, collectionId
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body<LodgingDTO>().toDomainModel()
        }
        logger.e { "Failed to create lodging: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to create the lodging: ${response.status}"
        )
    }

    override suspend fun updateLodging(
        lodgingId: String,
        name: String,
        type: String,
        description: String,
        checkIn: String?,
        checkOut: String?,
        timezone: String?,
        reservationNumber: String,
        price: String?,
        priceCurrency: String?,
        link: String,
        location: String,
        isPublic: Boolean
    ): Lodging {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/lodging/$lodgingId/"
        logger.d { "Updating lodging at: $url" }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                body(
                    name, type, description, checkIn, checkOut, timezone,
                    reservationNumber, price, priceCurrency, link, location, isPublic, null
                )
            )
        }

        if (response.status.isSuccess()) {
            return response.body<LodgingDTO>().toDomainModel()
        }
        logger.e { "Failed to update lodging: ${response.status}" }
        throw HttpException(
            response.status.value,
            "Failed to save the lodging: ${response.status}"
        )
    }

    override suspend fun deleteLodging(lodgingId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/lodging/$lodgingId/"
        logger.d { "Deleting lodging at: $url" }

        val response = httpClient.delete(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            logger.e { "Failed to delete lodging: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to delete the lodging: ${response.status}"
            )
        }
    }
}
