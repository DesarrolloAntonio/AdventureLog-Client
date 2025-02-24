package com.desarrollodroide.adventurelog.core.data

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.toDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

class LoginRepositoryImpl(
    private val adventureLogNetworkDataSource: AdventureLogNetworkDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : LoginRepository {
    private val logger = Logger.withTag("LoginRepositoryImpl")

    override suspend fun sendLogin(
        username: String,
        password: String
    ): Either<ApiResponse, UserDetails> =
        withContext(ioDispatcher) {
            try {
                // Get CSRF token first
                val token = adventureLogNetworkDataSource.getCsrfToken()
                    .getOrElse {
                        logger.e { "Error getting CSRF token: ${it.message}" }
                        return@withContext Either.Left(ApiResponse.InvalidCsrfToken)
                    }

                // Then perform login with the token
                try {
                    val userDetailsDTO = adventureLogNetworkDataSource.sendLogin(
                        username = username,
                        password = password,
                        token = token
                    )
                    Either.Right(userDetailsDTO.toDomainModel())
                } catch (e: HttpException) {
                    logger.e { "HTTP Error during login: ${e.code}" }
                    when (e.code) {
                        401 -> Either.Left(ApiResponse.InvalidCredentials)
                        403 -> Either.Left(ApiResponse.InvalidCsrfToken)
                        else -> Either.Left(ApiResponse.HttpError)
                    }
                }
            } catch (e: IOException) {
                logger.e(e) { "IO Error during login process" }
                Either.Left(ApiResponse.IOException)
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error during login process" }
                Either.Left(ApiResponse.HttpError)
            }
        }
}