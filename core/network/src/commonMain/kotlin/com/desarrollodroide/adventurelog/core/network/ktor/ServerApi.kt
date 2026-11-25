package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.call.HttpClientCall
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.http.isSuccess
import kotlinx.serialization.json.jsonObject

/**
 * Which REST API a server speaks. Server 0.14.0 moved every route under `/api/` and renamed most
 * of them (upstream's `backend/server/API.md`, "Changes in this version"), with no aliases for the
 * old ones. The app keeps building 0.13-style requests and [rewriteFor014] turns them into 0.14
 * ones on the way out, so one build talks to both.
 */
internal enum class ServerApi { Legacy, V014 }

/** What the first request to a server finds out about it; reset whenever the server changes. */
internal data class ServerProfile(val api: ServerApi, val behindWebProxy: Boolean)

/** Only 0.14.0 and later answer it; it needs no session. */
internal const val SERVER_INFO_PATH = "/api/server-info/"

/** Ends the path of allauth's session, browser or app client, old routes or new. */
internal const val SESSION_SUFFIX = "/auth/session"

/**
 * Finds out which API a server speaks and whether the web frontend's proxy stands in front of it,
 * with [get] sending an anonymous GET to a path on that server.
 *
 * - `GET /api/server-info/` answers JSON with `capabilities` only on 0.14.0+.
 * - The web frontend (SvelteKit, both versions) answers a trailing slash with a 308. Whether Ktor
 *   follows it or not, a proxied server shows: either the 308 itself or an answer for the path
 *   without the slash.
 * - Older servers answer neither; their /csrf/ tells the proxy apart, as before (HTML = proxy).
 */
internal suspend fun probeServer(get: suspend (path: String) -> HttpClientCall): ServerProfile {
    val info = get(SERVER_INFO_PATH)
    if (info.response.status.value in 300..399) {
        val withoutSlash = get(SERVER_INFO_PATH.trimEnd('/'))
        return ServerProfile(
            api = if (withoutSlash.isServerInfo()) ServerApi.V014 else ServerApi.Legacy,
            behindWebProxy = true,
        )
    }
    if (info.isServerInfo()) {
        return ServerProfile(ServerApi.V014, behindWebProxy = !info.request.url.encodedPath.endsWith("/"))
    }
    val csrf = get(CSRF_PROBE_PATH)
    return ServerProfile(
        api = ServerApi.Legacy,
        behindWebProxy = csrf.response.contentType()?.match(ContentType.Text.Html) == true,
    )
}

private suspend fun HttpClientCall.isServerInfo(): Boolean {
    if (!response.status.isSuccess()) return false
    val body = runCatching { response.bodyAsText() }.getOrNull() ?: return false
    return runCatching { defaultJson.parseToJsonElement(body).jsonObject.containsKey("capabilities") }
        .getOrDefault(false)
}

/**
 * Server 0.14.0 reads the app's `X-Session-Token` again (`SessionTokenAuthentication`) and skips
 * CSRF for a request that carries it **and no session cookie**
 * (`DisableCSRFForHeaderCredentialsMiddleware`). So the token travels alone: no cookie, no csrf
 * pair. The web frontend's proxy forwards the header untouched, still answers a trailing slash with
 * a 308 a POST does not survive (it puts the slash back itself), and still refuses a form post
 * without the site as Origin.
 */
internal fun HttpRequestBuilder.attachTokenSession(behindWebProxy: Boolean) {
    if (!headers.contains(HttpHeaders.Origin)) {
        headers.append(HttpHeaders.Origin, url.build().origin())
    }
    if (behindWebProxy) {
        val path = url.encodedPath
        if (path.length > 1 && path.endsWith("/")) {
            url.encodedPath = path.trimEnd('/')
        }
    }
}

/** Turns a 0.13-style request into its 0.14.0 equivalent: see [ApiRoute.for014]. */
internal fun HttpRequestBuilder.rewriteFor014() {
    val before = ApiRoute(method, url.encodedPath, url.parameters.entries().flatMap { (k, vs) -> vs.map { k to it } })
    val after = before.for014()
    if (after == before) return
    method = after.method
    url.encodedPath = after.path
    url.parameters.clear()
    after.query.forEach { (k, v) -> url.parameters.append(k, v) }
}

/** Marks the app's own probe requests, which go out bare. */
internal const val PROBE_HEADER = "X-AdventureLog-App-Probe"
