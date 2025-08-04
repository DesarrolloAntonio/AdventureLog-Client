package com.desarrollodroide.adventurelog.core.network.api

interface ContentApi {
    suspend fun generateDescription(name: String): String
}
