package com.desarrollodroide.adventurelog.feature.adventures.navigation

import kotlinx.serialization.Serializable

/**
 * Main route for the adventures graph
 */
@Serializable
data object AdventuresRoute

/**
 * Adventure list screen
 */
@Serializable
internal data object AdventuresScreen

/**
 * Screen with details of a specific adventure
 */
@Serializable
internal data object AdventureDetails {
    const val adventureIdArg = "adventureId"
    val routeWithArgs = "adventure_details/{$adventureIdArg}"
    
    fun createRoute(adventureId: String) = "adventure_details/$adventureId"
}