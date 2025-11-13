package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.ContentApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.model.response.WikipediaDescriptionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
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
    
    override suspend fun uploadImage(
        contentType: String,
        objectId: String,
        imageBytes: ByteArray,
        fileName: String
    ) {
        val sessionInfo = sessionProvider()
        val url = "${sessionInfo.baseUrl}/api/images/"
        logger.d { "Uploading image for $contentType: $objectId" }
        
        val response = httpClient.post(url) {
            headers {
                sessionInfo.sessionToken?.let { token ->
                    append("X-Session-Token", token)
                }
            }
            setBody(MultiPartFormDataContent(
                formData {
                    append("content_type", contentType)
                    append("object_id", objectId)
                    append("image", imageBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ))
        }
        
        if (!response.status.isSuccess()) {
            logger.e { "Failed to upload image with status: ${response.status}" }
            throw HttpException(
                response.status.value,
                "Failed to upload image with status: ${response.status}"
            )
        }
    }
}
