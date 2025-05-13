package com.desarrollodroide.adventurelog.core.network

import com.desarrollodroide.adventurelog.core.network.model.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.UserDetailsDTO

interface AdventureLogNetworkDataSource {

    suspend fun getAdventures(page: Int): List<AdventureDTO>

    suspend fun getAdventureDetail(objectId: String): AdventureDTO

    suspend fun sendLogin(url: String, username: String, password: String): UserDetailsDTO

    suspend fun getCsrfToken(): Result<String>

    suspend fun getUserDetails(csrfToken: String): UserDetailsDTO
}