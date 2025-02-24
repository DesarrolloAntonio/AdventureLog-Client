package com.desarrollodroide.adventurelog.core.network.ktor

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.UserDetailsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.append
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.formUrlEncode
import io.ktor.http.headers
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val ADVENTURELOG_HOST = "192.168.1.27:8016"
const val ADVENTURELOG_PATH = "api/"
const val HAS_IMAGE = "hasImage"

class KtorAdventurelogNetwork(
    private val adventurelogClient: HttpClient,
) : AdventureLogNetworkDataSource {

    private val logger = Logger.withTag("KtorAdventurelogNetwork")
    private var csrfToken: String? = null
    private var sessionId: String? = null

    companion object {
        private const val BASE_URL = "http://$ADVENTURELOG_HOST"
        private const val CSRF_ENDPOINT = "/csrf/"
        private const val LOGIN_ENDPOINT = "/accounts/login/"
        private const val ADVENTURES_ENDPOINT = "/api/adventures/"
        private const val USER_DETAILS_ENDPOINT = "/api/user/details/"
    }

    override suspend fun getCsrfToken(): Result<String> = runCatching {
        val response = adventurelogClient.get("$BASE_URL$CSRF_ENDPOINT") {
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
        }

        logger.d { "CSRF Response Headers: ${response.headers.entries()}" }
        logger.d { "CSRF Response Body: ${response.body<String>()}" }

        response.headers[HttpHeaders.SetCookie]?.let { cookies ->
            logger.d { "Raw cookies received: $cookies" }

            cookies.split(";").forEach { cookie ->
                if (cookie.startsWith("csrftoken=")) {
                    csrfToken = cookie.substringAfter("csrftoken=").trim()
                    logger.d { "Extracted CSRF token from cookie: $csrfToken" }
                }
            }
        }

        csrfToken ?: throw IllegalStateException("No CSRF token received in cookies")
    }

    override suspend fun sendLogin(username: String, password: String, token: String): UserDetailsDTO {
        logger.d { "Starting login request with token: $token" }

        val parameters = Parameters.build {
            append("login", username)
            append("password", password)
        }

        try {
            logger.d { "Attempting POST request to $LOGIN_ENDPOINT" }

            val loginResponse = adventurelogClient.post("$BASE_URL$LOGIN_ENDPOINT") {
                contentType(ContentType.Application.FormUrlEncoded)
                headers {
                    append(HttpHeaders.Accept, "application/json")
                    append("X-CSRFToken", token)
                    append(HttpHeaders.Cookie, "csrftoken=$token")
                    append(HttpHeaders.UserAgent, "Mozilla/5.0 (Android 10; Mobile)")
                }
                setBody(FormDataContent(parameters))
            }

            logger.d { "POST request completed. Status: ${loginResponse.status}" }
            logger.d { "Response headers: ${loginResponse.headers.entries()}" }

            if (loginResponse.status != HttpStatusCode.OK) {
                throw IllegalStateException("Login failed with status: ${loginResponse.status}")
            }

            val sessionId = loginResponse.headers[HttpHeaders.SetCookie]?.let { cookies ->
                logger.d { "Processing cookies: $cookies" }
                cookies.split(";").find { it.startsWith("sessionid=") }?.substringAfter("sessionid=")?.trim()
            } ?: throw IllegalStateException("No session ID received")

            logger.d { "Session ID extracted: $sessionId" }
            this.sessionId = sessionId

            logger.d { "Making request to user details endpoint: $USER_DETAILS_ENDPOINT" }
            val userDetailsResponse = adventurelogClient.get("$BASE_URL$USER_DETAILS_ENDPOINT") {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                    append(HttpHeaders.Cookie, "sessionid=$sessionId; csrftoken=$token")
                }
            }

            logger.d { "User details response received. Status: ${userDetailsResponse.status}" }
            val userDetails = userDetailsResponse.body<UserDetailsDTO>()
            logger.d { "User details parsed successfully: $userDetails" }

            return userDetails

        } catch (e: Exception) {
            logger.e(e) { "Error during login request: ${e.message}" }
            throw e
        }
    }

    override suspend fun getAdventures(page: Int): List<AdventureDTO> {
        return adventurelogClient.get("$BASE_URL$ADVENTURES_ENDPOINT") {
            parameter("page", page)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-CSRFToken", csrfToken ?: "")
                append(HttpHeaders.Cookie, "sessionid=$sessionId; csrftoken=${csrfToken ?: ""}")
            }
        }.body()
    }

    override suspend fun getAdventureDetail(objectId: String): AdventureDTO {
        TODO("Not yet implemented")
    }

    private fun HeadersBuilder.appendAuthHeaders() {
        csrfToken?.let { token ->
            append("X-CSRFToken", token)
            append(HttpHeaders.Cookie, "sessionid=$sessionId; csrftoken=$token")
        }
    }
}


class HttpException(val code: Int, override val message: String) : Exception(message)