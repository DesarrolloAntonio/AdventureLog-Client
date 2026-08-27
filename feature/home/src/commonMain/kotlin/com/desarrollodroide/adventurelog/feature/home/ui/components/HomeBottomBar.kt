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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
 */
@Composable
fun HomeBottomBar(
    current: CurrentScreen,
    onSelect: (CurrentScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        Destination.entries.forEach { destination ->
            val selected = destination.screen == current

            NavigationBarItem(
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
}

private enum class Destination(
    val screen: CurrentScreen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(CurrentScreen.HOME, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    LOCATIONS(CurrentScreen.ADVENTURES, "Places", Icons.Outlined.Explore, Icons.Filled.Explore),
    COLLECTIONS(
        CurrentScreen.COLLECTIONS,
        "Collections",
        Icons.Outlined.Collections,
        Icons.Filled.Collections
    ),
    MAP(CurrentScreen.MAP, "Map", Icons.Outlined.Map, Icons.Filled.Map),
    WORLD(CurrentScreen.TRAVEL, "World", Icons.Outlined.Public, Icons.Filled.Public)
}
