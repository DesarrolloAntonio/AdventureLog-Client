package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.AuthApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.ktor.redactSecrets
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

        if (response.status.isSuccess()) {
            val cookies = response.headers.getAll("Set-Cookie") ?: emptyList()

            var sessionToken: String? = null
            for (cookie in cookies) {
                if (cookie.contains("sessionid=")) {
                    val sessionidPattern = Regex("sessionid=([^;]+)")
                    val matchResult = sessionidPattern.find(cookie)
                    sessionToken = matchResult?.groupValues?.get(1)
                }
            }

            if (sessionToken == null) {
                // Pointing the app at the web frontend instead of the backend lands here: the
                // SvelteKit proxy strips set-cookie from everything it forwards, so login answers
                // 200 with no session at all.
                logger.w { "No session token found in cookies - is the server URL the backend?" }
            } else {
                // Never log the token itself - it grants full account access.
                logger.d { "Session token received (${sessionToken.length} chars)" }
                // Notify the parent about the new session token
                onSessionTokenReceived(sessionToken)
            }

            val responseBody = response.body<String>()

            val loginResponse = json.decodeFromString<LoginResponse>(responseBody)

            logger.d { "Login successful for user: ${loginResponse.data.user.username}" }

            return UserDetailsDTO(
                id = loginResponse.data.user.id,
                uuid = loginResponse.data.user.uuid ?: "",
                username = loginResponse.data.user.username,
                email = loginResponse.data.user.email,
                firstName = loginResponse.data.user.firstName,
                lastName = loginResponse.data.user.lastName,
                profilePic = loginResponse.data.user.profilePic,
                publicProfile = loginResponse.data.user.publicProfile ?: false,
                measurementSystem = loginResponse.data.user.measurementSystem,
                dateJoined = loginResponse.data.user.dateJoined,
                isStaff = loginResponse.data.user.isStaff ?: false,
                disablePassword = loginResponse.data.user.disablePassword ?: false,
                hasPassword = loginResponse.data.user.hasUsablePassword ?: true,
                sessionToken = sessionToken
            )
        } else {
            try {
                val errorBody = response.body<String>()
                logger.e {
                    "Login failed with status: ${response.status}. " +
                        "Error body: ${redactSecrets(errorBody)}"
                }
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
