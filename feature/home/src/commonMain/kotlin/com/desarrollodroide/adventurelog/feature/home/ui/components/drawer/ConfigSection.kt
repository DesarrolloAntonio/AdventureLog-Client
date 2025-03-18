package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Animated version of the configuration section for the drawer
 */
@Composable
fun ConfigSectionAnimated(
    onSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    isSettingsSelected: Boolean = false,
    visible: Boolean,
    delayMillis: Int
) {
    val headerDelayMillis = delayMillis
    val settingsDelayMillis = delayMillis + 50
    val helpDelayMillis = delayMillis + 100
    val footerDelayMillis = delayMillis + 150
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            // Animated divider
            val dividerOffsetY by animateDpAsState(
                targetValue = if (visible) 0.dp else 20.dp,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = headerDelayMillis,
                    easing = FastOutSlowInEasing
                ),
                label = "dividerOffset"
            )
            
            val dividerAlpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = headerDelayMillis
                ),
                label = "dividerAlpha"
            )
            
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = dividerOffsetY.toPx()
                        alpha = dividerAlpha
                    }
            ) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 16.dp)
                )
            }

            // Section title with animation
            val headerOffsetX by animateDpAsState(
                targetValue = if (visible) 0.dp else (-30).dp,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = headerDelayMillis,
                    easing = FastOutSlowInEasing
                ),
                label = "headerOffsetX"
            )
            
            val headerAlpha by animateFloatAsState(
                targetValue = if (visible) 1f else 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = headerDelayMillis
                ),
                label = "headerAlpha"
            )
            
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = headerOffsetX.toPx()
                        alpha = headerAlpha
                    }
            ) {
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
            }

            // Use the common DrawerItemAnimated component for settings options
            DrawerItemAnimated(
                title = "Settings",
                icon = Icons.Outlined.Settings,
                selectedIcon = Icons.Filled.Settings,
                isSelected = isSettingsSelected,
                onClick = onSettingsClick,
                visible = visible,
                delayMillis = settingsDelayMillis
            )

            DrawerItemAnimated(
                title = "Help & Support",
                icon = Icons.Outlined.Help,
                isSelected = false,
                onClick = onHelpClick,
                visible = visible,
                delayMillis = helpDelayMillis
            )
        }

        // Animated footer
        val footerOffsetY by animateDpAsState(
            targetValue = if (visible) 0.dp else 20.dp,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = footerDelayMillis,
                easing = FastOutSlowInEasing
            ),
            label = "footerOffsetY"
        )
        
        val footerAlpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = footerDelayMillis
            ),
            label = "footerAlpha"
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .graphicsLayer {
                    translationY = footerOffsetY.toPx()
                    alpha = footerAlpha
                },
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