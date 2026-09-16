package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.collections.ui.components.PublicSwitchRow
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.components.BasicInfoSection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data.CollectionFormData
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * A tap on the words of a "Public" row toggles it (QA 04, CO-11). Only the switch did, and it cost
 * the campaign two false readings: the label was tapped, nothing changed, and it looked saved.
 */
class PublicRowsTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingTheLabelOfAnItemsPublicRowTogglesItBothWays() = runComposeUiTest {
        setContent {
            var public by remember { mutableStateOf(false) }
            PublicSwitchRow(title = "Public note", checked = public, onCheckedChange = { public = it })
        }

        onNodeWithText("Public note").performClick()
        onNodeWithText("Public note", useUnmergedTree = false).assertIsOn()
        onNodeWithText("Public note").performClick()
        onNodeWithText("Public note").assertIsOff()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tappingPublicCollectionTogglesTheCollection() = runComposeUiTest {
        var form = CollectionFormData(name = "Peru")
        setContent {
            var state by remember { mutableStateOf(form) }
            BasicInfoSection(formData = state, onFormDataChange = { state = it; form = it })
        }

        onNodeWithText("Public Collection").performClick()
        waitForIdle()

        assertEquals(true, form.isPublic)
    }
}
