package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import com.desarrollodroide.adventurelog.feature.home.model.NavigationItem

/**
 * Creates the navigation items list for the drawer
 */
@Composable
fun createNavigationItems(
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit
): List<NavigationItem> {
    return listOf(
        NavigationItem(
            title = "Adventures",
            icon = Icons.Outlined.Explore,
            selectedIcon = Icons.Filled.Explore,
            badgeCount = 3,
            onClick = onAdventuresClick
        ),
        NavigationItem(
            title = "Collections",
            icon = Icons.Outlined.Collections,
            selectedIcon = Icons.Filled.Collections,
            onClick = onCollectionsClick
        ),
        NavigationItem(
            title = "Travels",
            icon = Icons.Outlined.FlightTakeoff,
            selectedIcon = Icons.Filled.Flight,
            onClick = onTravelClick
        ),
        NavigationItem(
            title = "Map",
            icon = Icons.Outlined.Map,
            selectedIcon = Icons.Filled.Map,
            badgeCount = 1,
            onClick = onMapClick
        ),
        NavigationItem(
            title = "Calendar",
            icon = Icons.Outlined.CalendarMonth,
            selectedIcon = Icons.Filled.DateRange,
            onClick = onCalendarClick
        )
    )
}

/**
 * Creates the config items list for the drawer
 */
@Composable
fun createConfigItems(
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit
): List<NavigationItem> {
    return listOf(
        NavigationItem(
            title = "Settings",
            icon = Icons.Outlined.Settings,
            selectedIcon = Icons.Filled.Settings,
            onClick = onSettingsClick
        ),
        NavigationItem(
            title = "Help & Support",
            icon = Icons.Outlined.Help,
            selectedIcon = Icons.Outlined.Help, // No filled version available
            onClick = onHelpClick
        )
    )
}
