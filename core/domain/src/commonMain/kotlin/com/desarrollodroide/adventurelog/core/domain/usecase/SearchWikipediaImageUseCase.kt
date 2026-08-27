package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.domain.repository.WikipediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("SearchWikipediaImageUseCase")

class SearchWikipediaImageUseCase(
    private val wikipediaRepository: WikipediaRepository
) {
    operator fun invoke(query: String): Flow<WikipediaImageResult> = flow {
        logger.d { "SearchWikipediaImageUseCase: Starting search for '$query'" }
        emit(WikipediaImageResult.Loading)
        
        wikipediaRepository.searchImage(query)
            .fold(
                onSuccess = { imageUrl ->
                    logger.d { "SearchWikipediaImageUseCase: Repository returned: $imageUrl" }
                    if (imageUrl != null) {
                        emit(WikipediaImageResult.Success(imageUrl))
                    } else {
                        emit(WikipediaImageResult.NotFound)
                    }
                },
                onFailure = { exception ->
                    logger.e { "SearchWikipediaImageUseCase: Error: ${exception.message}" }
                    exception.printStackTrace()
                    emit(WikipediaImageResult.Error(exception.message ?: "Unknown error"))
                }
            )
    }
}

sealed class WikipediaImageResult {
    object Idle : WikipediaImageResult()
    object Loading : WikipediaImageResult()
    data class Success(val imageUrl: String) : WikipediaImageResult()
    object NotFound : WikipediaImageResult()
    data class Error(val message: String) : WikipediaImageResult()
}
