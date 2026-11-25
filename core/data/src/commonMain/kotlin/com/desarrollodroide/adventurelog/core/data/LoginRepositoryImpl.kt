package com.desarrollodroide.adventurelog.core.data

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LoginCodeError
import com.desarrollodroide.adventurelog.core.domain.repository.LoginRepository
import com.desarrollodroide.adventurelog.core.model.LoginResult
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.model.response.LoginOutcomeDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
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
    ): Either<ApiResponse, LoginResult> =
        withContext(ioDispatcher) {
            try {
                when (val outcome = adventureLogNetworkDataSource.sendLogin(
                    url = url,
                    username = username,
                    password = password
                )) {
                    is LoginOutcomeDTO.SignedIn -> Either.Right(LoginResult.SignedIn(named(outcome.user, url)))
                    LoginOutcomeDTO.SecondFactorRequired -> Either.Right(LoginResult.CodeRequired)
                }
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

    override suspend fun sendLoginCode(
        url: String,
        code: String
    ): Either<LoginCodeError, UserDetails> =
        withContext(ioDispatcher) {
            try {
                Either.Right(named(adventureLogNetworkDataSource.sendSecondFactorCode(code), url))
            } catch (e: HttpException) {
                logger.e { "HTTP Error sending the second factor: ${e.code}" }
                Either.Left(
                    when (e.code) {
                        // allauth: 400 incorrect_code; 401 once the login stage has timed out;
                        // 410 when the half-done session itself is gone.
                        400 -> LoginCodeError.IncorrectCode
                        401, 410 -> LoginCodeError.Expired
                        429 -> LoginCodeError.TooManyAttempts
                        else -> LoginCodeError.Failed
                    }
                )
            } catch (e: IOException) {
                logger.e(e) { "IO Error sending the second factor" }
                Either.Left(LoginCodeError.CannotReachServer)
            } catch (e: Exception) {
                logger.e(e) { "Unexpected error sending the second factor" }
                Either.Left(LoginCodeError.Failed)
            }
        }

    override fun cancelPendingLogin() = adventureLogNetworkDataSource.cancelPendingLogin()

    /**
     * The login response omits first/last name, so the greeting would read as the username until
     * the next app start. One extra GET buys a correct name now.
     */
    private suspend fun named(dto: UserDetailsDTO, url: String): UserDetails {
        val details = dto.toDomainModel(url)
        return try {
            adventureLogNetworkDataSource.getUserDetails()
                .toDomainModel(url)
                .copy(sessionToken = details.sessionToken)
        } catch (e: Exception) {
            logger.w { "Could not load user metadata after login: ${e.message}" }
            details
        }
    }
}
