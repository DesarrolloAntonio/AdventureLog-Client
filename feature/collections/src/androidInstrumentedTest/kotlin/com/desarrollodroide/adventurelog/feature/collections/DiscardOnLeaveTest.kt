package com.desarrollodroide.adventurelog.feature.collections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.v2.runComposeUiTest
import coil3.ImageLoader
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.AddEditCollectionScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.AddEditTransportationContent
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * QA RL-09: the collection and transport forms left on Cancel without a word, losing what was
 * typed. With something typed they ask first; untouched, they still leave at once (the pair).
 */
@OptIn(ExperimentalTestApi::class)
class DiscardOnLeaveTest {

    @Composable
    private fun WithAppLocals(content: @Composable () -> Unit) {
        val context = LocalContext.current
        CompositionLocalProvider(LocalImageLoader provides ImageLoader(context)) { content() }
    }

    private fun ComposeUiTest.cancel() = onNodeWithText("Cancel").performScrollTo().performClick()

    @Test
    fun aTypedCollectionIsNotThrownAwayWithoutAsking() = runComposeUiTest {
        var left = 0
        setContent { WithAppLocals { AddEditCollectionScreen(onNavigateBack = { left++ }, onSave = {}) } }

        onNode(hasSetTextAction() and hasText("Collection Name")).performTextInput("QA_Trip")
        cancel()

        onNodeWithText("Discard this collection?").assertIsDisplayed()
        assertEquals(0, left)
        onNodeWithText("Discard").performClick()
        assertEquals(1, left)
    }

    @Test
    fun anUntouchedCollectionFormLeavesAtOnce() = runComposeUiTest {
        var left = 0
        setContent { WithAppLocals { AddEditCollectionScreen(onNavigateBack = { left++ }, onSave = {}) } }

        cancel()

        assertEquals(1, left)
    }

    @Composable
    private fun TransportForm(onLeave: () -> Unit) = AddEditTransportationContent(
        transportationTypes = listOf("train"),
        onNavigateBack = onLeave,
        onSave = {},
        onGenerateDescription = { _, _ -> },
        isGeneratingDescription = false
    )

    @Test
    fun aTypedTransportIsNotThrownAwayWithoutAsking() = runComposeUiTest {
        var left = 0
        setContent { WithAppLocals { TransportForm { left++ } } }

        onNode(hasSetTextAction() and hasText("Transportation Name")).performTextInput("QA_Train")
        cancel()

        onNodeWithText("Discard this transport?").assertIsDisplayed()
        assertEquals(0, left)
        onNodeWithText("Keep editing").performClick()
        onNode(hasSetTextAction() and hasText("QA_Train")).assertIsDisplayed()
        assertEquals(0, left)
    }

    @Test
    fun anUntouchedTransportFormLeavesAtOnce() = runComposeUiTest {
        var left = 0
        setContent { WithAppLocals { TransportForm { left++ } } }

        cancel()

        assertEquals(1, left)
    }
}
