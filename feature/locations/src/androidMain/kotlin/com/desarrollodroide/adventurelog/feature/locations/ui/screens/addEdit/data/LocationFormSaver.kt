package com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data

import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.json.Json

private val formJson = Json { ignoreUnknownKeys = true }

/**
 * Keeps the place form across a process death as one JSON string - a half-filled form came back
 * empty after the system killed the app in the background (measured).
 */
val LocationFormSaver: Saver<LocationFormData, String> = Saver(
    save = { formJson.encodeToString(LocationFormData.serializer(), it) },
    restore = { runCatching { formJson.decodeFromString(LocationFormData.serializer(), it) }.getOrNull() }
)
