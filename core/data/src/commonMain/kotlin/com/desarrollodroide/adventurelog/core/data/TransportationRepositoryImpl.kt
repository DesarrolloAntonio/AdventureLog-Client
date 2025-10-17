package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.TransportationRepository
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork

class TransportationRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : TransportationRepository {
    
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
    ): Either<String, Transportation> {
        return try {
            val transportation = networkDataSource.createTransportation(
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
                distance = distance,
                originLatitude = originLatitude,
                originLongitude = originLongitude,
                destinationLatitude = destinationLatitude,
                destinationLongitude = destinationLongitude,
                isPublic = isPublic,
                images = images,
                attachments = attachments
            )
            Either.Right(transportation)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to create transportation")
        }
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
        attachments: List<String>
    ): Either<String, Transportation> {
        return try {
            val transportation = networkDataSource.updateTransportation(
                transportationId = transportationId,
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
                distance = distance,
                originLatitude = originLatitude,
                originLongitude = originLongitude,
                destinationLatitude = destinationLatitude,
                destinationLongitude = destinationLongitude,
                isPublic = isPublic,
                images = images,
                attachments = attachments
            )
            Either.Right(transportation)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to update transportation")
        }
    }
    
    override suspend fun getTransportation(transportationId: String): Either<String, Transportation> {
        return try {
            val transportation = networkDataSource.getTransportation(transportationId)
            Either.Right(transportation)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to get transportation")
        }
    }
    
    override suspend fun deleteTransportation(transportationId: String): Either<String, Unit> {
        return try {
            networkDataSource.deleteTransportation(transportationId)
            Either.Right(Unit)
        } catch (e: Exception) {
            Either.Left(e.message ?: "Failed to delete transportation")
        }
    }
}
