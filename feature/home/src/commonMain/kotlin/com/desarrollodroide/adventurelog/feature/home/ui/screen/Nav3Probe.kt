package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable

/**
 * Not wired to anything yet: this is the shape the Places list and a place's page will take, kept
 * compiling so the migration starts from something known to work on both platforms rather than
 * from a guess.
 *
 * Three things this pinned down, none of them obvious from the documentation:
 *
 * - `entry` is a member of `EntryProviderScope`, so it needs no import of its own; importing it
 *   is an unresolved reference.
 * - `NavDisplay` takes `sceneStrategies` - a list - not a single `sceneStrategy`.
 * - `rememberNavBackStack()` has an Android-only overload that takes just the keys. The one in
 *   common code wants a `SavedStateConfiguration` first, so shared code must pass one or it
 *   compiles on Android and fails on iOS.
 */
@Serializable
internal data object ProbeList : NavKey

@Serializable
internal data class ProbeDetail(val id: String) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun Nav3ListDetailProbe() {
    val backStack = rememberNavBackStack(SavedStateConfiguration.DEFAULT, ProbeList)
    val listDetail = rememberListDetailSceneStrategy<NavKey>()

    NavDisplay(
        backStack = backStack,
        sceneStrategies = listOf(listDetail),
        entryProvider = entryProvider {
            entry<ProbeList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = { Text("Pick something on the left") }
                )
            ) { Text("list") }

            entry<ProbeDetail>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                Text("detail ${key.id}")
            }
        }
    )
}
