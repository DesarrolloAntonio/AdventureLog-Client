package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.ItineraryDayNote
import com.desarrollodroide.adventurelog.core.model.ItineraryEntry
import com.desarrollodroide.adventurelog.core.model.ItineraryItemKind
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItineraryEntryDTO(
    @SerialName("id")
    val id: String,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("object_id")
    val objectId: String? = null,

    /**
     * The kind, twice over. `item.type` is the string the API accepts back on a write
     * ("location"); `object_name` is the same word from the model's own metadata; `content_type`
     * is a numeric primary key that means nothing outside this server's database. Reading the
     * string and ignoring the number is what keeps a restored backup from pointing at the wrong
     * kind of thing.
     */
    @SerialName("item")
    val item: ItineraryItemRefDTO? = null,

    @SerialName("object_name")
    val objectName: String? = null,

    @SerialName("date")
    val date: String? = null,

    @SerialName("is_global")
    val isGlobal: Boolean = false,

    @SerialName("order")
    val order: Int = 0
)

@Serializable
data class ItineraryItemRefDTO(
    @SerialName("id")
    val id: String? = null,

    @SerialName("type")
    val type: String? = null
)

@Serializable
data class ItineraryDayDTO(
    @SerialName("id")
    val id: String,

    @SerialName("collection")
    val collection: String? = null,

    @SerialName("date")
    val date: String,

    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null
)

/** Null for an entry pointing at a kind this client does not know, rather than a guess. */
fun ItineraryEntryDTO.toDomainModel(): ItineraryEntry? {
    val kind = ItineraryItemKind.fromWire(item?.type ?: objectName) ?: return null
    val itemId = item?.id ?: objectId ?: return null
    return ItineraryEntry(
        id = id,
        collectionId = collection ?: "",
        kind = kind,
        itemId = itemId,
        date = date,
        isGlobal = isGlobal,
        order = order
    )
}

fun ItineraryDayDTO.toDomainModel(): ItineraryDayNote = ItineraryDayNote(
    id = id,
    collectionId = collection ?: "",
    date = date,
    name = name ?: "",
    description = description ?: ""
)
