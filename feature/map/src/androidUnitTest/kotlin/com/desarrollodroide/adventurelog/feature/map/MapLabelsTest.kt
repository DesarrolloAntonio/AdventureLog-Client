package com.desarrollodroide.adventurelog.feature.map

import com.desarrollodroide.adventurelog.feature.map.ui.components.statText
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the map card says (QA 06). */
class MapLabelsTest {

    @Test
    fun aCountNotLoadedYetIsADashAndALoadedZeroIsAZero() {
        // The card said 0 / 0 / 0 over the spinner, as if the account were empty.
        assertEquals("–", statText(null))
        assertEquals("0", statText(0))
        assertEquals("12", statText(12))
    }
}
