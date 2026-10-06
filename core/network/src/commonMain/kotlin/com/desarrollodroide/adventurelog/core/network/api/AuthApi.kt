package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO

interface AuthApi {
    suspend fun login(
        username: String,
        password: String
    ): UserDetailsDTO

    /** Ends [sessionToken]'s session on the server at [baseUrl]. */
    suspend fun logout(baseUrl: String, sessionToken: String)
}
