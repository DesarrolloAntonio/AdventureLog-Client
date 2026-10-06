package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.components.BasicInfoTransportationSection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import com.desarrollodroide.adventurelog.feature.ui.components.date.DatePickerField
import org.junit.Assert.assertEquals
import org.junit.Test

/** What the collection editors looked like on a phone, checked from screenshots (QA 04). */
class FormFieldsTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aChosenDateKeepsItsLabelReadsAsADateAndCanBeCleared() = runComposeUiTest {
        // The stay form showed "2026-10-10" and "2026-10-13" side by side with nothing saying
        // which was the check-in, and dates had to be typed as YYYY-MM-DD.
        var value = "2026-10-10"
        setContent {
            var v by remember { mutableStateOf(value) }
            DatePickerField(label = "Check-in", value = v, onValueChange = { v = it; value = it })
        }

        onNodeWithText("Check-in").assertIsDisplayed()
        onNodeWithText("10/10/2026").assertIsDisplayed()
        onNodeWithContentDescription("Clear Check-in").performClick()
        onNodeWithText("Not set").assertIsDisplayed()
        assertEquals("", value)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aChosenDateOpensTheCalendar() = runComposeUiTest {
        setContent { DatePickerField(label = "Date (optional)", value = "", onValueChange = {}) }

        onNodeWithText("Not set").performClick()

        onNodeWithText("OK").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun theTransportFormSaysRatingOnce() = runComposeUiTest {
        setContent {
            BasicInfoTransportationSection(
                formData = TransportationFormData(name = "Train to Cusco"), transportationTypes = listOf("train"),
                onFormDataChange = {}, onNavigateBack = {}, onGenerateDescription = {}, isGeneratingDescription = false
            )
        }

        onAllNodes(hasText("Rating", substring = false)).assertCountEquals(1)
    }
}
