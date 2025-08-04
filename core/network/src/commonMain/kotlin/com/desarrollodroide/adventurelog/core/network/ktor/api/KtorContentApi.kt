package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.ContentApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.model.response.WikipediaDescriptionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class KtorContentApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo
) : ContentApi {
    
    private val logger = Logger.withTag("KtorContentApi")
    
    override suspend fun generateDescription(name: String): String {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/generate/desc/"
        logger.d { "Generating description for: $name" }
        
        val response = httpClient.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/json")
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
            parameter("name", name)
        }
        
        if (response.status.isSuccess()) {
            val wikipediaResponse = response.body<WikipediaDescriptionResponse>()
            return wikipediaResponse.extract ?: throw Exception("No description found")
        } else {
            logger.e { "Failed to generate description with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to generate description with status: ${response.status}"
            )
        }
    }
}
