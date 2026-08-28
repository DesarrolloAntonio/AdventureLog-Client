package com.desarrollodroide.adventurelog.core.data

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LoginRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.io.IOException

class LoginRepositoryImpl(
    private val adventureLogNetworkDataSource: AdventureLogNetwork,
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
                // The login response omits first/last name, so the greeting would read as the
                // username until the next app start. One extra GET buys a correct name now.
                val details = userDetailsDTO.toDomainModel(url)
                val named = try {
                    adventureLogNetworkDataSource.getUserDetails()
                        .toDomainModel(url)
                        .copy(sessionToken = details.sessionToken)
                } catch (e: Exception) {
                    logger.w { "Could not load user metadata after login: ${e.message}" }
                    details
                }
                Either.Right(named)
            } catch (e: HttpException) {
                logger.e { "HTTP Error during login: ${e.code}" }
                when (e.code) {
                    // allauth answers a wrong username or password with 400 and the code
                    // username_password_mismatch, not 401 - so the commonest mistake anyone can
                    // make was landing on "try again later", which suggests waiting rather than
                    // checking what you typed.
                    400 -> Either.Left(ApiResponse.InvalidCredentials)
                    401 -> Either.Left(ApiResponse.InvalidCredentials)
                    403 -> Either.Left(ApiResponse.InvalidCredentials)
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