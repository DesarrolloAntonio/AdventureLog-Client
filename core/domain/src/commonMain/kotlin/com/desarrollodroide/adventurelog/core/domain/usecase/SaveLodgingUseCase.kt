package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Lodging

class SaveLodgingUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        lodgingId: String?,
        collectionId: String,
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
    ): Either<String, Lodging> {
        val result = if (lodgingId == null) {
            collectionsRepository.createLodging(
                collectionId, name, type, description, checkIn, checkOut, timezone,
                reservationNumber, price, priceCurrency, link, location, isPublic
            )
        } else {
            collectionsRepository.updateLodging(
                lodgingId, name, type, description, checkIn, checkOut, timezone,
                reservationNumber, price, priceCurrency, link, location, isPublic
            )
        }
        return when (result) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> Either.Left(result.value.lodgingMessage())
        }
    }
}

class DeleteLodgingUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(lodgingId: String): Either<String, Unit> =
        when (val result = collectionsRepository.deleteLodging(lodgingId)) {
            is Either.Right -> Either.Right(Unit)
            is Either.Left -> Either.Left(result.value.lodgingMessage())
        }
}

private fun ApiResponse.lodgingMessage(): String = when (this) {
    is ApiResponse.IOException -> CANT_REACH_SERVER
    is ApiResponse.HttpError -> "Could not save that, try again later"
    is ApiResponse.Forbidden -> "You don't have permission to do that."
    is ApiResponse.InvalidCredentials -> "Session expired, please log in again"
}
