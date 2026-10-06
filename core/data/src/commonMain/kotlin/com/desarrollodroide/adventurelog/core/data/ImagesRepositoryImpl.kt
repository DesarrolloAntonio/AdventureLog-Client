package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.ImagesRepository
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import kotlinx.io.IOException

class ImagesRepositoryImpl(
    private val networkDataSource: AdventureLogNetwork
) : ImagesRepository {

    override suspend fun uploadImage(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    ): Either<ApiResponse, Unit> {
        return try {
            networkDataSource.uploadImage(
                contentType = contentType,
                objectId = objectId,
                imageBytes = imageBytes,
                fileName = fileName
            )
            Either.Right(Unit)
        } catch (e: IOException) {
            Either.Left(ApiResponse.IOException)
        } catch (e: HttpException) {
            when (e.code) {
                401 -> Either.Left(ApiResponse.InvalidCredentials)
                403 -> Either.Left(ApiResponse.Forbidden)
                else -> Either.Left(ApiResponse.HttpError)
            }
        } catch (e: Exception) {
            Either.Left(ApiResponse.HttpError)
        }
    }

    override suspend fun deleteImage(imageId: String): Either<ApiResponse, Unit> =
        call { networkDataSource.deleteImage(imageId) }

    override suspend fun setPrimaryImage(imageId: String): Either<ApiResponse, Unit> =
        call { networkDataSource.setPrimaryImage(imageId) }

    private suspend fun call(block: suspend () -> Unit): Either<ApiResponse, Unit> = try {
        block()
        Either.Right(Unit)
    } catch (e: IOException) {
        Either.Left(ApiResponse.IOException)
    } catch (e: HttpException) {
        when (e.code) {
            401 -> Either.Left(ApiResponse.InvalidCredentials)
            403 -> Either.Left(ApiResponse.Forbidden)
            else -> Either.Left(ApiResponse.HttpError)
        }
    } catch (e: Exception) {
        Either.Left(ApiResponse.HttpError)
    }
}
