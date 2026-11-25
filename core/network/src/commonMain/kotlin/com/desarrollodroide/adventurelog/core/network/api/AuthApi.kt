package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.LoginOutcomeDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO

interface AuthApi {
    suspend fun login(
        username: String,
        password: String
    ): LoginOutcomeDTO

    /**
     * Finishes a sign-in that answered [LoginOutcomeDTO.SecondFactorRequired]: [code] is a TOTP or
     * recovery code, [pendingToken] the half-done session the login handed back.
     */
    suspend fun authenticateSecondFactor(
        pendingToken: String,
        code: String
    ): UserDetailsDTO

    /**
     * Trades a provider sign-in's one-time [code] and its PKCE [codeVerifier] for a session token
     * (`POST /api/auth/mobile/token/`). The answer names no user.
     */
    suspend fun redeemMobileCode(code: String, codeVerifier: String, redirectUri: String): String

    /** Ends [sessionToken]'s session on the server at [baseUrl]. */
    suspend fun logout(baseUrl: String, sessionToken: String)
}
