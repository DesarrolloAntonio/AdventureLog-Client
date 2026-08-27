package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.UserApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.model.response.DashboardDTO
import com.desarrollodroide.adventurelog.core.network.model.response.EmailAddressDTO
import com.desarrollodroide.adventurelog.core.network.model.response.EmailAddressListDTO
import com.desarrollodroide.adventurelog.core.network.model.response.MediaUsageDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserStatsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.desarrollodroide.adventurelog.core.network.model.response.CalendarEventsDTO
import io.ktor.client.request.parameter

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
        firstName: String?,
        lastName: String?,
        publicProfile: Boolean?,
        measurementSystem: String?,
        defaultCurrency: String?,
        mapStyle: String?
    ): UserDetailsDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/auth/update-user/"

        // A JsonObject rather than a Map<String, Any>: the values are of mixed types and Ktor's
        // content negotiation cannot serialise a heterogeneous map.
        val updates = buildJsonObject {
            username?.let { put("username", JsonPrimitive(it)) }
            firstName?.let { put("first_name", JsonPrimitive(it)) }
            lastName?.let { put("last_name", JsonPrimitive(it)) }
            publicProfile?.let { put("public_profile", JsonPrimitive(it)) }
            measurementSystem?.let { put("measurement_system", JsonPrimitive(it)) }
            defaultCurrency?.let { put("default_currency", JsonPrimitive(it)) }
            mapStyle?.let { put("map_style", JsonPrimitive(it)) }
        }

        logger.d { "🌐 API Request - PATCH $url (${updates.keys.joinToString()})" }

        val response = httpClient.patch(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
            }
            setBody(updates)
        }

        val responseText = response.body<String>()

        if (!response.status.isSuccess()) {
            // DRF answers {"field": ["message"]} - surfacing the field's own message is the
            // difference between "Update failed" and "A user with that username already exists."
            throw HttpException(response.status.value, firstFieldError(responseText)
                ?: "Failed to update user profile with status: ${response.status}")
        }

        return json.decodeFromString<UserDetailsDTO>(responseText)
    }

    /**
     * The first message out of a DRF field-error body, or null if the body is not one.
     */
    private fun firstFieldError(body: String): String? = try {
        json.parseToJsonElement(body).jsonObject.values
            .firstNotNullOfOrNull { value ->
                when (value) {
                    is JsonArray -> value.firstOrNull()?.jsonPrimitive?.contentOrNull
                    is JsonPrimitive -> value.contentOrNull
                    else -> null
                }
            }
    } catch (e: Exception) {
        null
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Boolean {
        val session = sessionProvider()
        // allauth headless endpoint. The old /auth/change-password/ route does not exist on the
        // server and answered 404 for every call.
        val url = "${session.baseUrl}/auth/browser/v1/account/password/change"

        // Accounts created through a social provider have no usable password; allauth rejects the
        // request if current_password is sent as an empty string, so omit the key entirely.
        val body = buildMap {
            if (currentPassword.isNotBlank()) {
                put("current_password", currentPassword)
            }
            put("new_password", newPassword)
        }

        val response = httpClient.post(url) {
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
                append("Referer", session.baseUrl)
            }
            setBody(body)
        }

        if (!response.status.isSuccess()) {
            logger.e { "Password change failed with status: ${response.status}" }
        }

        return response.status.isSuccess()
    }

    override suspend fun uploadAvatar(imageData: ByteArray): String {
        // TODO: Implement avatar upload
        throw NotImplementedError("Avatar upload not implemented yet")
    }

    override suspend fun getMediaUsage(): MediaUsageDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/auth/user-media-usage/"

        logger.d { "🌐 API Request - GET $url" }

        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch media usage with status: ${response.status}"
            )
        }

        return json.decodeFromString<MediaUsageDTO>(response.body<String>())
    }

    override suspend fun getEmailAddresses(): List<EmailAddressDTO> {
        val response = httpClient.get(emailUrl()) {
            headers {
                commonHeaders(sessionProvider().sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch email addresses with status: ${response.status}"
            )
        }

        return json.decodeFromString<EmailAddressListDTO>(response.body<String>()).data
    }

    override suspend fun addEmailAddress(email: String) {
        emailRequest(HttpMethod.Post, buildJsonObject { put("email", JsonPrimitive(email)) })
    }

    override suspend fun requestEmailVerification(email: String) {
        emailRequest(HttpMethod.Put, buildJsonObject { put("email", JsonPrimitive(email)) })
    }

    override suspend fun setPrimaryEmailAddress(email: String) {
        emailRequest(
            HttpMethod.Patch,
            buildJsonObject {
                put("email", JsonPrimitive(email))
                put("primary", JsonPrimitive(true))
            }
        )
    }

    override suspend fun removeEmailAddress(email: String) {
        emailRequest(HttpMethod.Delete, buildJsonObject { put("email", JsonPrimitive(email)) })
    }

    private fun emailUrl() = "${sessionProvider().baseUrl}/auth/browser/v1/account/email"

    /**
     * allauth's headless email endpoint is one URL that switches on the HTTP verb, and it rejects
     * any session-bearing request without a same-origin Referer.
     */
    private suspend fun emailRequest(method: HttpMethod, body: JsonObject) {
        val session = sessionProvider()
        val url = emailUrl()

        logger.d { "🌐 API Request - ${method.value} $url" }

        val response = httpClient.request(url) {
            this.method = method
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(session.sessionToken)
                append("Referer", session.baseUrl)
            }
            setBody(body)
        }

        if (!response.status.isSuccess()) {
            val text = response.body<String>()
            throw HttpException(
                response.status.value,
                firstAllAuthError(text) ?: "Request failed with status: ${response.status}"
            )
        }
    }

    /**
     * allauth answers failures as {"status": 400, "errors": [{"code", "param", "message"}]}.
     */
    private fun firstAllAuthError(body: String): String? = try {
        json.parseToJsonElement(body).jsonObject["errors"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonPrimitive?.contentOrNull
    } catch (e: Exception) {
        null
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

    override suspend fun getPublicUsers(): List<UserDetailsDTO> {
        val session = sessionProvider()
        val url = "${session.baseUrl}/auth/users/"

        logger.d { "🌐 API Request - GET $url" }

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch public users with status: ${response.status}"
            )
        }

        return json.decodeFromString<List<UserDetailsDTO>>(response.body<String>())
    }

    override suspend fun getDashboard(): DashboardDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/stats/dashboard/"

        logger.d { "🌐 API Request - GET $url" }

        val response = httpClient.get(url) {
            headers {
                commonHeaders(session.sessionToken)
            }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch dashboard with status: ${response.status}"
            )
        }

        val dashboard = json.decodeFromString<DashboardDTO>(response.body<String>())

        logger.d { "📦 API Response - Dashboard fetched successfully" }

        return dashboard
    }

    override suspend fun getCalendarEvents(start: String?, end: String?): CalendarEventsDTO {
        val session = sessionProvider()
        val url = "${session.baseUrl}/api/calendar/events/"

        logger.d { "🌐 API Request - GET $url (${start ?: "…"} to ${end ?: "…"})" }

        val response = httpClient.get(url) {
            headers { commonHeaders(session.sessionToken) }
            start?.let { parameter("start", it) }
            end?.let { parameter("end", it) }
        }

        if (!response.status.isSuccess()) {
            throw HttpException(
                response.status.value,
                "Failed to fetch the calendar with status: ${response.status}"
            )
        }

        val events = json.decodeFromString<CalendarEventsDTO>(response.body<String>())

        logger.d { "📦 API Response - ${events.events.size} calendar events" }

        return events
    }
}
