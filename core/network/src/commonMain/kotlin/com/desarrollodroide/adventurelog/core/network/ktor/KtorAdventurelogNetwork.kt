package com.desarrollodroide.adventurelog.core.network.ktor

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.model.AdventureDTO
import com.desarrollodroide.adventurelog.core.network.model.UserDetailsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.append
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class LoginResponse(
    val status: Int,
    val data: LoginData,
    val meta: LoginMeta
)

@Serializable
data class LoginData(
    val user: LoginUserData,
    val methods: List<LoginMethod>
)

@Serializable
data class LoginUserData(
    val id: Int,
    val display: String,
    val has_usable_password: Boolean,
    val email: String,
    val username: String
)

@Serializable
data class LoginMethod(
    val method: String,
    val at: Double,
    val username: String
)

@Serializable
data class LoginMeta(val is_authenticated: Boolean)

class KtorAdventurelogNetwork(
    private val adventurelogClient: HttpClient
) : AdventureLogNetworkDataSource {

    private val logger = Logger.withTag("KtorAdventurelogNetwork")
    
    companion object {
        private const val LOGIN_ENDPOINT = "auth/browser/v1/auth/login"
        private const val ADVENTURES_ENDPOINT = "api/adventures/"
        private const val USER_DETAILS_ENDPOINT = "api/user/details/"
    }

    override suspend fun getCsrfToken(): Result<String> = Result.success("")

    override suspend fun sendLogin(url: String, username: String, password: String): UserDetailsDTO {
        val loginClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                level = LogLevel.ALL
            }
        }
        
        // Normalize URL by removing trailing slash
        val baseUrl = url.trimEnd('/')
        val loginUrl = "$baseUrl/$LOGIN_ENDPOINT"
        logger.d { "Login URL: $loginUrl" }
        
        val response = loginClient.post(loginUrl) {
            contentType(ContentType.Application.Json)
            headers {
                append(HttpHeaders.Accept, "application/json")
                append("X-Is-Mobile", "true")
                append("Referer", baseUrl)
            }
            setBody(LoginRequest(username, password))
        }
        
        if (response.status.isSuccess()) {
            val loginResponse = response.body<LoginResponse>()
            
            return UserDetailsDTO(
                id = loginResponse.data.user.id,
                username = loginResponse.data.user.username,
                email = loginResponse.data.user.email,
                hasPassword = if (loginResponse.data.user.has_usable_password) "true" else "false"
            )
        } else {
            throw HttpException(response.status.value, "Login failed with status: ${response.status}")
        }
    }

    override suspend fun getUserDetails(csrfToken: String): UserDetailsDTO {
        // Implementation
        TODO("Not implemented yet")
    }

    override suspend fun getAdventures(page: Int): List<AdventureDTO> {
        // Implementation
        TODO("Not implemented yet")
    }

    override suspend fun getAdventureDetail(objectId: String): AdventureDTO {
        TODO("Not yet implemented")
    }
}

class HttpException(val code: Int, override val message: String) : Exception(message)