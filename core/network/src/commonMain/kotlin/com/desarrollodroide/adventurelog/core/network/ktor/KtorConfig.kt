package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.serialization.json.Json

data class SessionInfo(
    val baseUrl: String,
    val sessionToken: String?
)

internal const val SESSION_TOKEN_HEADER = "X-Session-Token"

/** allauth's browser session: GET is the session, DELETE is signing out. */
internal const val SESSION_PATH = "/auth/browser/v1/auth/session"

internal const val CSRF_HEADER = "X-CSRFToken"

private val CSRF_ALPHABET = ('A'..'Z') + ('a'..'z') + ('0'..'9')

/**
 * A CSRF token the app makes up itself. Django's check is double-submit - the `csrftoken` cookie
 * must equal the `X-CSRFToken` header - so it needs no round trip to the server, only the right
 * shape: 32 characters from [A-Za-z0-9], or Django rejects it as malformed.
 */
internal fun newCsrfToken(): String = (1..32).map { CSRF_ALPHABET.random() }.joinToString("")

internal fun Url.origin(): String =
    if (port == protocol.defaultPort) "${protocol.name}://$host" else "${protocol.name}://$host:$port"

/**
 * What a Django session needs since server 0.13.1, which stopped reading [SESSION_TOKEN_HEADER]
 * and checks CSRF on login: the session as a `sessionid` cookie, a csrftoken pair, and a
 * same-origin Referer (required on every unsafe request over HTTPS). [SESSION_TOKEN_HEADER] stays
 * on the request: older servers still read it, and it marks a request that carried a session.
 */
internal fun HttpRequestBuilder.attachDjangoSession(sessionToken: String?, csrfToken: String) {
    val cookies = listOfNotNull("csrftoken=$csrfToken", sessionToken?.let { "sessionid=$it" })
    headers.append(HttpHeaders.Cookie, cookies.joinToString("; "))
    headers.append(CSRF_HEADER, csrfToken)
    if (!headers.contains(HttpHeaders.Referrer)) {
        headers.append(HttpHeaders.Referrer, url.build().origin())
    }
}

internal fun HeadersBuilder.commonHeaders(sessionToken: String?) {
    append(HttpHeaders.Accept, "application/json")
    append("X-Is-Mobile", "true")
    sessionToken?.let { 
        append(SESSION_TOKEN_HEADER, it)
    }
}

internal val defaultJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true  // Include fields with default values
    explicitNulls = true   // Include null values in JSON
}
