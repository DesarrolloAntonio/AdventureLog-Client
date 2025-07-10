package com.desarrollodroide.adventurelog.core.data

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

class LoginRepositoryImpl(
    private val adventureLogNetworkDataSource: AdventureLogNetworkDataSource,
    private val ioDispatcher: CoroutineDispatcher
) : LoginRepository {
    private val logger = Logger.withTag("LoginRepositoryImpl")

    override suspend fun sendLogin(
        url: String,
        username: String,
        password: String
    ): Either<ApiResponse, UserDetails> =
        withContext(ioDispatcher) {
            try {
                val userDetailsDTO = adventureLogNetworkDataSource.sendLogin(
                    url = url,
                    username = username,
                    password = password
                )
                Either.Right(userDetailsDTO.toDomainModel(url))
            } catch (e: HttpException) {
                logger.e { "HTTP Error during login: ${e.code}" }
                when (e.code) {
                    401 -> Either.Left(ApiResponse.InvalidCredentials)
                    403 -> Either.Left(ApiResponse.InvalidCredentials)
                    404 -> Either.Left(ApiResponse.HttpError)
                    else -> Either.Left(ApiResponse.HttpError)
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