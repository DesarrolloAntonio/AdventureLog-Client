package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.UserApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

internal class KtorUserApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val json: Json = defaultJson
) : UserApi {

    private val logger = Logger.withTag("KtorUserApi")

    override suspend fun getUserDetails(): UserDetailsDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/auth/user-metadata/"
        
        logger.d { "🌐 API Request - GET $url" }
        
        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch user details with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<UserDetailsDTO>(responseText)
    }

    override suspend fun updateUserProfile(
        username: String?,
        email: String?,
        displayName: String?
    ): UserDetailsDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/auth/update-user/"
        
        val updates = buildMap {
            username?.let { put("username", it) }
            email?.let { put("email", it) }
            displayName?.let { put("display_name", it) }
        }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(updates)
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to update user profile with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        return json.decodeFromString<UserDetailsDTO>(responseText)
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Boolean {
        val session = sessionProvider()
        val url = "${session.baseUrl}/auth/change-password/"
        
        val body = mapOf(
            "current_password" to currentPassword,
            "new_password" to newPassword
        )

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(body)
        }

        return response.status.isSuccess()
    }

    override suspend fun uploadAvatar(imageData: ByteArray): String {
        // TODO: Implement avatar upload
        throw NotImplementedError("Avatar upload not implemented yet")
    }

    override suspend fun getUserStats(username: String): UserStatsDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/stats/counts/$username"
        
        logger.d { "🌐 API Request - GET $url" }
        
        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch user stats with status: ${response.status}"
            )
        }

        val responseText = response.body<String>()
        val statsDTO = json.decodeFromString<UserStatsDTO>(responseText)
        
        logger.d { "📦 API Response - User stats fetched successfully" }
        
        return statsDTO
    }
}
