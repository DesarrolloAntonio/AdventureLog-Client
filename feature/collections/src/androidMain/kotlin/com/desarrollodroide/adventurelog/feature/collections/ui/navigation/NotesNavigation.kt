package com.desarrollodroide.adventurelog.feature.collections.ui.navigation

import com.desarrollodroide.adventurelog.feature.ui.navigation.routeArgument
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEditNote.AddEditNoteScreen
import kotlinx.serialization.json.Json

interface NotesNavigator {
    fun navigateToAddNote(collectionId: String)
    fun navigateToEditNote(collectionId: String, noteId: String, noteJson: String)
    fun navigateBack()
}

fun NavGraphBuilder.notesScreen(navigator: NotesNavigator) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    composable(
        route = NavigationRoutes.Collections.Notes.addRoute,
        arguments = listOf(
            navArgument("collectionId") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { entry ->
        AddEditNoteScreen(
            collectionId = entry.routeArgument("collectionId"),
            existingNote = null,
            onDone = { navigator.navigateBack() },
            onCancel = { navigator.navigateBack() }
        )
    }

    composable(
        route = NavigationRoutes.Collections.Notes.editRoute,
        arguments = listOf(
            navArgument("collectionId") { type = NavType.StringType },
            navArgument("noteId") { type = NavType.StringType },
            navArgument("noteJson") { type = NavType.StringType }
        )
    ) { entry ->
        val noteJson = entry.routeArgument("noteJson")
        val note = if (noteJson.isNotEmpty()) {
            runCatching { json.decodeFromString<Note>(noteJson) }.getOrNull()
        } else {
            null
        }

        AddEditNoteScreen(
            collectionId = entry.routeArgument("collectionId"),
            existingNote = note,
            onDone = { navigator.navigateBack() },
            onCancel = { navigator.navigateBack() }
        )
    }
}
