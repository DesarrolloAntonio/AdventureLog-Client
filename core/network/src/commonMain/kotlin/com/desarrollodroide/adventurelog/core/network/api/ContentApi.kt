package com.desarrollodroide.adventurelog.core.network.api

interface ContentApi {
    suspend fun generateDescription(name: String): String

    suspend fun uploadImage(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    )

    suspend fun deleteImage(imageId: String)

    /** The server refuses (400) an image that is already the primary one. */
    suspend fun setPrimaryImage(imageId: String)
}
