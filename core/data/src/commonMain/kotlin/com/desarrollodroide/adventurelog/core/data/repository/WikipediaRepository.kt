package com.desarrollodroide.adventurelog.core.data.repository

import com.desarrollodroide.adventurelog.core.network.datasource.WikipediaDataSource

interface WikipediaRepository {
    suspend fun searchImage(query: String): Result<String?>
}

class WikipediaRepositoryImpl(
    private val wikipediaDataSource: WikipediaDataSource
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
