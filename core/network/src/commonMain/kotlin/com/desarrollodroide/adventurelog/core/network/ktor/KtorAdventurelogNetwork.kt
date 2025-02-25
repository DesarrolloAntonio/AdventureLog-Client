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
        // TODO Implement login logic
        return UserDetailsDTO()
    }

    override suspend fun getUserDetails(csrfToken: String): UserDetailsDTO {
        return adventurelogClient.get("$BASE_URL$USER_DETAILS_ENDPOINT") {
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-CSRFToken", csrfToken)
            }
        }.body()
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