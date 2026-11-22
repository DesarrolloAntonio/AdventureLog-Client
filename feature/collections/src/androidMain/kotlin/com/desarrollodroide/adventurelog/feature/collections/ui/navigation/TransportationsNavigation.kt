package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditTransportation.AddEditTransportationScreen
import kotlinx.serialization.json.Json

interface TransportationsNavigator {
    fun navigateToAddTransportation(collectionId: String)
    fun navigateToEditTransportation(transportationId: String, transportationJson: String)
    fun navigateBack()
}

fun NavGraphBuilder.transportationsScreen(
    navigator: TransportationsNavigator
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    composable(
        route = NavigationRoutes.Collections.Transportations.addRoute,
        arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { backStackEntry ->
        AddEditTransportationScreen(
            transportationId = null,
            transportation = null,
            collectionId = backStackEntry.savedStateHandle.get<String>("collectionId"),
            onNavigateBack = {
                navigator.navigateBack()
            }
        )
    }

    composable(
        route = NavigationRoutes.Collections.Transportations.editRoute,
        arguments = listOf(
            navArgument("transportationId") {
                type = NavType.StringType
            },
            navArgument("transportationJson") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry ->
        val transportationId = backStackEntry.savedStateHandle.get<String>("transportationId") ?: ""
        val transportationJson = backStackEntry.savedStateHandle.get<String>("transportationJson") ?: ""
        
        val transportation = if (transportationJson.isNotEmpty()) {
            json.decodeFromString<Transportation>(transportationJson)
        } else {
            null
        }

        AddEditTransportationScreen(
            transportationId = transportationId,
            transportation = transportation,
            onNavigateBack = {
                navigator.navigateBack()
            }
        )
    }
}
