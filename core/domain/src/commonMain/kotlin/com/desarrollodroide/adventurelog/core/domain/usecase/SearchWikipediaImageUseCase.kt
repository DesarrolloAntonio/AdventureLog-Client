package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.data.repository.WikipediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchWikipediaImageUseCase(
    private val wikipediaRepository: WikipediaRepository
) {
    operator fun invoke(query: String): Flow<WikipediaImageResult> = flow {
        println("SearchWikipediaImageUseCase: Starting search for '$query'")
        emit(WikipediaImageResult.Loading)
        
        wikipediaRepository.searchImage(query)
            .fold(
                onSuccess = { imageUrl ->
                    println("SearchWikipediaImageUseCase: Repository returned: $imageUrl")
                    if (imageUrl != null) {
                        emit(WikipediaImageResult.Success(imageUrl))
                    } else {
                        emit(WikipediaImageResult.NotFound)
                    }
                },
                onFailure = { exception ->
                    println("SearchWikipediaImageUseCase: Error: ${exception.message}")
                    exception.printStackTrace()
                    emit(WikipediaImageResult.Error(exception.message ?: "Unknown error"))
                }
            )
    }
}

sealed class WikipediaImageResult {
    object Loading : WikipediaImageResult()
    data class Success(val imageUrl: String) : WikipediaImageResult()
    object NotFound : WikipediaImageResult()
    data class Error(val message: String) : WikipediaImageResult()
}
