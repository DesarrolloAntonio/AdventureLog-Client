package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.model.Checklist

/**
 * Checklists on a collection, with their items.
 *
 * The server takes the whole checklist including its items in one body, so there is no separate
 * item endpoint to call: ticking a line is a save of the list it belongs to.
 */
interface ChecklistApi {
    suspend fun createChecklist(
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): Checklist

    suspend fun updateChecklist(
        checklistId: String,
        name: String,
        items: List<Pair<String, Boolean>>,
        date: String?,
        isPublic: Boolean
    ): Checklist

    suspend fun deleteChecklist(checklistId: String)
}
