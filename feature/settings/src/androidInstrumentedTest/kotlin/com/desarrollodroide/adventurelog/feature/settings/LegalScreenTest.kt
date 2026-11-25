package com.desarrollodroide.adventurelog.feature.settings

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.settings.ui.components.LegalDoc
import com.desarrollodroide.adventurelog.feature.settings.ui.components.LegalScreen
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsGroup
import com.desarrollodroide.adventurelog.feature.ui.components.settings.SettingsRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Settings as the canvas draws it: segmented groups, and Terms and Privacy as one page with tabs. */
@OptIn(ExperimentalTestApi::class)
class LegalScreenTest {

    @Test
    fun theLegalPageOpensOnTheDocAskedForAndSwitches() = runComposeUiTest {
        setContent { LegalScreen(initial = LegalDoc.PRIVACY, onBack = {}) }

        onNodeWithText("Privacy policy").assertIsDisplayed()
        onNodeWithText("Privacy").assertIsOn()
        onNodeWithText("Terms").assertIsOff()

        onNodeWithText("Terms").performClick()
        onNodeWithText("Terms of Use").assertIsDisplayed()
        onNodeWithText("Terms").assertIsOn()
    }

    @Test
    fun backLeavesTheLegalPage() = runComposeUiTest {
        var left = false
        setContent { LegalScreen(initial = LegalDoc.TERMS, onBack = { left = true }) }

        onNodeWithContentDescription("Back").performClick()
        assertTrue(left)
    }

    @Test
    fun aSegmentedGroupKeepsItsRowsClickable() = runComposeUiTest {
        var tapped: String? = null
        setContent {
            SettingsGroup(title = "Appearance", segmented = true) {
                SettingsRow(title = "Theme", supporting = "Light", onClick = { tapped = "Theme" })
                SettingsRow(title = "Language", supporting = "English", onClick = { tapped = "Language" })
            }
        }

        onNodeWithText("Appearance").assertIsDisplayed()
        onNodeWithText("Language").performClick()
        assertEquals("Language", tapped)
    }
}
