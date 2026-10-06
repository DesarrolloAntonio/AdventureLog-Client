package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.Collection

/**
 * One note, checklist or lodging as the server has it now, read from its collection.
 *
 * The edit forms were filled from a copy carried in the navigation route, so they saved that copy
 * back over anything changed elsewhere since the collection was opened (measured).
 */
class GetCollectionItemUseCase(
    private val getCollectionDetailUseCase: GetCollectionDetailUseCase
) {
    suspend operator fun <T : Any> invoke(
        collectionId: String,
        pick: (Collection) -> T?
    ): Either<String, T> = when (val result = getCollectionDetailUseCase(collectionId)) {
        is Either.Left -> Either.Left(result.value)
        is Either.Right -> pick(result.value)?.let { Either.Right(it) }
            ?: Either.Left("This is no longer in the collection.")
    }
}
