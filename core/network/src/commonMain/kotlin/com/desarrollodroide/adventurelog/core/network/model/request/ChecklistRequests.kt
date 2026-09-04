package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One line of a checklist as the server wants it: a name and whether it is ticked. */
@Serializable
data class ChecklistItemRequest(
    @SerialName("name")
    val name: String,
    @SerialName("is_checked")
    val isChecked: Boolean = false
)

@Serializable
data class ChecklistRequest(
    @SerialName("name")
    val name: String,
    @SerialName("items")
    val items: List<ChecklistItemRequest> = emptyList(),
    @SerialName("date")
    val date: String? = null,
    @SerialName("is_public")
    val isPublic: Boolean = false,
    @SerialName("collection")
    val collection: String? = null
)
