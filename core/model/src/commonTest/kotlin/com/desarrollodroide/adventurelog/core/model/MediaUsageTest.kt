package com.desarrollodroide.adventurelog.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MediaUsageTest {

    @Test
    fun `bytes below a kilobyte are printed whole`() {
        assertEquals("0 B", formatBytes(0))
        assertEquals("512 B", formatBytes(512))
        assertEquals("1023 B", formatBytes(1023))
    }

    @Test
    fun `larger sizes step through the units`() {
        assertEquals("1 KB", formatBytes(1024))
        assertEquals("1 MB", formatBytes(1024L * 1024))
        assertEquals("1 GB", formatBytes(1024L * 1024 * 1024))
        assertEquals("1 TB", formatBytes(1024L * 1024 * 1024 * 1024))
    }

    @Test
    fun `a fraction keeps one decimal`() {
        assertEquals("16.6 KB", formatBytes(17_000))
        assertEquals("1.5 MB", formatBytes(1024L * 1024 * 3 / 2))
    }

    @Test
    fun `total files counts every kind`() {
        val usage = MediaUsage(
            totalBytes = 300,
            imagesBytes = 100,
            attachmentsBytes = 100,
            profilePicsBytes = 100,
            imagesFiles = 1408,
            attachmentsFiles = 1,
            profilePicsFiles = 0,
            limitBytes = null
        )
        assertEquals(1409, usage.totalFiles)
    }
}
