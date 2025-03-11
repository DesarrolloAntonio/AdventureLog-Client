package com.desarrollodroide.adventurelog.feature.home.model

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data model for drawer navigation items
 */
data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val badgeCount: Int = 0,
    val onClick: () -> Unit
)