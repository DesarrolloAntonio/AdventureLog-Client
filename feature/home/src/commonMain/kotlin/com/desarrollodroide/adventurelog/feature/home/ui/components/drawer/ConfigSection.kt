package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider

/**
 * Configuration section for the drawer wrapped in a root Column
 */
@Composable
fun ConfigSection(
    onSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    isSettingsSelected: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 16.dp)
            )

            // Configuration header
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 8.dp)
                    .semantics {
                        contentDescription = "Section: Settings"
                        heading()
                    }
            )

            // Configuration options
            ConfigOption(
                icon = if (isSettingsSelected) Icons.Filled.Settings else Icons.Outlined.Settings,
                title = "Settings",
                onClick = onSettingsClick,
                isSelected = isSettingsSelected
            )

            ConfigOption(
                icon = Icons.Outlined.Help,
                title = "Help & Support",
                onClick = onHelpClick,
                isSelected = false
            )
        }

        // Drawer footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Adventure Log v1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "Application version: Adventure Log version 1.0"
                }
            )
        }
    }
}

/**
 * Individual configuration option item
 */
@Composable
fun ConfigOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {},
    isSelected: Boolean = false
) {
    val itemColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val backgroundModifier = if (isSelected) {
        Modifier.background(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
        )
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .then(backgroundModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp)
            .clearAndSetSemantics {
                contentDescription = "$title option"
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

        Icon(
            imageVector = icon,
            contentDescription = null, // Description already set in the Row
            tint = itemColor
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = itemColor
        )
    }
}