package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

/**
 * What a location's page can navigate to.
 */
interface DetailNavigator {
    fun navigateUp()

    /**
     * Open one of the collections this location belongs to. The chips were tappable before and
     * printed the collection's name to stdout.
     */
    fun navigateToCollection(collectionId: String, collectionName: String)
}
