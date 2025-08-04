package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.AuthApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.request.LoginRequest
import com.desarrollodroide.adventurelog.core.network.model.request.LoginResponse
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class KtorAuthApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val onSessionTokenReceived: (String) -> Unit,
    private val json: Json = defaultJson
) : AuthApi {
    
    private val logger = Logger.withTag("KtorAuthApi")
    
    override suspend fun login(
        username: String,
        password: String
    ): UserDetailsDTO {
        val sessionInfo = sessionProvider()
        val loginUrl = "${sessionInfo.baseUrl}/auth/browser/v1/auth/login"
        logger.d { "Login URL: $loginUrl" }

        val response = httpClient.post(loginUrl) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-Is-Mobile", "true")
                append("Referer", sessionInfo.baseUrl)
            }
            setBody(LoginRequest(
                username = username,
                password = password
            ))
        }

        logger.d { "Login response status: ${response.status}" }
        logger.d { "Login response headers: ${response.headers.entries()}" }

        if (response.status.isSuccess()) {
            val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()
            logger.d { "Cookies from login response: $cookies" }

            var sessionToken: String? = null
            for (cookie in cookies) {
                if (cookie.contains("sessionid=")) {
                    val sessionidPattern = Regex("sessionid=([^;]+)")
                    val matchResult = sessionidPattern.find(cookie)
                    sessionToken = matchResult?.groupValues?.get(1)
                    logger.d { "Extracted sessionId from cookies: $sessionToken" }
                }
            }

            if (sessionToken == null) {
                logger.w { "No session token found in cookies" }
            } else {
                // Notify the parent about the new session token
                onSessionTokenReceived(sessionToken)
            }

            val responseBody = response.body<String>()
            logger.d { "Login response body: $responseBody" }

            val loginResponse = json.decodeFromString<LoginResponse>(responseBody)

            logger.d { "Login successful for user: ${loginResponse.data.user.username}" }

            return UserDetailsDTO(
                id = loginResponse.data.user.id,
                username = loginResponse.data.user.username,
                email = loginResponse.data.user.email,
                hasPassword = if (loginResponse.data.user.hasUsablePassword) "true" else "false",
                sessionToken = sessionToken
            )
        } else {
            try {
                val errorBody = response.body<String>()
                logger.e { "Login failed with status: ${response.status}. Error body: $errorBody" }
            } catch (e: Exception) {
                logger.e { "Login failed with status: ${response.status}. Could not read error body." }
            }

            throw HttpException(
                response.status.value,
                "Login failed with status: ${response.status}"
            )
        }
    }
}
