package com.desarrollodroide.adventurelog.feature.home.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.model.NavigationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
 * Navigation items section in the drawer
 */
@Composable
fun NavigationItemsList(
    navigationItems: List<NavigationItem>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    scope: CoroutineScope,
    onCloseDrawer: suspend () -> Unit
) {
    // Section header
    Text(
        text = "MY ADVENTURES",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            .semantics {
                contentDescription = "Section: My Adventures"
                heading()
            }
    )

    // Navigation items with animation
    navigationItems.forEachIndexed { index, item ->
        val isSelected = selectedItem == index
        val itemColor = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        val backgroundModifier = if (isSelected) {
            Modifier.background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            )
        } else {
            Modifier
        }

        // Accessibility description
        val itemState = if (isSelected) "selected" else "not selected"
        val badgeDescription = if (item.badgeCount > 0) "with ${item.badgeCount} notifications" else ""
        val accessibilityDescription = "${item.title} navigation item, $itemState $badgeDescription"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(end = 16.dp)
                .then(backgroundModifier)
                .clickable(
                    onClick = {
                        // If already selected, do nothing
                        if (!isSelected) {
                            onItemSelected(index)
                            // Call onClick directly to avoid issues with scope
                            item.onClick()
                        }
                    }
                )
                .padding(start = 16.dp, end = 8.dp)
                .clearAndSetSemantics {
                    contentDescription = accessibilityDescription
                    role = Role.Button
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Icon
            Icon(
                imageVector = if (isSelected) item.selectedIcon else item.icon,
                contentDescription = null, // Main description already set in Row
                tint = itemColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = itemColor,
                modifier = Modifier.weight(1f)
            )

            // Badge for notifications
            if (item.badgeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = item.badgeCount.toString(),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}