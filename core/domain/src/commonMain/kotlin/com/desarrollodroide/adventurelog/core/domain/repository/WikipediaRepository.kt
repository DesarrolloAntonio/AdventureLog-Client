package com.desarrollodroide.adventurelog.core.domain.repository

interface WikipediaRepository {
    suspend fun searchImage(query: String): Result<String?>
}