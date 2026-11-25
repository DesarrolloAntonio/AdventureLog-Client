package com.desarrollodroide.adventurelog.feature.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import com.desarrollodroide.adventurelog.feature.ui.theme.robotoFlex
import com.desarrollodroide.adventurelog.feature.ui.components.DateTile
import com.desarrollodroide.adventurelog.feature.ui.components.PassportChip
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedList
import com.desarrollodroide.adventurelog.feature.ui.components.SegmentedListDefaults
import com.desarrollodroide.adventurelog.feature.ui.components.TicketCard
import com.desarrollodroide.adventurelog.feature.ui.components.TicketStub
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The canvas's own pieces, shared by several Expressive screens. */
@OptIn(ExperimentalTestApi::class)
class ExpressivePiecesTest {

    private val longName = "The very long name of a collection that somebody typed in full, all sixty"

    @Test
    fun aLongTicketTitleTakesAtMostTwoLines() = runComposeUiTest {
        setContent {
            Box(Modifier.width(360.dp)) {
                TicketCard(
                    onClick = {},
                    photo = { Box(Modifier.testTag("photo")) },
                    title = longName,
                    subtitle = "Liguria",
                    stubs = listOf(TicketStub("FROM", "5 SEP"), TicketStub("TO", "9 SEP")),
                )
            }
        }

        val title = onNodeWithText(longName, useUnmergedTree = true).fetchSemanticsNode()
        val layout = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        title.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(layout)
        assertTrue("title took ${layout.first().lineCount} lines", layout.first().lineCount <= 2)
        assertTrue(layout.first().hasVisualOverflow)
        onNodeWithText("FROM", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("9 SEP", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun aTicketOpensOnTap() = runComposeUiTest {
        var opened = false
        setContent {
            TicketCard(
                onClick = { opened = true },
                photo = {},
                title = "Cinque Terre",
                subtitle = "",
                stubs = emptyList(),
                modifier = Modifier.testTag("ticket"),
            )
        }

        onNodeWithTag("ticket").performClick()

        assertTrue(opened)
    }

    @Test
    fun aPlannedChipSaysSo() = runComposeUiTest {
        setContent { PassportChip(code = "IT", name = "Italy", visited = false) }

        onNodeWithContentDescription("IT Italy, planned").assertIsDisplayed()
    }

    @Test
    fun aVisitedChipSaysSo() = runComposeUiTest {
        setContent { PassportChip(code = "NO", name = "Norway", visited = true) }

        onNodeWithContentDescription("NO Norway, visited").assertIsDisplayed()
    }

    @Test
    fun aDateTileReadsAsOneDate() = runComposeUiTest {
        setContent { DateTile(month = "SEP", day = 5, highlighted = true) }

        onNodeWithContentDescription("5 SEP").assertIsDisplayed()
    }

    @Test
    fun segmentedListRoundsTheOuterCornersOnly() {
        assertEquals(SegmentedListDefaults.single, SegmentedListDefaults.shapeFor(index = 0, count = 1))
        assertEquals(SegmentedListDefaults.first, SegmentedListDefaults.shapeFor(index = 0, count = 3))
        assertEquals(SegmentedListDefaults.middle, SegmentedListDefaults.shapeFor(index = 1, count = 3))
        assertEquals(SegmentedListDefaults.last, SegmentedListDefaults.shapeFor(index = 2, count = 3))
    }

    @Test
    fun condensedDisplayIsReallyNarrower() = runComposeUiTest {
        var normal = 0
        var condensed = 0
        setContent {
            val measurer = androidx.compose.ui.text.rememberTextMeasurer()
            val text = "Cinque Terre"
            val base = androidx.compose.ui.text.TextStyle(fontSize = 30.sp, fontWeight = androidx.compose.ui.text.font.FontWeight(800))
            normal = measurer.measure(text, base.copy(fontFamily = robotoFlex())).size.width
            condensed = measurer.measure(text, AdventureType.display(30.sp)).size.width
        }
        waitForIdle()

        // Width 38 at optical size 30 is 85% of the normal cut (fontTools measure of the same file);
        // at the default optical size 14 it would be 92%, which is what failed here first.
        assertTrue("normal $normal px, condensed $condensed px", condensed < normal * 0.88)
    }

    @Test
    fun segmentedListShowsEveryRow() = runComposeUiTest {
        setContent {
            SegmentedList(items = listOf("Units", "Currency", "Map style")) { Text(it) }
        }

        listOf("Units", "Currency", "Map style").forEach { onNodeWithText(it).assertIsDisplayed() }
    }
}
