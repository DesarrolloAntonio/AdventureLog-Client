package com.desarrollodroide.adventurelog.core.data

interface WikipediaRepository {
    suspend fun searchImage(query: String): Result<String?>
}