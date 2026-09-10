package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import com.desarrollodroide.adventurelog.feature.ui.navigation.routeArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Lodging
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditLodging.AddEditLodgingScreen
import kotlinx.serialization.json.Json

interface LodgingNavigator {
    fun navigateToAddLodging(collectionId: String)
    fun navigateToEditLodging(collectionId: String, lodgingId: String, lodgingJson: String)
    fun navigateBack()
}

fun NavGraphBuilder.lodgingScreen(navigator: LodgingNavigator) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    composable(
        route = NavigationRoutes.Collections.Lodgings.addRoute,
        arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { entry ->
        AddEditLodgingScreen(
            collectionId = entry.routeArgument("collectionId"),
            existingLodging = null,
            onDone = { navigator.navigateBack() },
            onCancel = { navigator.navigateBack() }
        )
    }

    composable(
        route = NavigationRoutes.Collections.Lodgings.editRoute,
        arguments = listOf(
            navArgument("collectionId") { type = NavType.StringType },
            navArgument("lodgingId") { type = NavType.StringType },
            navArgument("lodgingJson") { type = NavType.StringType }
        )
    ) { entry ->
        val lodgingJson = entry.routeArgument("lodgingJson")
        val lodging = if (lodgingJson.isNotEmpty()) {
            runCatching { json.decodeFromString<Lodging>(lodgingJson) }.getOrNull()
        } else {
            null
        }

        AddEditLodgingScreen(
            collectionId = entry.routeArgument("collectionId"),
            existingLodging = lodging,
            onDone = { navigator.navigateBack() },
            onCancel = { navigator.navigateBack() }
        )
    }
}
