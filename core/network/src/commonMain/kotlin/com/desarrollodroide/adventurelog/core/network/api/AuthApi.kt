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

    /** Ends [sessionToken]'s session on the server at [baseUrl]. */
    suspend fun logout(baseUrl: String, sessionToken: String)
}
