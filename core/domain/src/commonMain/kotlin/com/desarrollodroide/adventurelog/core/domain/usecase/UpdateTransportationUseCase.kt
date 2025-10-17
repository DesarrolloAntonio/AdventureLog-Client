package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.TransportationRepository
import com.desarrollodroide.adventurelog.core.model.Transportation

class UpdateTransportationUseCase(
    private val transportationRepository: TransportationRepository
) {
    suspend operator fun invoke(
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
        return transportationRepository.updateTransportation(
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
    }
}
