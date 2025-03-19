package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object AdventureDetailRoute {
    const val route = "detail"
}

@Serializable
data class AdventureDetailScreen(
    val adventureId: String
) {
    companion object {
        const val route = "detail/{adventureId}"
        fun createRoute(adventureId: String) = "detail/$adventureId"
    }
}
