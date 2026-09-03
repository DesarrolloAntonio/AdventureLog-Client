package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.detail.ui.screen.AdventureDetailScreenRoute
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList.LocationListScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
private data object PlacesList : NavKey

@Serializable
private data class PlaceDetail(val locationId: String) : NavKey

/**
 * The back stack is persisted by serialising its keys, and NavKey is an open type, so the keys
 * have to be declared or it throws the moment the screen is composed. SavedStateConfiguration
 * .DEFAULT does not do this for you - it compiles happily and then fails at runtime.
 */
private val placesNavConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(PlacesList::class, PlacesList.serializer())
            subclass(PlaceDetail::class, PlaceDetail.serializer())
        }
    }
}

/**
 * The places list and a place's page, as two panes of one screen.
 *
 * On a phone this is what it always was: the list, and tapping something replaces it. Given the
 * width of a tablet the two sit side by side, and tapping a card changes only the right-hand
 * side - which is the whole point, since choosing between places means comparing them, and the
 * old behaviour threw the list away to show you one.
 *
 * The pane split is Navigation 3's to make: the scene strategy reads the window and decides. This
 * is the only part of the app on Navigation 3 so far; everything else still runs on the older
 * NavHost, and the two coexist because a NavDisplay is just a composable.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PlacesPane(
    onAddPlace: () -> Unit,
    onEditPlace: (Location) -> Unit,
    onCollectionClick: (UltraSlimCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(placesNavConfiguration, PlacesList)
    val listDetail = rememberListDetailSceneStrategy<NavKey>()

    // The detail is a pane beside the list, not a screen on top of it, whenever the window is
    // wide enough for the strategy to show both - and a back arrow pointing at a list that never
    // went anywhere is just a wrong button.
    val twoPanes = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass !=
        WindowWidthSizeClass.COMPACT

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategies = listOf(listDetail),
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<PlacesList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = { PlaceholderPane() }
                )
            ) {
                LocationListScreen(
                    onAdventureClick = { location ->
                        // Replace rather than stack: on two panes the list stays put and only the
                        // right side changes, so a back press should leave Places, not walk back
                        // through every place looked at on the way.
                        if (backStack.lastOrNull() is PlaceDetail) backStack.removeLastOrNull()
                        backStack.add(PlaceDetail(location.id))
                    },
                    onAddAdventureClick = onAddPlace,
                    onEditAdventure = onEditPlace
                )
            }

            entry<PlaceDetail>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                AdventureDetailScreenRoute(
                    locationId = key.locationId,
                    onBackClick = { backStack.removeLastOrNull() },
                    onCollectionClick = onCollectionClick,
                    showBack = !twoPanes
                )
            }
        }
    )
}

/** What the right-hand pane shows before anything has been chosen. */
@Composable
private fun PlaceholderPane() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pick a place to see it here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
