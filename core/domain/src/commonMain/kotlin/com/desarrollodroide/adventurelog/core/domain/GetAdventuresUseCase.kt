package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.model.Adventure

class GetAdventuresUseCase(
    private val adventuresRepository: AdventuresRepository
) {
    suspend operator fun invoke(page: Int, pageSize: Int): Either<String, List<Adventure>> =
        when (val result = adventuresRepository.getAdventures(page, pageSize)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("Network unavailable")
                    is ApiResponse.HttpError -> Either.Left("Error getting adventures, try again later")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired, please log in again")
                    is ApiResponse.InvalidCsrfToken -> Either.Left("Session expired, please try again")
                }
            }
            is Either.Right -> Either.Right(result.value)
        }
}
