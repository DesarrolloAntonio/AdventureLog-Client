package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.SearchRepository
import com.desarrollodroide.adventurelog.core.model.SearchHit

class SearchEverythingUseCase(
    private val searchRepository: SearchRepository
) {
    /**
     * The server rejects a term shorter than two characters, so one is answered here with an
     * empty result rather than a round trip and an error message.
     */
    suspend operator fun invoke(query: String, limit: Int = 20): Either<String, List<SearchHit>> {
        val term = query.trim()
        if (term.length < MIN_LENGTH) return Either.Right(emptyList())

        return when (val result = searchRepository.search(term, limit)) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    is ApiResponse.IOException -> "No internet connection."
                    is ApiResponse.HttpError -> "Search is not answering. Please try again."
                    is ApiResponse.InvalidCredentials -> "Session expired. Please log in again."
                }
            )
            is Either.Right -> Either.Right(result.value)
        }
    }

    companion object {
        const val MIN_LENGTH = 2
    }
}
