package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.testing.AdventureLogNetworkStub
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class UserRepositoryImplTest {

    private class RecordingNetwork : AdventureLogNetworkStub() {
        var initializedWith: Pair<String, String?>? = null

        override fun initializeFromSession(serverUrl: String, sessionToken: String?) {
            initializedWith = serverUrl to sessionToken
        }
    }

    private fun storedSession(serverUrl: String, token: String) = Json { encodeDefaults = true }.encodeToString(
        UserDetails.serializer(),
        UserDetails(uuid = "u1", username = "claude", dateJoined = "2026-08-28", sessionToken = token, serverUrl = serverUrl)
    )

    @Test
    fun `a session restored from storage reaches the network`() {
        val network = RecordingNetwork()

        UserRepositoryImpl(MapSettings("user_session" to storedSession("https://qa.test", "token-1")), network)

        // Android restores a killed app onto Home without the login screen, which is where this
        // used to happen; without it every request failed with "Base URL is not initialized".
        assertEquals("https://qa.test" to "token-1", network.initializedWith)
    }

    @Test
    fun `with no stored session the network is left alone`() {
        val network = RecordingNetwork()

        UserRepositoryImpl(MapSettings(), network)

        assertNull(network.initializedWith)
    }

    @Test
    fun `a password an older build stored is deleted when the repository loads`() {
        val settings = MapSettings(
            "remember_username" to "claude",
            "remember_url" to "https://qa.test",
            "remember_password" to "hunter2"
        )

        UserRepositoryImpl(settings, RecordingNetwork())

        assertFalse(settings.hasKey("remember_password"), "the plain-text password survived the upgrade")
    }

    @Test
    fun `remember me still remembers the server and the username`() = runTest {
        val settings = MapSettings()
        val repository = UserRepositoryImpl(settings, RecordingNetwork())

        repository.saveRememberMeCredentials(url = "https://qa.test", username = "claude")

        val remembered = UserRepositoryImpl(settings, RecordingNetwork()).getRememberMeCredentials().first()
        assertEquals("claude", remembered?.userName)
        assertEquals("https://qa.test", remembered?.serverUrl)
        assertEquals("", remembered?.password)
        assertFalse(settings.hasKey("remember_password"))
    }
}
