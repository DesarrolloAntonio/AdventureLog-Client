package com.desarrollodroide.adventurelog.feature.settings

import com.desarrollodroide.adventurelog.feature.settings.ui.components.IosPrivacyPolicy
import com.desarrollodroide.adventurelog.feature.settings.ui.components.IosTermsOfUse
import com.desarrollodroide.adventurelog.feature.settings.ui.components.PrivacyPolicy
import com.desarrollodroide.adventurelog.feature.settings.ui.components.TermsOfUse
import com.desarrollodroide.adventurelog.feature.settings.ui.components.plainText
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * QA #4: the terms were another app's ("Shiori", under Apache 2.0) and the privacy policy read
 * "Effective as of [Insert Date Here]" - a store reviewer reads both.
 */
class LegalTextsTest {

    private val terms = TermsOfUse.plainText()
    private val privacy = PrivacyPolicy.plainText()
    private val iosTerms = IosTermsOfUse.plainText()
    private val iosPrivacy = IosPrivacyPolicy.plainText()

    @Test
    fun `neither text names another app or leaves a placeholder`() {
        listOf(terms, privacy, iosTerms, iosPrivacy).forEach { text ->
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
        assertTrue("Effective 25 November 2026." in privacy)
    }

    @Test
    fun `both policies say a provider sign-in goes through the provider`() {
        listOf(privacy, iosPrivacy).forEach { assertTrue("sign-in provider you choose" in it) }
    }

    @Test
    fun `the iOS texts describe the iPhone app, not the Android one`() {
        assertTrue("AdventureLog for iOS" in iosTerms)
        listOf("Android", "Google Maps", "Wikipedia", "Camera").forEach { assertFalse(it in iosPrivacy, it) }
        assertTrue("Apple Maps" in iosPrivacy)
        assertTrue("iOS includes in your device's backups" in iosPrivacy)
    }
}
