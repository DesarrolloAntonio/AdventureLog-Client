package com.desarrollodroide.adventurelog.feature.adventures.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AdventuresRoute {
    const val route = "adventures"
}

@Serializable
data object AdventuresScreen {
    const val route = "adventures/list"
}
