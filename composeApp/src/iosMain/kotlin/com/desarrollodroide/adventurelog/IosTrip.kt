package com.desarrollodroide.adventurelog

import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.feature.collections.ui.state.CollectionItinerary
import com.desarrollodroide.adventurelog.feature.collections.ui.state.itinerary

/**
 * The trip laid out by day, for Swift. `Collection.itinerary()` reaches Swift as a method named
 * like the `itinerary` property, and Swift always picks the property.
 */
object IosTrip {
    fun plan(collection: Collection): CollectionItinerary = collection.itinerary()
}
