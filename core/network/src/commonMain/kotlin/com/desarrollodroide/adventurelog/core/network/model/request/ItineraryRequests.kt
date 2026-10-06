package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutoGenerateItineraryRequest(
    @SerialName("collection_id")
    val collectionId: String
)

@Serializable
data class ItineraryEntryRequest(
    @SerialName("collection")
    val collection: String,

    @SerialName("content_type")
    val contentType: String,

    @SerialName("object_id")
    val objectId: String,

    /**
     * The server rejects a dated entry with no date and a global one with a date, so exactly one
     * of these carries the answer: a null date means the trip-context bucket.
     */
    @SerialName("date")
    val date: String?,

    @SerialName("is_global")
    val isGlobal: Boolean,

    @SerialName("order")
    val order: Int
)
