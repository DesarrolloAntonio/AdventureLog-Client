package com.desarrollodroide.adventurelog.feature.collections.ui.components

enum class CollectionTab(val title: String, val isEnabled: Boolean = true) {
    ALL("All"),
    LOCATIONS("Places"),

    /**
     * "Transportations" is what the API calls it. Nobody says that, and at six tabs the length
     * was what pushed the row past the width of a phone.
     */
    TRANSPORTATIONS("Transport"),
    LODGING("Lodging"),
    NOTES("Notes"),
    CHECKLISTS("Checklists")
}
