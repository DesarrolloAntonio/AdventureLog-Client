package com.desarrollodroide.adventurelog.feature.ui.theme

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdventureTypeTest {

    @Test
    fun displayIsHeavyAndTight() {
        val style = DisplaySpec(44.sp)

        assertEquals(FontWeight(800), style.fontWeight)
        assertEquals(44.sp, style.fontSize)
        // The canvas sets display lines at 0.95 of the size: tighter than the size, never looser.
        assertTrue(style.lineHeight.isSp)
        assertTrue(style.lineHeight.value <= style.fontSize.value)
    }

    @Test
    fun stampIsTracked() {
        assertEquals(FontWeight(800), StampSpec.fontWeight)
        assertEquals(1.sp, StampSpec.letterSpacing)
    }
}
