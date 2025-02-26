package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue

/**
 * Animated drawer with Spring animation for a smoother bounce effect
 */
@Composable
fun AnimatedDrawerContent(
    drawerState: DrawerState,
    drawerWidth: Int = 300,
    extraBounceSpace: Int = 50,
    content: @Composable () -> Unit
) {
    val isDrawerOpen = drawerState.isOpen

    val offsetX by animateDpAsState(
        targetValue = if (isDrawerOpen) 0.dp else (-drawerWidth - 30).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    val alpha by animateFloatAsState(
        targetValue = if (isDrawerOpen) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )

    Box(
        modifier = Modifier
            .offset(x = offsetX)
            .alpha(alpha)
            .zIndex(1f)
    ) {
        content()
    }
}