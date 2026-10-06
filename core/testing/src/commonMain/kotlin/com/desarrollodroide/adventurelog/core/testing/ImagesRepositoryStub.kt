package com.desarrollodroide.adventurelog.core.testing

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
// The interface's own package, wholesale: some of these declare their error types beside
// themselves rather than in core:model.
import com.desarrollodroide.adventurelog.core.domain.repository.*

/**
 * Everything a ImagesRepository has to answer, refusing by default.
 *
 * A test overrides the one call it is about. Anything else being reached is a mistake, and
 * throwing says so rather than quietly returning an empty list the assertion then passes on.
 */
abstract class ImagesRepositoryStub : ImagesRepository {
    override suspend fun uploadImage(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    ): Either<ApiResponse, Unit> = unused()
    override suspend fun deleteImage(imageId: String): Either<ApiResponse, Unit> = unused()
    override suspend fun setPrimaryImage(imageId: String): Either<ApiResponse, Unit> = unused()
}
