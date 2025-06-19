package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json

data class SessionInfo(
    val baseUrl: String,
    val sessionToken: String?
)

internal fun HeadersBuilder.commonHeaders(sessionToken: String?) {
    append(HttpHeaders.Accept, "application/json")
    append("X-Is-Mobile", "true")
    sessionToken?.let { 
        append("X-Session-Token", it)
    }
}

internal val defaultJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}
