package com.desarrollodroide.adventurelog.feature.ui.util

import androidx.compose.ui.platform.UriHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class UriOpeningTest {

    private class Device(private val canOpen: Boolean) : UriHandler {
        val opened = mutableListOf<String>()
        override fun openUri(uri: String) {
            // What Android's handler does when no app takes the link.
            if (!canOpen) throw IllegalArgumentException("Can't open $uri.")
            opened += uri
        }
    }

    @Test
    fun `a link no app can open is reported instead of crashing`() {
        val opened = runCatching { Device(canOpen = false).tryOpenUri("ftp://example.org/qa") }
            .getOrElse { fail("opening the link threw: $it") }

        assertFalse(opened)
    }

    @Test
    fun `a link an app can open is opened`() {
        val device = Device(canOpen = true)

        assertTrue(device.tryOpenUri("https://example.org/qa"))
        assertEquals(listOf("https://example.org/qa"), device.opened)
    }
}
