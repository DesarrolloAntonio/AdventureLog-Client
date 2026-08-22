package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.network.api.TransportationApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.request.TransportationRequest
import com.desarrollodroide.adventurelog.core.network.model.response.TransportationDTO
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
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

/**
 * Talks to the server's `/api/transportations/` endpoints.
 *
 * The form hands every unset text field over as an empty string; the server rejects those for
 * nullable columns such as `date` and the timezone choices, so they are normalised to null here.
 */
class KtorTransportationApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json = defaultJson
) : TransportationApi {

    private val logger = Logger.withTag("KtorTransportationApi")

    override suspend fun createTransportation(
        name: String,
        type: String,
        description: String,
        rating: Double,
        link: String,
        fromLocation: String,
        toLocation: String,
        departureDate: String,
        arrivalDate: String,
        departureTimezone: String,
        arrivalTimezone: String,
        flightNumber: String,
        distance: String,
        originLatitude: String?,
        originLongitude: String?,
        destinationLatitude: String?,
        destinationLongitude: String?,
        isPublic: Boolean,
        images: List<String>,
        attachments: List<String>,
        collectionId: String?
    ): Transportation {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/transportations/"

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                buildRequest(
                    name = name,
                    type = type,
                    description = description,
                    rating = rating,
                    link = link,
                    fromLocation = fromLocation,
                    toLocation = toLocation,
                    departureDate = departureDate,
                    arrivalDate = arrivalDate,
                    departureTimezone = departureTimezone,
                    arrivalTimezone = arrivalTimezone,
                    flightNumber = flightNumber,
                    originLatitude = originLatitude,
                    originLongitude = originLongitude,
                    destinationLatitude = destinationLatitude,
                    destinationLongitude = destinationLongitude,
                    isPublic = isPublic,
                    collectionId = collectionId
                )
            )
        }

        return response.parseAs("create transportation")
    }

    override suspend fun updateTransportation(
        transportationId: String,
        name: String,
        type: String,
        description: String,
        rating: Double,
        link: String,
        fromLocation: String,
        toLocation: String,
        departureDate: String,
        arrivalDate: String,
        departureTimezone: String,
        arrivalTimezone: String,
        flightNumber: String,
        distance: String,
        originLatitude: String?,
        originLongitude: String?,
        destinationLatitude: String?,
        destinationLongitude: String?,
        isPublic: Boolean,
        images: List<String>,
        attachments: List<String>,
        collectionId: String?
    ): Transportation {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/transportations/$transportationId/"

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers { commonHeaders(session.sessionToken) }
            setBody(
                buildRequest(
                    name = name,
                    type = type,
                    description = description,
                    rating = rating,
                    link = link,
                    fromLocation = fromLocation,
                    toLocation = toLocation,
                    departureDate = departureDate,
                    arrivalDate = arrivalDate,
                    departureTimezone = departureTimezone,
                    arrivalTimezone = arrivalTimezone,
                    flightNumber = flightNumber,
                    originLatitude = originLatitude,
                    originLongitude = originLongitude,
                    destinationLatitude = destinationLatitude,
                    destinationLongitude = destinationLongitude,
                    isPublic = isPublic,
                    collectionId = collectionId
                )
            )
        }

        return response.parseAs("update transportation")
    }

    override suspend fun getTransportation(transportationId: String): Transportation {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/transportations/$transportationId/"

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        return response.parseAs("get transportation")
    }

    override suspend fun deleteTransportation(transportationId: String) {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/transportations/$transportationId/"

        val response = httpClient.delete(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to delete transportation with status: ${response.status}"
            )
        }
    }

    /**
     * `distance` is deliberately not sent - the server derives it from the coordinates (or from an
     * attached GPX track) and exposes it read-only.
     */
    private fun buildRequest(
        name: String,
        type: String,
        description: String,
        rating: Double,
        link: String,
        fromLocation: String,
        toLocation: String,
        departureDate: String,
        arrivalDate: String,
        departureTimezone: String,
        arrivalTimezone: String,
        flightNumber: String,
        originLatitude: String?,
        originLongitude: String?,
        destinationLatitude: String?,
        destinationLongitude: String?,
        isPublic: Boolean,
        collectionId: String?
    ) = TransportationRequest(
        type = type,
        name = name,
        description = description.ifBlank { null },
        rating = rating.takeIf { it > 0 },
        link = link.ifBlank { null },
        date = departureDate.ifBlank { null },
        endDate = arrivalDate.ifBlank { null },
        startTimezone = departureTimezone.ifBlank { null },
        endTimezone = arrivalTimezone.ifBlank { null },
        flightNumber = flightNumber.ifBlank { null },
        fromLocation = fromLocation.ifBlank { null },
        toLocation = toLocation.ifBlank { null },
        originLatitude = originLatitude?.toDoubleOrNull(),
        originLongitude = originLongitude?.toDoubleOrNull(),
        destinationLatitude = destinationLatitude?.toDoubleOrNull(),
        destinationLongitude = destinationLongitude?.toDoubleOrNull(),
        isPublic = isPublic,
        collection = collectionId?.ifBlank { null }
    )

    private suspend fun io.ktor.client.statement.HttpResponse.parseAs(
        action: String
    ): Transportation {
        val responseText = body<String>()

        if (!status.isSuccess()) {
            logger.e { "Failed to $action with status: $status. Body: $responseText" }
            throw HttpException(status.value, "Failed to $action with status: $status")
        }

        return try {
            json.decodeFromString<TransportationDTO>(responseText).toDomainModel()
        } catch (e: Exception) {
            logger.e(e) { "Failed to parse $action response" }
            throw e
        }
    }
}
