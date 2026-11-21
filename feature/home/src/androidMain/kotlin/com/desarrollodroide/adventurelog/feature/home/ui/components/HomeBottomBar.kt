package com.desarrollodroide.adventurelog.feature.home.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.ui.graphics.vector.ImageVector
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen

/**
 * The app's five places to be, always visible.
 *
 * This replaced a navigation drawer. Every switch used to cost two gestures - open the drawer,
 * then aim - starting from the top-left corner, the hardest one to reach one-handed; in an app
 * built around hopping between the list, the map and a collection, that adds up.
 *
 * Five is the ceiling for a bar like this, which is why World is a hub rather than a leaf:
 * countries, regions and cities live under it instead of each claiming a slot.
 *
 * These are supplied as navigation-suite items rather than drawn as a bar, so the same five
 * destinations become a bottom bar on a phone and a rail down the side on a tablet - where a bar
 * pinned to the bottom edge of a large screen is a long way from where the hands are.
 */
fun NavigationSuiteScope.homeNavigationItems(
    current: CurrentScreen,
    onSelect: (CurrentScreen) -> Unit
) {
    Destination.entries.forEach { destination ->
        val selected = destination.screen == current

        item(
            selected = selected,
            onClick = { onSelect(destination.screen) },
            icon = {
                Icon(
                    imageVector = if (selected) destination.selectedIcon else destination.icon,
                    contentDescription = null
                )
            },
            label = { Text(destination.label) }
        )
    }
}

private enum class Destination(
    val screen: CurrentScreen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(CurrentScreen.HOME, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    PLACES(CurrentScreen.PLACES, "Places", Icons.Outlined.Explore, Icons.Filled.Explore),
    COLLECTIONS(
        CurrentScreen.COLLECTIONS,
        "Collections",
        Icons.Outlined.Collections,
        Icons.Filled.Collections
    ),
    MAP(CurrentScreen.MAP, "Map", Icons.Outlined.Map, Icons.Filled.Map),
    WORLD(CurrentScreen.TRAVEL, "World", Icons.Outlined.Public, Icons.Filled.Public)
}
