package com.desarrollodroide.adventurelog.core.model

/**
 * The files a collection can be turned into, all rendered by the server so what leaves the phone
 * looks the same as what leaves the web.
 */
enum class CollectionExport(val fileExtension: String) {
    /** A branded PNG card for sharing. */
    SHARE_CARD("png"),

    /** The day-by-day itinerary, printable. */
    PDF("pdf"),

    /** The collection and everything in it. */
    ZIP("zip")
}
