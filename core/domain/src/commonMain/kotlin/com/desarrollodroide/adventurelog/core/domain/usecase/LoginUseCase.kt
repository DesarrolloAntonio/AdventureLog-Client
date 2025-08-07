package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.data.LoginRepository
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

            is Either.Right -> Either.Right(result.value)
        }
}