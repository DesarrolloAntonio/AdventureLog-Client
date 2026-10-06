package com.desarrollodroide.adventurelog.core.network.api

import com.desarrollodroide.adventurelog.core.model.Note

/**
 * Notes attached to a collection.
 *
 * Read support existed - the collection endpoint returns them - but nothing could write one, so
 * the Notes tab could only ever be empty.
 */
interface NoteApi {
    suspend fun createNote(
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean,
        collectionId: String
    ): Note

    suspend fun updateNote(
        noteId: String,
        name: String,
        content: String,
        date: String?,
        isPublic: Boolean
    ): Note

    suspend fun deleteNote(noteId: String)
}
