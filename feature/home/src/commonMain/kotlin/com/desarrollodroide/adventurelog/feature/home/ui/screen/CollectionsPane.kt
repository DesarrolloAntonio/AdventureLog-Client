package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
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
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionDetailScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.CollectionsScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
private data object CollectionsList : NavKey

@Serializable
private data class CollectionDetail(val collectionId: String) : NavKey

/** See the note in PlacesPane: NavKey is open, so its subclasses have to be declared. */
private val collectionsNavConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(CollectionsList::class, CollectionsList.serializer())
            subclass(CollectionDetail::class, CollectionDetail.serializer())
        }
    }
}

/**
 * Collections and a collection's contents, as two panes.
 *
 * The same shape as the places screen, and for the same reason: a trip is chosen by looking
 * between them, which is hard when opening one hides the rest.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CollectionsPane(
    onAddCollection: () -> Unit,
    onEditCollection: (UltraSlimCollection) -> Unit,
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    onAddTransportation: (String) -> Unit,
    onEditTransportation: (Transportation) -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(collectionsNavConfiguration, CollectionsList)
    val listDetail = rememberListDetailSceneStrategy<NavKey>()

    val twoPanes = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass !=
        WindowWidthSizeClass.COMPACT

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategies = listOf(listDetail),
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<CollectionsList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Pick a collection to see what is in it.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                )
            ) {
                CollectionsScreen(
                    onCollectionClick = { id, _ ->
                        // Swap the open collection rather than stacking them, so back leaves
                        // Collections instead of retracing everything opened along the way.
                        if (backStack.lastOrNull() is CollectionDetail) backStack.removeLastOrNull()
                        backStack.add(CollectionDetail(id))
                    },
                    onAddCollectionClick = onAddCollection,
                    onEditCollection = onEditCollection,
                    onFirstLoaded = { first ->
                        if (twoPanes && backStack.size == 1) backStack.add(CollectionDetail(first.id))
                    }
                )
            }

            entry<CollectionDetail>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                CollectionDetailScreen(
                    collectionId = key.collectionId,
                    onBackClick = { backStack.removeLastOrNull() },
                    onHomeClick = onHomeClick,
                    onAdventureClick = onAdventureClick,
                    onEditAdventure = onEditAdventure,
                    onAddTransportation = { onAddTransportation(key.collectionId) },
                    onEditTransportation = onEditTransportation,
                    showTitle = true
                )
            }
        }
    )
}
