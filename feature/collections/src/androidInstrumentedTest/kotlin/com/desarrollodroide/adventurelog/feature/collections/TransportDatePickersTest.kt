package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.components.DateTransportationSection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.data.TransportationFormData
import org.junit.Assert.assertEquals
import org.junit.Test
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsDisplayed

/**
 * The transport form's date and time buttons (QA 04, CO-06): they set a flag that nothing read,
 * so no dialog ever opened and a transport's dates could not be set from the app.
 */
class TransportDatePickersTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun selectDateOpensAPickerAndNothingOpensBeforeIt() = runComposeUiTest {
        setContent {
            var form by remember { mutableStateOf(TransportationFormData(departureDate = "2026-10-05T00:00:00Z", isAllDay = true)) }
            DateTransportationSection(formData = form, onFormDataChange = { form = it })
        }
        onNodeWithText("Date Information").performClick()
        onNodeWithText("Cancel").assertDoesNotExist()

        onAllNodesWithContentDescriptionFirst("Select date").performClick()

        onNodeWithText("OK").assertExists()
        onNodeWithText("Cancel").assertExists()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aTimeConfirmedInTheDialogIsStoredInUtc() = runComposeUiTest {
        var stored = ""
        setContent {
            var form by remember {
                mutableStateOf(
                    TransportationFormData(
                        departureDate = "2026-10-05T12:30:00Z",
                        departureTimezone = "Europe/Madrid",
                        isAllDay = false
                    )
                )
            }
            DateTransportationSection(formData = form, onFormDataChange = { form = it; stored = it.departureDate })
        }
        onNodeWithText("Date Information").performClick()

        onAllNodesWithContentDescriptionFirst("Select time").performClick()
        onNodeWithText("Select time").assertExists()
        onNodeWithText("OK").performClick()

        // The dialog opened on 14:30 Madrid, the local reading of 12:30 UTC; confirming it keeps
        // the same moment.
        waitForIdle()
        assertEquals("2026-10-05T12:30:00Z", stored)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun theArrivalIsOnScreenBesideTheDeparture() = runComposeUiTest {
        // QA 04: side by side, the departure field took the width and the arrival was pushed
        // out of the card, its date nowhere to be seen.
        setContent {
            DateTransportationSection(
                formData = TransportationFormData(departureDate = "2026-10-05T00:00:00Z", arrivalDate = "2026-10-07T00:00:00Z", isAllDay = true),
                onFormDataChange = {}
            )
        }
        onNodeWithText("Date Information").performClick()

        onNodeWithText("05/10/2026").assertIsDisplayed()
        onNodeWithText("07/10/2026").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun constrainedToTheCollectionADayOutsideItCannotBePicked() = runComposeUiTest {
        setContent {
            var form by remember { mutableStateOf(TransportationFormData(departureDate = "2026-10-05T00:00:00Z", isAllDay = true)) }
            DateTransportationSection(
                formData = form, onFormDataChange = { form = it },
                collectionStart = "2026-10-01", collectionEnd = "2026-10-20"
            )
        }
        onNodeWithText("Date Information").performClick()
        // The words toggle it, not only the switch (as CO-11).
        onNodeWithText("Constrain to Collection Dates").performClick()
        onAllNodesWithContentDescriptionFirst("Select date").performClick()

        // A day's name ("Sunday, October 25, 2026") is its text or its description depending on
        // the Material version; either will do.
        fun day(name: String) = hasText(name, substring = true) or hasContentDescription(name, substring = true)
        onAllNodes(day("October 25, 2026"), useUnmergedTree = true)[0].assertIsNotEnabled()
        onAllNodes(day("October 15, 2026"), useUnmergedTree = true)[0].assertIsEnabled()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun withoutCollectionDatesThereIsNothingToConstrainTo() = runComposeUiTest {
        setContent { DateTransportationSection(formData = TransportationFormData(), onFormDataChange = {}) }
        onNodeWithText("Date Information").performClick()

        onNodeWithText("Constrain to Collection Dates").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    private fun androidx.compose.ui.test.ComposeUiTest.onAllNodesWithContentDescriptionFirst(label: String) =
        onAllNodes(androidx.compose.ui.test.hasContentDescription(label))[0]
}
