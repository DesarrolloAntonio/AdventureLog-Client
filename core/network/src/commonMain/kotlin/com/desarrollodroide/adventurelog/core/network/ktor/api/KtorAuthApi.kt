package com.desarrollodroide.adventurelog.core.network.ktor.api

import co.touchlab.kermit.Logger
import com.desarrollodroide.adventurelog.core.network.api.AuthApi
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.network.ktor.SessionInfo
import com.desarrollodroide.adventurelog.core.network.ktor.AuthStageRequest
import com.desarrollodroide.adventurelog.core.network.ktor.LOGIN_PATH
import com.desarrollodroide.adventurelog.core.network.ktor.MFA_AUTHENTICATE_PATH
import com.desarrollodroide.adventurelog.core.network.ktor.SESSION_PATH
import com.desarrollodroide.adventurelog.core.network.ktor.commonHeaders
import com.desarrollodroide.adventurelog.core.network.ktor.defaultJson
import com.desarrollodroide.adventurelog.core.network.ktor.redactSecrets
import com.desarrollodroide.adventurelog.core.network.model.request.LoginRequest
import com.desarrollodroide.adventurelog.core.network.model.request.LoginResponse
import com.desarrollodroide.adventurelog.core.network.model.request.PendingLoginResponse
import com.desarrollodroide.adventurelog.core.network.model.request.SecondFactorRequest
import com.desarrollodroide.adventurelog.core.network.model.response.LoginOutcomeDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UserDetailsDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class KtorAuthApi(
    private val httpClient: HttpClient,
    private val sessionProvider: () -> SessionInfo,
    private val onSessionTokenReceived: (String) -> Unit,
    private val onPendingLogin: (String) -> Unit = {},
    private val json: Json = defaultJson
) : AuthApi {
    
    private val logger = Logger.withTag("KtorAuthApi")
    
    override suspend fun login(
        username: String,
        password: String
    ): LoginOutcomeDTO {
        val sessionInfo = sessionProvider()
        val loginUrl = "${sessionInfo.baseUrl}$LOGIN_PATH"
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

        if (response.status == HttpStatusCode.Unauthorized) {
            // allauth's "not finished yet": with two-step verification the password step ends in a
            // 401 naming mfa_authenticate as pending, and the session that carries it on.
            val pending = pendingLogin(response.body<String>())
            val pendingToken = pending?.meta?.sessionToken
            if (pending != null && pending.needsSecondFactor && pendingToken != null) {
                logger.d { "Password accepted; the account asks for a second factor" }
                onPendingLogin(pendingToken)
                return LoginOutcomeDTO.SecondFactorRequired
            }
        }
        return LoginOutcomeDTO.SignedIn(signedInUser(response, sentToken = null, step = "Login"))
    }

    override suspend fun authenticateSecondFactor(
        pendingToken: String,
        code: String
    ): UserDetailsDTO {
        val sessionInfo = sessionProvider()
        val response = httpClient.post("${sessionInfo.baseUrl}$MFA_AUTHENTICATE_PATH") {
            // The interceptor must not read a 401 here as the end of a signed-in session: it is
            // a sign-in that has not finished, or has run out of time.
            attributes.put(AuthStageRequest, Unit)
            contentType(ContentType.Application.Json)
            headers {
                commonHeaders(pendingToken)
                append("Referer", sessionInfo.baseUrl)
            }
            setBody(SecondFactorRequest(code = code))
        }
        logger.d { "Second factor response status: ${response.status}" }
        return signedInUser(response, sentToken = pendingToken, step = "Second factor")
    }

    private fun pendingLogin(body: String): PendingLoginResponse? =
        runCatching { json.decodeFromString<PendingLoginResponse>(body) }.getOrNull()

    /**
     * The user an authenticated answer names, after handing its session to [onSessionTokenReceived].
     * allauth only puts `session_token` in the body when it differs from the one the request sent
     * ([sentToken]): its absence then means the sent one is still the session.
     */
    private suspend fun signedInUser(response: HttpResponse, sentToken: String?, step: String): UserDetailsDTO {
        if (!response.status.isSuccess()) {
            try {
                val errorBody = response.body<String>()
                logger.e {
                    "$step failed with status: ${response.status}. " +
                        "Error body: ${redactSecrets(errorBody)}"
                }
            } catch (e: Exception) {
                logger.e { "$step failed with status: ${response.status}. Could not read error body." }
            }

            throw HttpException(
                response.status.value,
                "$step failed with status: ${response.status}"
            )
        }

        val responseBody = response.body<String>()
        val loginResponse = json.decodeFromString<LoginResponse>(responseBody)

        // The app client returns the session in the body, so it survives the web frontend's
        // proxy, which strips every Set-Cookie. The cookie is only a fallback.
        val sessionToken = loginResponse.meta.sessionToken
            ?: response.headers.getAll("Set-Cookie").orEmpty()
                .firstNotNullOfOrNull { Regex("sessionid=([^;]+)").find(it)?.groupValues?.get(1) }
            ?: sentToken

        if (sessionToken == null) {
            logger.w { "$step answered ${response.status.value} with no session token" }
        } else {
            // Never log the token itself - it grants full account access.
            logger.d { "Session token received (${sessionToken.length} chars)" }
            // Notify the parent about the new session token
            onSessionTokenReceived(sessionToken)
        }

        logger.d { "$step successful for user: ${loginResponse.data.user.username}" }

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
    }

    /**
     * The web's sign-out: DELETE on allauth's session. allauth answers **401** when it has worked -
     * "you are now not authenticated" - so no status is an error here; this is best effort, and
     * the local sign-out never waits on it.
     */
    override suspend fun logout(baseUrl: String, sessionToken: String) {
        val response = httpClient.delete("$baseUrl$SESSION_PATH") {
            headers {
                commonHeaders(sessionToken)
                append("Referer", baseUrl)
            }
        }
        logger.d { "Server session ended (${response.status.value})" }
    }
}
