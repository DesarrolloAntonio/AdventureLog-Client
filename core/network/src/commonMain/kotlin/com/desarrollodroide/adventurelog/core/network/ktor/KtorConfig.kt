package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

data class SessionInfo(
    val baseUrl: String,
    val sessionToken: String?
)

internal const val SESSION_TOKEN_HEADER = "X-Session-Token"

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
