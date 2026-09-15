package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.ImagesRepository
import com.desarrollodroide.adventurelog.core.model.ContentImage

/**
 * Brings a place's photos on the server in line with the edit form: photos removed from the form
 * are deleted, and the one marked primary becomes primary. Photos added in the form are uploaded by
 * the caller, which holds their bytes; photos already on the server are never uploaded again.
 */
class SyncLocationImagesUseCase(
    private val imagesRepository: ImagesRepository
) {
    suspend operator fun invoke(
        existing: List<ContentImage>,
        keptIds: List<String>,
        primaryId: String?
    ): Either<String, Unit> {
        for (image in existing.filter { it.id !in keptIds }) {
            if (imagesRepository.deleteImage(image.id) is Either.Left) {
                return Either.Left("The place was saved, but a photo could not be removed.")
            }
        }
        val primary = existing.firstOrNull { it.id == primaryId }
        if (primary != null && !primary.isPrimary) {
            if (imagesRepository.setPrimaryImage(primary.id) is Either.Left) {
                return Either.Left("The place was saved, but the primary photo could not be changed.")
            }
        }
        return Either.Right(Unit)
    }
}
