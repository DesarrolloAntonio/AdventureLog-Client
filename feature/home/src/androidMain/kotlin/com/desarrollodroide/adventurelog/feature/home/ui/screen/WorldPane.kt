package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
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
import androidx.window.core.layout.WindowWidthSizeClass
import com.desarrollodroide.adventurelog.feature.world.ui.screen.CountryDetailScreen
import com.desarrollodroide.adventurelog.feature.world.ui.screen.WorldScreen
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

@Serializable
private data object WorldList : NavKey

@Serializable
internal data class CountryDetail(val countryCode: String) : NavKey

/** The keys have to be declared for the back stack to be saved; see placesNavConfiguration. */
private val worldNavConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(WorldList::class, WorldList.serializer())
            subclass(CountryDetail::class, CountryDetail.serializer())
        }
    }
}

/**
 * The countries and a country's regions, as two panes of one screen - as Places does it.
 *
 * On a phone the country replaces the list, as before. On a tablet they sit side by side and
 * choosing another country changes only the right-hand pane, which opens on the first visited
 * country rather than on nothing.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun WorldPane(
    onMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(worldNavConfiguration, WorldList)
    val listDetail = rememberQuietListDetailSceneStrategy<NavKey>()
    val twoPanes = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass !=
        WindowWidthSizeClass.COMPACT
    val selected = (backStack.lastOrNull() as? CountryDetail)?.countryCode

    fun open(code: String) {
        // Replace rather than stack, so Back leaves World instead of retracing every country.
        if (backStack.lastOrNull() is CountryDetail) backStack.removeLastOrNull()
        backStack.add(CountryDetail(code))
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategies = listOf(listDetail),
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<WorldList>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = { CountryPlaceholder() }
                )
            ) {
                WorldScreen(
                    onCountryClick = ::open,
                    onMapClick = onMapClick,
                    selectedCountryCode = if (twoPanes) selected else null,
                    onFirstLoaded = { code -> if (twoPanes && backStack.size == 1) open(code) }
                )
            }

            entry<CountryDetail>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                CountryDetailScreen(
                    countryCode = key.countryCode,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    showBack = !twoPanes
                )
            }
        }
    )
}

/** The right-hand pane before a country has been chosen. */
@Composable
private fun CountryPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Pick a country to see its regions here.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
