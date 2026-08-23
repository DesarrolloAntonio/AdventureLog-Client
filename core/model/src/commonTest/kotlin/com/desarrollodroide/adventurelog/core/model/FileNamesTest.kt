package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileNamesTest {

    @Test
    fun `accented letters survive`() {
        // A stricter ASCII filter turned "Riaño" into "Riao" on the shared file.
        assertEquals("Riaño y los Fiordos Leoneses.png", "Riaño y los Fiordos Leoneses".toSafeFileName(extension = "png"))
    }

    @Test
    fun `path separators are removed`() {
        assertEquals("etcpasswd.png", "../etc/passwd".toSafeFileName(extension = "png").also {
            assertTrue('/' !in it && '\\' !in it)
        }.removePrefix(".."))
    }

    @Test
    fun `a name of nothing but illegal characters falls back`() {
        assertEquals("location.png", "///".toSafeFileName(extension = "png"))
    }

    @Test
    fun `a very long name is truncated`() {
        val name = "a".repeat(500).toSafeFileName()
        assertTrue(name.length <= 80, "was ${name.length}")
    }

    @Test
    fun `no extension is added when none is asked for`() {
        assertEquals("Guadalest", "Guadalest".toSafeFileName())
    }
}
