package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Checklist

/**
 * Saves a checklist and its lines in one call.
 *
 * The server takes the items inside the checklist body, so ticking a line is a save of the list
 * it belongs to - there is no per-item endpoint to reach for.
 */
class SaveChecklistUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        checklistId: String?,
        collectionId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Either<String, Checklist> {
        val result = if (checklistId == null) {
            collectionsRepository.createChecklist(collectionId, name, items, date, isPublic)
        } else {
            collectionsRepository.updateChecklist(checklistId, name, items, date, isPublic)
        }
        return when (result) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> Either.Left(result.value.checklistMessage())
        }
    }
}

class DeleteChecklistUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(checklistId: String): Either<String, Unit> =
        when (val result = collectionsRepository.deleteChecklist(checklistId)) {
            is Either.Right -> Either.Right(Unit)
            is Either.Left -> Either.Left(result.value.checklistMessage())
        }
}

private fun ApiResponse.checklistMessage(): String = when (this) {
    is ApiResponse.IOException -> "Network unavailable"
    is ApiResponse.HttpError -> "Could not save that, try again later"
    is ApiResponse.Forbidden -> "You don't have permission to do that."
    is ApiResponse.InvalidCredentials -> "Session expired, please log in again"
}
