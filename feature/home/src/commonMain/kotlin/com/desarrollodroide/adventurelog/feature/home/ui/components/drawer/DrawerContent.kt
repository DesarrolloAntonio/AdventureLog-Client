package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen

/**
 * Reusable drawer content between modal and permanent versions
 */
@Composable
fun DrawerContent(
    userName: String,
    adventureCount: Int,
    currentScreen: CurrentScreen,
    onHomeClick: () -> Unit,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit = {},
    drawerOpen: Boolean // Parameter to control drawer visibility
) {
    // Convert current screen to corresponding navigation index
    val selectedItemIndex = when (currentScreen) {
        CurrentScreen.HOME -> 0
        CurrentScreen.ADVENTURES -> 1
        CurrentScreen.COLLECTIONS -> 2
        CurrentScreen.TRAVEL -> 3
        CurrentScreen.MAP -> 4
        CurrentScreen.CALENDAR -> 5
        CurrentScreen.SETTINGS -> 6
    }

    // Maintain selection state internally, but initialize it from currentScreen
    var selectedItem by rememberSaveable { mutableStateOf(selectedItemIndex) }
    if (selectedItem != selectedItemIndex) {
        selectedItem = selectedItemIndex
    }
    
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(drawerOpen) {
        visible = drawerOpen
    }

    // Advanced 3D animations
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else 45f,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 150f
        ),
        label = "rotation"
    )
    
    val offsetX by animateDpAsState(
        targetValue = if (visible) 0.dp else (-320).dp,
        animationSpec = spring(
            dampingRatio = 0.7f,
            stiffness = 200f
        ),
        label = "offsetX"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(300),
        label = "scale"
    )
    
    val opacity by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "opacity"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .width(280.dp)
                .graphicsLayer {
                    // Apply 3D transformations
                    rotationY = rotation
                    translationX = offsetX.toPx()
                    scaleX = scale
                    scaleY = scale
                    alpha = opacity
                    // Add perspective effect
                    cameraDistance = 8f * density
                    // Transform origin
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
        ) {
            DrawerContentBody(
                userName = userName,
                adventureCount = adventureCount,
                selectedItem = selectedItem,
                onSelectionChanged = { selectedItem = it },
                visible = visible,
                drawerOpen = drawerOpen,
                onHomeClick = onHomeClick,
                onAdventuresClick = onAdventuresClick,
                onCollectionsClick = onCollectionsClick,
                onTravelClick = onTravelClick,
                onMapClick = onMapClick,
                onCalendarClick = onCalendarClick,
                onSettingsClick = onSettingsClick,
                onHelpClick = onHelpClick
            )
        }
    }
}

/**
 * The actual content of the drawer without animation wrapper
 * Useful for previews and testing
 */
@Composable
fun DrawerContentBody(
    userName: String,
    adventureCount: Int,
    selectedItem: Int,
    onSelectionChanged: (Int) -> Unit,
    visible: Boolean,
    drawerOpen: Boolean,
    onHomeClick: () -> Unit,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit = {}
) {
    ModalDrawerSheet {
        // Get navigation and config items
        val navigationItems = createNavigationItems(
            onHomeClick = onHomeClick,
            onAdventuresClick = onAdventuresClick,
            onCollectionsClick = onCollectionsClick,
            onTravelClick = onTravelClick,
            onMapClick = onMapClick,
            onCalendarClick = onCalendarClick
        )
        
        val configItems = createConfigItems(
            onSettingsClick = onSettingsClick,
            onHelpClick = onHelpClick
        )
        
        // Total items count for delay calculations
        val totalItems = 8 + navigationItems.size + configItems.size 
        // (Header + 2 section titles + divider + footer + all items)
        
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))

                // Animate the Adventures section title
                AnimatedSectionTitle(
                    title = "MY ADVENTURES",
                    visible = visible,
                    delayMillis = calculateDelayMillis(1, totalItems, drawerOpen)
                )

                // Navigation items section
                navigationItems.forEachIndexed { index, item ->
                    val itemDelayMillis = calculateDelayMillis(index + 2, totalItems, drawerOpen)
                    
                    DrawerItemAnimated(
                        title = item.title,
                        icon = item.icon,
                        selectedIcon = item.selectedIcon,
                        isSelected = selectedItem == index,
                        badgeCount = item.badgeCount,
                        onClick = {
                            if (selectedItem != index) {
                                onSelectionChanged(index)
                                item.onClick()
                            }
                        },
                        visible = visible,
                        delayMillis = itemDelayMillis
                    )
                }

                // Divider with animation
                val dividerDelayMillis = calculateDelayMillis(navigationItems.size + 2, totalItems, drawerOpen)
                AnimatedDivider(
                    visible = visible,
                    delayMillis = dividerDelayMillis
                )
                
                // Settings section title
                AnimatedSectionTitle(
                    title = "SETTINGS",
                    visible = visible,
                    delayMillis = dividerDelayMillis + 50
                )

                // Config items section
                configItems.forEachIndexed { index, item ->
                    val configBaseIndex = navigationItems.size + 4 // Header + adventure title + divider + settings title
                    val itemDelayMillis = calculateDelayMillis(configBaseIndex + index, totalItems, drawerOpen)
                    
                    // Settings is at position 6 in the global index (after adding Home)
                    val isItemSelected = index == 0 && selectedItem == 6
                    
                    DrawerItemAnimated(
                        title = item.title,
                        icon = item.icon,
                        selectedIcon = item.selectedIcon,
                        isSelected = isItemSelected,
                        onClick = {
                            if (index == 0 && selectedItem != 6) { // Settings item
                                onSelectionChanged(6)
                            }
                            item.onClick()
                        },
                        visible = visible,
                        delayMillis = itemDelayMillis
                    )
                }
            }

            // Footer with version
            AnimatedFooter(
                visible = visible,
                delayMillis = calculateDelayMillis(totalItems - 1, totalItems, drawerOpen)
            )
        }
    }
}

