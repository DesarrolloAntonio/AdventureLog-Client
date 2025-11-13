package com.desarrollodroide.adventurelog.core.network.api

interface ContentApi {
    suspend fun generateDescription(name: String): String

    suspend fun uploadImage(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    )
}
