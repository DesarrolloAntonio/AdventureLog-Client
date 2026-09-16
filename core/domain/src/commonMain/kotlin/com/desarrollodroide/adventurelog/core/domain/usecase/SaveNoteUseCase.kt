package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.model.Note

/** Creates a note, or saves an existing one when [noteId] is given. */
class SaveNoteUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(
        noteId: String?,
        collectionId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Either<String, Note> {
        val result = if (noteId == null) {
            collectionsRepository.createNote(collectionId, name, content, date, isPublic)
        } else {
            collectionsRepository.updateNote(noteId, name, content, date, isPublic)
        }
        return when (result) {
            is Either.Right -> Either.Right(result.value)
            is Either.Left -> Either.Left(result.value.message())
        }
    }
}

class DeleteNoteUseCase(
    private val collectionsRepository: CollectionsRepository
) {
    suspend operator fun invoke(noteId: String): Either<String, Unit> =
        when (val result = collectionsRepository.deleteNote(noteId)) {
            is Either.Right -> Either.Right(Unit)
            is Either.Left -> Either.Left(result.value.message())
        }
}

private fun ApiResponse.message(): String = when (this) {
    is ApiResponse.IOException -> "Network unavailable"
    is ApiResponse.HttpError -> "Could not save that, try again later"
    is ApiResponse.Forbidden -> "You don't have permission to do that."
    is ApiResponse.InvalidCredentials -> "Session expired, please log in again"
}
