package com.desarrollodroide.adventurelog.core.network.ktor.api

import com.desarrollodroide.adventurelog.core.model.ContentImage
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.network.api.TransportationApi
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import io.ktor.client.HttpClient
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Mock implementation of TransportationApi for development
 * TODO: Replace with actual API calls when backend is ready
 */
class KtorTransportationApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : TransportationApi {
    
    // In-memory storage for mock data
    private val transportations = mutableMapOf<String, Transportation>()
    private var nextId = 1
    
    @OptIn(ExperimentalTime::class)
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
        attachments: List<String>
    ): Transportation {
        val id = "transport_${nextId++}"
        val now = Clock.System.now().toString()
        
        val transportation = Transportation(
            id = id,
            user = "current_user",
            type = type,
            name = name,
            description = description.ifBlank { null },
            rating = if (rating > 0) rating else null,
            link = link.ifBlank { null },
            date = departureDate.ifBlank { null },
            endDate = arrivalDate.ifBlank { null },
            flightNumber = flightNumber.ifBlank { null },
            fromLocation = fromLocation.ifBlank { null },
            toLocation = toLocation.ifBlank { null },
            isPublic = isPublic,
            collection = null,
            createdAt = now,
            updatedAt = now,
            originLatitude = originLatitude,
            originLongitude = originLongitude,
            destinationLatitude = destinationLatitude,
            destinationLongitude = destinationLongitude,
            startTimezone = departureTimezone.ifBlank { null },
            endTimezone = arrivalTimezone.ifBlank { null },
            distance = distance.ifBlank { null },
            images = images.mapIndexed { index, imageUrl ->
                ContentImage(
                    id = "${id}_img_$index",
                    image = imageUrl,
                    isPrimary = index == 0,
                    user = "current_user",
                    immichId = null
                )
            }.ifEmpty { null },
            attachments = emptyList()
        )
        
        transportations[id] = transportation
        return transportation
    }
    
    @OptIn(ExperimentalTime::class)
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
        attachments: List<String>
    ): Transportation {
        val existing = transportations[transportationId]
            ?: throw IllegalArgumentException("Transportation not found: $transportationId")
        
        val updated = existing.copy(
            type = type,
            name = name,
            description = description.ifBlank { null },
            rating = if (rating > 0) rating else null,
            link = link.ifBlank { null },
            date = departureDate.ifBlank { null },
            endDate = arrivalDate.ifBlank { null },
            flightNumber = flightNumber.ifBlank { null },
            fromLocation = fromLocation.ifBlank { null },
            toLocation = toLocation.ifBlank { null },
            isPublic = isPublic,
            updatedAt = Clock.System.now().toString(),
            originLatitude = originLatitude,
            originLongitude = originLongitude,
            destinationLatitude = destinationLatitude,
            destinationLongitude = destinationLongitude,
            startTimezone = departureTimezone.ifBlank { null },
            endTimezone = arrivalTimezone.ifBlank { null },
            distance = distance.ifBlank { null },
            images = images.mapIndexed { index, imageUrl ->
                ContentImage(
                    id = "${transportationId}_img_$index",
                    image = imageUrl,
                    isPrimary = index == 0,
                    user = existing.user,
                    immichId = null
                )
            }.ifEmpty { null }
        )
        
        transportations[transportationId] = updated
        return updated
    }
    
    override suspend fun getTransportation(transportationId: String): Transportation {
        return transportations[transportationId]
            ?: throw IllegalArgumentException("Transportation not found: $transportationId")
    }
    
    override suspend fun deleteTransportation(transportationId: String) {
        if (!transportations.containsKey(transportationId)) {
            throw IllegalArgumentException("Transportation not found: $transportationId")
        }
        transportations.remove(transportationId)
    }
}
