package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.collections.ui.state.summary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** QA CO-19: a transport's card showed its name and type, never when it leaves or where it goes. */
class TransportSummaryTest {

    private fun leg(date: String? = null, from: String? = null, to: String? = null) = Transportation(
        id = "t1", user = "u", type = "train", name = "QA_Train", date = date,
        fromLocation = from, toLocation = to, createdAt = "", updatedAt = ""
    )

    @Test
    fun `the card says when it leaves and where it goes`() {
        assertEquals("2 Nov 2026 · Madrid → Barcelona", leg("2026-11-02T08:00:00Z", "Madrid", "Barcelona").summary())
    }

    @Test
    fun `half a route still says which half`() {
        assertEquals("From Madrid", leg(from = "Madrid").summary())
        assertEquals("2 Nov 2026 · To Barcelona", leg("2026-11-02", to = "Barcelona").summary())
    }

    @Test
    fun `a leg with nothing dated or placed adds no line`() {
        assertNull(leg(from = " ").summary())
    }
}
