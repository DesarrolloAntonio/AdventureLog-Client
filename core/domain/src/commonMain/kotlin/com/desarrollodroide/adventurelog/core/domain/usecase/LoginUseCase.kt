package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.domain.repository.LoginRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails

class LoginUseCase(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(url: String, username: String, password: String): Either<String, UserDetails> =
        when (val result = loginRepository.sendLogin(url, username, password)) {
            is Either.Left -> {
                when (result.value) {
                    is ApiResponse.IOException -> Either.Left("Network unavailable")
                    is ApiResponse.HttpError -> Either.Left("Error getting user credentials, try again later")
                    ApiResponse.InvalidCredentials -> Either.Left("Invalid username or password")
                }
            }

            is Either.Right -> {
                val user = result.value
                // A login can answer 200, name the user, and still carry no session: the
                // AdventureLog web app forwards the request to the API but strips set-cookie
                // from what it forwards back. Taken at face value that reads as success, so the
                // app used to open Home and save the credentials, and only there - once every
                // request came back 401 - admit that nobody was signed in. Without a session
                // there is no session, whatever the status code says.
                if (user.sessionToken.isNullOrBlank()) {
                    Either.Left(
                        "Signed in, but the server sent no session. This usually means the " +
                            "address points at the AdventureLog web app rather than its API - " +
                            "check that you are using the backend server's address and port."
                    )
                } else {
                    Either.Right(user)
                }
            }
        }
}
