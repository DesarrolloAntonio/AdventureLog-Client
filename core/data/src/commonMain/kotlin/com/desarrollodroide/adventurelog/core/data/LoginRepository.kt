package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.model.UserDetails

interface LoginRepository {

    suspend fun sendLogin(
        username: String,
        password: String
    ): Either<ApiResponse, UserDetails>

}
