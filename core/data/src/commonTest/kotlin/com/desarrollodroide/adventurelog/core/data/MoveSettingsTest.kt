package com.desarrollodroide.adventurelog.core.data

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The iOS move from NSUserDefaults to the keychain, with maps standing in for both stores. */
class MoveSettingsTest {

    private val flag = "settings_in_keychain"

    @Test
    fun theSessionAndPreferencesMoveAndLeaveNothingBehind() {
        val old = MapSettings("user_session" to "{\"token\":\"t\"}", "theme_mode" to 2, "use_dynamic_colors" to true)
        val new = MapSettings()

        moveSettings(old, new, flag)

        assertEquals("{\"token\":\"t\"}", new.getStringOrNull("user_session"))
        assertEquals(2, new.getIntOrNull("theme_mode"))
        assertEquals(true, new.getBooleanOrNull("use_dynamic_colors"))
        assertNull(old.getStringOrNull("user_session"))
        assertTrue(old.getBoolean(flag, false))
    }

    @Test
    fun theOldPasswordKeyIsDroppedNotCarried() {
        val old = MapSettings("remember_password" to "secret")
        val new = MapSettings()

        moveSettings(old, new, flag)

        assertFalse("remember_password" in new.keys)
        assertFalse("remember_password" in old.keys)
    }

    @Test
    fun aReinstallDoesNotComeBackSignedInToTheLastAccount() {
        // The keychain outlives an uninstall; the fresh install's defaults have no flag.
        val leftInKeychain = MapSettings("user_session" to "{\"token\":\"old\"}")

        moveSettings(MapSettings(), leftInKeychain, flag)

        assertNull(leftInKeychain.getStringOrNull("user_session"))
    }

    @Test
    fun itMovesOnlyOnce() {
        val old = MapSettings()
        val new = MapSettings()
        moveSettings(old, new, flag)
        new.putString("user_session", "{\"token\":\"new\"}")

        moveSettings(old, new, flag)

        assertEquals("{\"token\":\"new\"}", new.getStringOrNull("user_session"))
    }
}
