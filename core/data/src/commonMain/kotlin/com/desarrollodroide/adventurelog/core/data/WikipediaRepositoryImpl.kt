package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.network.datasource.WikipediaNetworkDataSource

class WikipediaRepositoryImpl(
    private val wikipediaDataSource: WikipediaNetworkDataSource
) : WikipediaRepository {

    override suspend fun searchImage(query: String): Result<String?> {
        return try {
            val imageUrl = wikipediaDataSource.searchImage(query)
            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}