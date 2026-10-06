package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.constants.ThemeMode
import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SettingsRepositoryImplTest {

    @Test
    fun `the compact view value an older build stored is deleted`() {
        val settings = MapSettings("compact_view" to true)

        SettingsRepositoryImpl(settings)

        assertFalse(settings.hasKey("compact_view"), "a setting nothing reads survived the upgrade")
    }

    @Test
    fun `the settings that are still used survive the same upgrade`() {
        val settings = MapSettings("compact_view" to true, "theme_mode" to ThemeMode.DARK.ordinal)

        val repository = SettingsRepositoryImpl(settings)

        assertEquals(ThemeMode.DARK, repository.getThemeMode().value)
    }
}
