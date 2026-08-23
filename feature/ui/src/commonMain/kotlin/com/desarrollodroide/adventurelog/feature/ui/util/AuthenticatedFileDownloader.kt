package com.desarrollodroide.adventurelog.feature.ui.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.isSuccess

/**
 * Fetches a file from the user's own server.
 *
 * Attachments sit behind the same auth check as location photos, so this goes through the client
 * that attaches the session token for the signed-in origin - the same one the image loader uses.
 * Anything opened with a plain request would come back 403.
 */
class AuthenticatedFileDownloader(
    private val client: HttpClient
) {
    suspend fun download(url: String): ByteArray? = try {
        val response = client.get(url)
        if (response.status.isSuccess()) response.bodyAsBytes() else null
    } catch (e: Exception) {
        null
    }
}
