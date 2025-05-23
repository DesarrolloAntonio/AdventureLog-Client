package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.animation.core.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlin.random.Random

/**
 * Reusable drawer content between modal and permanent versions
 */
@Composable
fun DrawerContent(
    userName: String,
    adventureCount: Int,
    userEmail: String? = null,
    profileImageUrl: String? = null,
    currentScreen: CurrentScreen,
    onHomeClick: () -> Unit,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    drawerOpen: Boolean // Parameter to control drawer visibility
) {
    // Get the current selected index directly from CurrentScreen
    val selectedItemIndex = currentScreen.index
    
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
                userEmail = userEmail,
                profileImageUrl = profileImageUrl,
                currentScreen = currentScreen,
                visible = visible,
                drawerOpen = drawerOpen,
                onHomeClick = onHomeClick,
                onAdventuresClick = onAdventuresClick,
                onCollectionsClick = onCollectionsClick,
                onTravelClick = onTravelClick,
                onMapClick = onMapClick,
                onCalendarClick = onCalendarClick,
                onSettingsClick = onSettingsClick,
                onLogout = onLogout,
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
    userEmail: String? = null,
    profileImageUrl: String? = null,
    currentScreen: CurrentScreen,
    visible: Boolean,
    drawerOpen: Boolean,
    onHomeClick: () -> Unit,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onHelpClick: () -> Unit = {}
) {
    // Colors for gradient background - matching website dark theme
    val darkBgColor = Color(0xFF1E232B) // Slate dark color matching website cards
    val accentColor = Color(0xFF252B35) // Slightly lighter accent for subtle gradient
    val gradientOverlayColor = Color(0xFF2C313D) // Pattern color

    // Gradient background
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(darkBgColor, accentColor, darkBgColor)
    )

    // Navigation items section
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .drawBehind {
                // Subtle diagonal lines pattern
                val patternColor = gradientOverlayColor.copy(alpha = 0.15f)
                val lineWidth = 1.5f
                val spacing = 40f
                
                // Draw diagonal lines in background
                for (i in 0..(size.width + size.height).toInt() step spacing.toInt()) {
                    val startX = minOf(i.toFloat(), size.width)
                    val startY = if (i > size.width) i - size.width else 0f
                    
                    drawLine(
                        color = patternColor,
                        start = Offset(startX, startY),
                        end = Offset(
                            if (startY + (size.width - startX) < size.height) 0f else startX - (size.height - startY),
                            if (startY + (size.width - startX) < size.height) startY + (size.width - startX) else size.height
                        ),
                        strokeWidth = lineWidth
                    )
                }
                
                // Subtle glow effect at the top
                val glowBrush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF5038FF).copy(alpha = 0.08f), // Matching the purple from website buttons
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2, 0f),
                    radius = size.width * 0.8f
                )
                
                drawRect(
                    brush = glowBrush,
                    size = size
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // User profile header
                DrawerHeader(
                    userName = userName,
                    email = userEmail,
                    profileImageUrl = profileImageUrl,
                    visible = visible,
                    onLogout = onLogout
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Animate the Adventures section title
                AnimatedSectionTitle(
                    title = "MY ADVENTURES",
                    visible = visible,
                    delayMillis = calculateDelayMillis(1, totalItems, drawerOpen)
                )

                navigationItems.forEachIndexed { index, item ->
                    val itemDelayMillis = calculateDelayMillis(index + 2, totalItems, drawerOpen)
                    val screenForThisIndex = CurrentScreen.fromIndex(index)
                    
                    DrawerItemAnimated(
                        title = item.title,
                        icon = item.icon,
                        selectedIcon = item.selectedIcon,
                        isSelected = currentScreen == screenForThisIndex,
                        badgeCount = item.badgeCount,
                        onClick = item.onClick,
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
                    
                    // Settings is at position 6 in the global index
                    val isItemSelected = index == 0 && currentScreen == CurrentScreen.SETTINGS
                    
                    DrawerItemAnimated(
                        title = item.title,
                        icon = item.icon,
                        selectedIcon = item.selectedIcon,
                        isSelected = isItemSelected,
                        onClick = item.onClick,
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
            color = Color(0xFF5038FF), // Purple from website buttons
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
                .padding(vertical = 16.dp, horizontal = 16.dp),
            color = Color.White.copy(alpha = 0.2f) // Subtle white divider with transparency
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
            color = Color.White.copy(alpha = 0.6f), // Semi-transparent white text
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
        Color(0xFF5038FF) // Purple from website buttons
    } else {
        Color.White.copy(alpha = 0.8f) // White for non-selected items
    }
    
    // Apply background with rounded shape on the right side if selected
    val backgroundModifier = if (isSelected) {
        Modifier.background(
            color = Color(0xFF5038FF).copy(alpha = 0.15f), // Semi-transparent purple
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
                            color = Color(0xFF5038FF), // Purple indicator
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
                    containerColor = Color(0xFFFF4081) // Pink-purple for notifications (from website color scheme)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White
                    )
                }
            }
        }
    }
}