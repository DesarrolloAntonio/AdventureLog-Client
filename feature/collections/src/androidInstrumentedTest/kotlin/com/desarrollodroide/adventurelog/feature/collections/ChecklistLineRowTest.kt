package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditChecklist.ChecklistLineRow
import org.junit.Test
import org.junit.Assert.assertEquals

/**
 * QA RL-06: the checklist editor's checkboxes had no name, so a screen reader could not say which
 * item it was about to tick. The toggle has to carry the item's text; the pair checks the toggle
 * still works and Remove names its item.
 */
@OptIn(ExperimentalTestApi::class)
class ChecklistLineRowTest {

    @Test
    fun theToggleIsNamedAfterItsItem() = runComposeUiTest {
        setContent {
            ChecklistLineRow(name = "item one", checked = false, onToggle = {}, onRemove = {})
        }

        onNode(isToggleable() and hasText("item one")).assertIsOff()
    }

    @Test
    fun tappingTheNamedToggleTicksTheItem() = runComposeUiTest {
        var checked by mutableStateOf(false)
        setContent {
            ChecklistLineRow(name = "item one", checked = checked, onToggle = { checked = !checked }, onRemove = {})
        }

        onNode(isToggleable() and hasText("item one")).performClick()

        onNode(isToggleable() and hasText("item one")).assertIsOn()
    }

    @Test
    fun removeSaysWhichItemItRemoves() = runComposeUiTest {
        var removed = 0
        setContent {
            ChecklistLineRow(name = "item two", checked = false, onToggle = {}, onRemove = { removed++ })
        }

        onNodeWithContentDescription("Remove item two").performClick()

        assertEquals(1, removed)
    }
}
