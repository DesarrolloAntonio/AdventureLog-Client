package com.desarrollodroide.adventurelog.feature.detail.ui.navigation

import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import kotlinx.serialization.Serializable

/**
 * This class is for internal use in the detail module only.
 * For navigation between modules, use NavigationRoutes from core.common
 */
@Serializable
data object AdventureDetailRoute {
    const val route = NavigationRoutes.Detail.route
}

/**
 * For internal use only. For navigation from other modules, use NavigationRoutes.
 */
@Serializable
data class AdventureDetailScreen(
    val adventureId: String
) {
    companion object {
        const val route = "detail/{adventureId}"
        fun createRoute(adventureId: String) = NavigationRoutes.Detail.createDetailRoute(adventureId)
    }
}
