package com.desarrollodroide.adventurelog.core.network.datasource

interface WikipediaNetworkDataSource {
    suspend fun searchImage(query: String): String?
}

