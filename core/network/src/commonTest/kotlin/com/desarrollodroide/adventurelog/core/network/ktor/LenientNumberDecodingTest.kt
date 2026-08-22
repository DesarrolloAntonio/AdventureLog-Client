package com.desarrollodroide.adventurelog.core.network.ktor

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins down how the shared [defaultJson] handles a JSON number arriving in a String property.
 *
 * Several DTOs declare server-computed numbers (coordinates, counts, distance) as String. Whether
 * that is merely untidy or an actual crash depends entirely on `isLenient`, so it is worth an
 * explicit test rather than an assumption.
 */
class LenientNumberDecodingTest {

    @Serializable
    private data class StringHolder(val value: String? = null)

    @Serializable
    private data class DoubleHolder(val value: Double? = null)

    @Test
    fun `lenient json decodes a json number into a String property`() {
        val decoded = defaultJson.decodeFromString<StringHolder>("""{"value":-2.178188}""")

        assertEquals("-2.178188", decoded.value)
    }

    @Test
    fun `lenient json decodes an integer into a String property`() {
        assertEquals("69", defaultJson.decodeFromString<StringHolder>("""{"value":69}""").value)
    }

    @Test
    fun `strict json would reject the same payload`() {
        val strict = Json { ignoreUnknownKeys = true }
        val failed = try {
            strict.decodeFromString<StringHolder>("""{"value":-2.178188}""")
            false
        } catch (e: Exception) {
            true
        }

        assertEquals(true, failed, "strict parsing should reject a number in a String field")
    }

    @Test
    fun `a number decodes into a Double property either way`() {
        assertEquals(
            -2.178188,
            defaultJson.decodeFromString<DoubleHolder>("""{"value":-2.178188}""").value
        )
    }
}
