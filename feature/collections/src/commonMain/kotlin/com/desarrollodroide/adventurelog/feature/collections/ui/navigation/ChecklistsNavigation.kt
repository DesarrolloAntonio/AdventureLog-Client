package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import com.desarrollodroide.adventurelog.feature.ui.navigation.routeArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditChecklist.AddEditChecklistScreen
import kotlinx.serialization.json.Json

interface ChecklistsNavigator {
    fun navigateToAddChecklist(collectionId: String)
    fun navigateToEditChecklist(collectionId: String, checklistId: String, checklistJson: String)
    fun navigateBack()
}

fun NavGraphBuilder.checklistsScreen(navigator: ChecklistsNavigator) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    composable(
        route = NavigationRoutes.Collections.Checklists.addRoute,
        arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { entry ->
        AddEditChecklistScreen(
            collectionId = entry.routeArgument("collectionId"),
            existingChecklist = null,
            onDone = { navigator.navigateBack() },
            onCancel = { navigator.navigateBack() }
        )
    }

    composable(
        route = NavigationRoutes.Collections.Checklists.editRoute,
        arguments = listOf(
            navArgument("collectionId") { type = NavType.StringType },
            navArgument("checklistId") { type = NavType.StringType },
            navArgument("checklistJson") { type = NavType.StringType }
        )
    ) { entry ->
        val checklistJson = entry.routeArgument("checklistJson")
        val checklist = if (checklistJson.isNotEmpty()) {
            runCatching { json.decodeFromString<Checklist>(checklistJson) }.getOrNull()
        } else {
            null
        }

        AddEditChecklistScreen(
            collectionId = entry.routeArgument("collectionId"),
            existingChecklist = checklist,
            onDone = { navigator.navigateBack() },
            onCancel = { navigator.navigateBack() }
        )
    }
}
