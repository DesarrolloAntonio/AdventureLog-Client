package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * How the app tells the four kinds of server apart from their answers, as measured on 0.13.1 and
 * as upstream's code describes 0.14.0 (main/views.py ServerInfoView, the SvelteKit proxy). No
 * 0.14.0 server has been driven yet.
 */
class ServerProbeTest {

    private val serverInfo = """{"public_url":"https://qa.test","allow_public_access":true,"version":"v0.14.0",""" +
        """"capabilities":["mobile_auth_v1","conditional_writes_v1","client_ids_v1"]}"""

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    private fun MockRequestHandleScope.html(status: HttpStatusCode = HttpStatusCode.NotFound) =
        respond("<!doctype html>", status, headersOf(HttpHeaders.ContentType, "text/html"))

    private fun MockRequestHandleScope.slashRedirect(request: HttpRequestData) =
        respond("", HttpStatusCode.PermanentRedirect, headersOf(HttpHeaders.Location, request.url.encodedPath.trimEnd('/')))

    private fun probe(answer: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): Pair<ServerProfile, List<String>> {
        val paths = mutableListOf<String>()
        // followRedirects off: the probe must not depend on whether Ktor follows the 308.
        val client = HttpClient(MockEngine { request -> paths += request.url.encodedPath; answer(request) }) {
            followRedirects = false
        }
        val profile = runBlocking { probeServer { path -> client.get("https://qa.test$path").call } }
        return profile to paths
    }

    @Test
    fun `0_14 Django directly`() {
        val (profile, _) = probe { request ->
            if (request.url.encodedPath == "/api/server-info/") json(serverInfo) else html()
        }
        assertEquals(ServerProfile(ServerApi.V014, behindWebProxy = false), profile)
    }

    @Test
    fun `0_14 behind the web frontend's proxy`() {
        val (profile, _) = probe { request ->
            when (request.url.encodedPath) {
                "/api/server-info/" -> slashRedirect(request)
                "/api/server-info" -> json(serverInfo)
                else -> html()
            }
        }
        assertEquals(ServerProfile(ServerApi.V014, behindWebProxy = true), profile)
    }

    @Test
    fun `0_13 Django directly`() {
        val (profile, paths) = probe { request ->
            when (request.url.encodedPath) {
                "/csrf/" -> json("""{"csrfToken":"x"}""")
                else -> html() // Django's own 404 page
            }
        }
        assertEquals(ServerProfile(ServerApi.Legacy, behindWebProxy = false), profile)
        assertEquals(listOf("/api/server-info/", "/csrf/"), paths)
    }

    @Test
    fun `0_13 behind the web frontend's proxy`() {
        val (profile, _) = probe { request ->
            when (request.url.encodedPath) {
                "/api/server-info/" -> slashRedirect(request)
                "/api/server-info" -> json("""{"detail":"Not found."}""", HttpStatusCode.NotFound)
                else -> html()
            }
        }
        assertEquals(ServerProfile(ServerApi.Legacy, behindWebProxy = true), profile)
    }

    @Test
    fun `a 200 that is not server info is an old server`() {
        // The 0.13 all-in-one frontend answers unknown pages with its own HTML, sometimes a 200.
        val (profile, _) = probe { request ->
            when (request.url.encodedPath) {
                "/api/server-info/" -> respond("<!doctype html>", HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "text/html"))
                else -> html()
            }
        }
        assertEquals(ServerProfile(ServerApi.Legacy, behindWebProxy = true), profile)
    }
}
