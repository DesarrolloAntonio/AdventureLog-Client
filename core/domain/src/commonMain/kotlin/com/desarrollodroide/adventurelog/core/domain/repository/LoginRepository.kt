package com.desarrollodroide.adventurelog.core.domain.repository

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.LoginResult
import com.desarrollodroide.adventurelog.core.model.ProviderSignInOptions
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

    /**
     * The providers the server at [url] signs the app in through, or null when it cannot (a
     * 0.13.1 server, none set up, no answer): the screen then offers only the password.
     */
    suspend fun getSignInProviders(url: String): ProviderSignInOptions?

    /** Trades a provider sign-in's one-time code for a session on the server at [url]. */
    suspend fun redeemProviderSignIn(
        url: String,
        code: String,
        codeVerifier: String,
        redirectUri: String,
    ): Either<ProviderSignInError, UserDetails>
}

/** Why the code a provider sign-in brought back did not sign anyone in. */
enum class ProviderSignInError {
    /** Wrong, already used, or older than its minute: the sign-in has to start again. */
    CodeRejected,
    TooManyAttempts,
    CannotReachServer,
    Failed,
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
