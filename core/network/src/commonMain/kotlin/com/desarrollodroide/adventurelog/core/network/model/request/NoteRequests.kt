package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body /api/notes/ expects.
 *
 * `date` is sent only when set: the server rejects an empty string for a date field, the same way
 * it rejected "UTC" as a timezone on transportations.
 */
@Serializable
data class NoteRequest(
    @SerialName("name")
    val name: String,
    @SerialName("content")
    val content: String,
    @SerialName("date")
    val date: String? = null,
    // No default: the client leaves out any field equal to its default, so `false` never went out
    // and a note could not be made private again (measured).
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("collection")
    val collection: String? = null
)
