package com.desarrollodroide.adventurelog.feature.settings

import com.desarrollodroide.adventurelog.feature.settings.ui.components.PrivacyPolicy
import com.desarrollodroide.adventurelog.feature.settings.ui.components.TermsOfUse
import com.desarrollodroide.adventurelog.feature.settings.ui.components.annotated
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * QA #4: the terms were another app's ("Shiori", under Apache 2.0) and the privacy policy read
 * "Effective as of [Insert Date Here]" - a store reviewer reads both.
 */
class LegalTextsTest {

    private val terms = TermsOfUse.annotated().text
    private val privacy = PrivacyPolicy.annotated().text

    @Test
    fun `neither text names another app or leaves a placeholder`() {
        listOf(terms, privacy).forEach { text ->
            assertFalse("Shiori" in text, "names another app")
            assertFalse("[Insert" in text || "Insert Date" in text, "a placeholder is left")
        }
    }

    @Test
    fun `the terms name this app's licence and say it is an independent client`() {
        assertTrue("MIT License" in terms)
        assertFalse("Apache" in terms)
        assertTrue("not affiliated" in terms)
    }

    @Test
    fun `the policy names every service the app talks to`() {
        listOf("AdventureLog server", "Google Maps", "Wikipedia").forEach { assertTrue(it in privacy, it) }
        assertTrue("Effective 6 October 2026." in privacy)
    }
}
