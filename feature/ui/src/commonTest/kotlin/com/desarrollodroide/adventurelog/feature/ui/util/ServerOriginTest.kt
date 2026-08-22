package com.desarrollodroide.adventurelog.feature.ui.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServerOriginTest {

    private val server = "https://ds224.boga-aeolian.ts.net:3447"

    @Test
    fun `media on the user's own server matches`() {
        assertTrue(isSameOrigin("$server/media/images/abc.webp", server))
    }

    @Test
    fun `trailing path and query do not affect the comparison`() {
        assertTrue(isSameOrigin("$server/media/images/a.webp?v=2#x", server))
    }

    @Test
    fun `default port is filled in on both sides`() {
        assertTrue(isSameOrigin("https://example.com/media/a.webp", "https://example.com:443"))
        assertTrue(isSameOrigin("http://example.com:80/media/a.webp", "http://example.com"))
    }

    @Test
    fun `a different port is a different origin`() {
        assertFalse(isSameOrigin("https://ds224.boga-aeolian.ts.net:3445/media/a.webp", server))
    }

    @Test
    fun `a different scheme is a different origin`() {
        assertFalse(isSameOrigin("http://ds224.boga-aeolian.ts.net:3447/media/a.webp", server))
    }

    @Test
    fun `third party hosts never match`() {
        assertFalse(isSameOrigin("https://upload.wikimedia.org/pic.jpg", server))
        assertFalse(isSameOrigin("https://evil.example/media/images/a.webp", server))
    }

    @Test
    fun `a look-alike host that only shares a suffix does not match`() {
        assertFalse(isSameOrigin("https://evil-ds224.boga-aeolian.ts.net:3447/x", server))
        assertFalse(isSameOrigin("https://ds224.boga-aeolian.ts.net.evil.com:3447/x", server))
    }

    @Test
    fun `credentials in the authority are rejected`() {
        assertFalse(
            isSameOrigin("https://user:pw@ds224.boga-aeolian.ts.net:3447/media/a.webp", server)
        )
    }

    @Test
    fun `blank or malformed input never matches`() {
        assertFalse(isSameOrigin(null, server))
        assertFalse(isSameOrigin("", server))
        assertFalse(isSameOrigin("not-a-url", server))
        assertFalse(isSameOrigin("file:///sdcard/a.jpg", server))
        assertFalse(isSameOrigin("$server/media/a.webp", null))
    }
}
