package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.ImagesRepository

class UploadImageUseCase(
    private val imagesRepository: ImagesRepository
) {
    suspend operator fun invoke(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    ): Either<String, Unit> {
        if (imageBytes.isEmpty()) {
            return Either.Left("Image data is empty")
        }

        return when (val result = imagesRepository.uploadImage(
            contentType = contentType,
            objectId = objectId,
            imageBytes = imageBytes,
            fileName = fileName
        )) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("No internet connection. Please check your network.")
                    is ApiResponse.HttpError -> Either.Left("Failed to upload image. Please try again.")
                    is ApiResponse.InvalidCredentials -> Either.Left("Session expired. Please log in again.")
                }
            }
            is Either.Right -> Either.Right(Unit)
        }
    }
}