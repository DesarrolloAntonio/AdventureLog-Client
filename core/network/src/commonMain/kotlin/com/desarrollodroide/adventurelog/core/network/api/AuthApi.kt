package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO

interface AuthApi {
    suspend fun login(
        username: String,
        password: String
    ): UserDetailsDTO
}
