package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.components

import com.desarrollodroide.adventurelog.core.model.VisitFormData

/**
 * The visit open for editing is known by its position, so deleting another visit moves it. Left
 * alone, Update wrote into whichever visit now held that position - or past the end of the list,
 * which crashed the app with the whole edit unsaved (measured).
 *
 * Returns the position of the visit being edited after [deletedIndex] is removed, or null when the
 * deleted visit was that one.
 */
internal fun editingIndexAfterDelete(editingIndex: Int?, deletedIndex: Int): Int? = when {
    editingIndex == null -> null
    deletedIndex == editingIndex -> null
    deletedIndex < editingIndex -> editingIndex - 1
    else -> editingIndex
}

/** The visits after Add or Update: the edited position replaced, or [visit] appended. */
internal fun List<VisitFormData>.withVisitSaved(editingIndex: Int?, visit: VisitFormData): List<VisitFormData> =
    if (editingIndex != null && editingIndex in indices) {
        toMutableList().apply { set(editingIndex, visit) }
    } else {
        this + visit
    }
