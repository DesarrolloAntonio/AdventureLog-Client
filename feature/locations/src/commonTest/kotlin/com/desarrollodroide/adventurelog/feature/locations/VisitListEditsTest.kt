package com.desarrollodroide.adventurelog.feature.locations

import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.editingIndexAfterDelete
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components.withVisitSaved
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Deleting one visit while another is open for editing, in the place form. */
class VisitListEditsTest {

    private fun visit(notes: String) = VisitFormData(id = notes, startDate = "2025-05-01", notes = notes)
    private val visits = listOf(visit("one"), visit("two"), visit("timed"), visit("no tz"))

    /** Open [editing], delete [deleted], press Update with the notes changed. */
    private fun updateAfterDelete(editing: Int, deleted: Int): List<VisitFormData> {
        val edited = visits[editing].copy(notes = "${visits[editing].notes} edited")
        val remaining = visits.filterIndexed { i, _ -> i != deleted }
        return remaining.withVisitSaved(editingIndexAfterDelete(editing, deleted), edited)
    }

    @Test
    fun `deleting an earlier visit still updates the visit that was open`() {
        // Editing the last of four and deleting the first crashed with Index 3 out of bounds.
        assertEquals(listOf("two", "timed", "no tz edited"), updateAfterDelete(editing = 3, deleted = 0).map { it.notes })
        // In range, the same move wrote the change into the wrong visit.
        assertEquals(listOf("two", "timed edited", "no tz"), updateAfterDelete(editing = 2, deleted = 0).map { it.notes })
    }

    @Test
    fun `deleting a later visit leaves the open one where it was`() {
        assertEquals(listOf("one", "two edited", "timed"), updateAfterDelete(editing = 1, deleted = 3).map { it.notes })
    }

    @Test
    fun `deleting the visit that is open stops editing it`() {
        assertNull(editingIndexAfterDelete(editingIndex = 2, deletedIndex = 2))
    }
}