@Composable
fun AnimatedSectionTitle(
    title: String,
    visible: Boolean,
    delayMillis: Int
) {
    val titleOffsetX by animateDpAsState(
        targetValue = if (visible) 0.dp else (-30).dp,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "titleOffsetX"
    )
    
    val titleAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis
        ),
        label = "titleAlpha"
    )
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = titleOffsetX.toPx()
                alpha = titleAlpha
            }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                .semantics {
                    contentDescription = "Section: $title"
                    heading()
                }
        )
    }
}

@Composable
fun AnimatedDivider(
    visible: Boolean,
    delayMillis: Int
) {
    val dividerOffsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 20.dp,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "dividerOffset"
    )
    
    val dividerAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis
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
}

@Composable
fun AnimatedFooter(
    visible: Boolean,
    delayMillis: Int
) {
    val footerOffsetY by animateDpAsState(
        targetValue = if (visible) 0.dp else 20.dp,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "footerOffsetY"
    )
    
    val footerAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis
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
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = "Application version: Adventure Log version 1.0"
            }
        )
    }
}

// Function to calculate animation delay based on order
private fun calculateDelayMillis(index: Int, totalItems: Int, drawerOpen: Boolean): Int {
    return if (drawerOpen) {
        // When opening, elements appear from top to bottom
        50 * index
    } else {
        // When closing, elements disappear from bottom to top
        50 * (totalItems - index - 1)
    }
}

/**
 * Common animated drawer item used for both navigation and settings
 */
@Composable
fun DrawerItemAnimated(
    title: String,
    icon: ImageVector,
    selectedIcon: ImageVector? = null, // Optional for settings items that might not have a selected icon
    isSelected: Boolean = false,
    badgeCount: Int = 0,
    onClick: () -> Unit = {},
    visible: Boolean,
    delayMillis: Int
) {
    val animatedOffsetX by animateDpAsState(
        targetValue = if (visible) 0.dp else (-50).dp,
        animationSpec = tween(
            durationMillis = 300,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "itemOffsetX"
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            delayMillis = delayMillis
        ),
        label = "itemAlpha"
    )
    
    // Calculate color based on selection
    val itemColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    }
    
    // Apply background with rounded shape on the right side if selected
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
    val badgeDescription = if (badgeCount > 0) "with $badgeCount notifications" else ""
    val accessibilityDescription = "$title item, $itemState $badgeDescription"
    
    // Apply animation to the container
    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = animatedOffsetX.toPx()
                alpha = animatedAlpha
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(end = 16.dp)
                .then(backgroundModifier)
                .clickable(onClick = onClick)
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
                imageVector = if (isSelected && selectedIcon != null) selectedIcon else icon,
                contentDescription = null, // Description already set in Row
                tint = itemColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Title
            Text(
                text = title,
                style = if (isSelected) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                color = itemColor,
                modifier = Modifier.weight(1f)
            )
            
            // Badge for notifications
            if (badgeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}