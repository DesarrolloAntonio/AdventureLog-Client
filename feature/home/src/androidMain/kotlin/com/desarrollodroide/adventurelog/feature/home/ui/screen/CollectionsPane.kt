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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.Lodging

@Serializable
private data object CollectionsList : NavKey

@Serializable
internal data class CollectionDetail(val collectionId: String) : NavKey

/** The back stack once [deletedId] no longer exists - see PlacesPane.backStackWithoutDeleted. */
internal fun collectionsBackStackWithoutDeleted(stack: List<NavKey>, deletedId: String): List<NavKey> =
    stack.filterNot { it is CollectionDetail && it.collectionId == deletedId }

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
    /** The collection open in the detail pane, edited from its header. */
    onEditCollectionById: (String) -> Unit = {},
    onAdventureClick: (Location) -> Unit,
    onEditAdventure: (Location) -> Unit,
    /** The edit form opened on its images, for a card's "+ Add photo". */
    onAddPhoto: (Location) -> Unit = onEditAdventure,
    onAddTransportation: (String) -> Unit,
    onAddPlace: (String) -> Unit = {},
    onEditTransportation: (Transportation) -> Unit,
    onAddNote: (String) -> Unit,
    onEditNote: (String, Note) -> Unit,
    onAddChecklist: (String) -> Unit,
    onEditChecklist: (String, Checklist) -> Unit,
    onAddLodging: (String) -> Unit,
    onEditLodging: (String, Lodging) -> Unit,
    onHomeClick: () -> Unit,
    openInvites: Boolean = false,
    onInvitesOpened: () -> Unit = { },
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(collectionsNavConfiguration, CollectionsList)
    val listDetail = rememberQuietListDetailSceneStrategy<NavKey>()

    val twoPanes = rememberShowsTwoPanes()

    val listOnShow = remember { PaneListOnShow() }
    PaneHidesShellChrome(
        detailShown = !twoPanes && backStack.lastOrNull() is CollectionDetail && !listOnShow.isOnShow
    ) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            // One pane is a scene per page, so the slides above run between them; the list-detail
            // scene would keep both in one scene and fade its panes itself.
            sceneStrategies = if (twoPanes) listOf(listDetail) else emptyList(),
            onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
            transitionSpec = paneTransition(twoPanes),
            popTransitionSpec = panePopTransition(twoPanes),
            predictivePopTransitionSpec = panePredictivePopTransition(twoPanes),
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
                    listOnShow.Mark()
                    CollectionsScreen(
                        openInvites = openInvites,
                        onInvitesOpened = onInvitesOpened,
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
                        },
                        onCollectionDeleted = { deletedId ->
                            val kept = collectionsBackStackWithoutDeleted(backStack.toList(), deletedId)
                            if (kept.size != backStack.size) {
                                backStack.clear()
                                backStack.addAll(kept)
                            }
                        }
                    )
                }

                entry<CollectionDetail>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                    PanePage(twoPanes) {
                        CollectionDetailScreen(
                            collectionId = key.collectionId,
                            onBackClick = { backStack.removeLastOrNull() },
                            onHomeClick = onHomeClick,
                            onAdventureClick = onAdventureClick,
                            onEditAdventure = onEditAdventure,
                            onAddPhoto = onAddPhoto,
                            onAddTransportation = { onAddTransportation(key.collectionId) },
                            onAddPlace = { onAddPlace(key.collectionId) },
                            onEditTransportation = onEditTransportation,
                            onAddNote = onAddNote,
                            onEditNote = onEditNote,
                            onAddChecklist = onAddChecklist,
                            onEditChecklist = onEditChecklist,
                            onAddLodging = onAddLodging,
                            onEditLodging = onEditLodging,
                            showTitle = true,
                            showBack = !twoPanes,
                            onEditCollection = { onEditCollectionById(key.collectionId) }
                        )
                    }
                }
            }
        )
    }
}
