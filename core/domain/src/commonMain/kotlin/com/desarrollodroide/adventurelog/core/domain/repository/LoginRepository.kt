package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.LoginResult
import com.desarrollodroide.adventurelog.core.model.UserDetails

interface LoginRepository {

    suspend fun sendLogin(
        url: String,
        username: String,
        password: String
    ): Either<ApiResponse, LoginResult>

    /** Finishes a sign-in that answered [LoginResult.CodeRequired], on the server at [url]. */
    suspend fun sendLoginCode(
        url: String,
        code: String
    ): Either<LoginCodeError, UserDetails>

    /** Drops a sign-in that is waiting for its code. */
    fun cancelPendingLogin()
}

/** Why a second-factor code did not finish the sign-in. */
enum class LoginCodeError {
    /** Wrong or already used: another code may work. */
    IncorrectCode,

    /** The sign-in is gone (the server keeps it a few minutes): the password has to go again. */
    Expired,

    /** The server is throttling attempts. */
    TooManyAttempts,

    CannotReachServer,
    Failed,
}
