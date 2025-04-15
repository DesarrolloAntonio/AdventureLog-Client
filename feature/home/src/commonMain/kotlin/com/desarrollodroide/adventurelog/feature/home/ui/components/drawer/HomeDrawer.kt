package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.model.fullName
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Main drawer component that combines all drawer sections with spectacular animations
 */
@Composable
fun HomeDrawer(
    drawerState: DrawerState,
    homeUiState: HomeUiState,
    userDetails: com.desarrollodroide.adventurelog.core.model.UserDetails? = null,
    scope: CoroutineScope,
    currentScreen: CurrentScreen = CurrentScreen.HOME,
    onHomeClick: () -> Unit,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onLogout: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val isDrawerOpen = drawerState.isOpen
    
    // Extract only what's needed from HomeUiState
    val userName = when {
        userDetails != null -> userDetails.fullName
        homeUiState is HomeUiState.Success -> homeUiState.userName
        else -> ""
    }
    
    val adventureCount = when (homeUiState) {
        is HomeUiState.Success -> homeUiState.recentAdventures.size
        else -> 0
    }
    
    // Animation for background darkening when drawer opens
    val animatedDimAmount by animateFloatAsState(
        targetValue = if (isDrawerOpen) 0.6f else 0f,  // Aumentamos el oscurecimiento
        animationSpec = tween(400),
        label = "dim"
    )
    
    // Main drawer with content and animations
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            DrawerContent(
                userName = userName,
                userEmail = userDetails?.email,
                profileImageUrl = userDetails?.profilePic,
                adventureCount = adventureCount,
                currentScreen = currentScreen,
                onHomeClick = {
                    onHomeClick()
                    scope.launch { drawerState.close() }
                },
                onAdventuresClick = {
                    onAdventuresClick()
                    scope.launch { drawerState.close() }
                },
                onCollectionsClick = {
                    onCollectionsClick()
                    scope.launch { drawerState.close() }
                },
                onTravelClick = {
                    onTravelClick()
                    scope.launch { drawerState.close() }
                },
                onMapClick = {
                    onMapClick()
                    scope.launch { drawerState.close() }
                },
                onCalendarClick = {
                    onCalendarClick()
                    scope.launch { drawerState.close() }
                },
                onSettingsClick = {
                    onSettingsClick()
                    scope.launch { drawerState.close() }
                },
                onLogout = {
                    onLogout()
                    scope.launch { drawerState.close() }
                },
                onHelpClick = {
                    onHelpClick()
                    scope.launch { drawerState.close() }
                },
                drawerOpen = isDrawerOpen
            )
        },
        scrimColor = Color.Black.copy(alpha = animatedDimAmount),
    ) {
        // Main content with additional darkening effect
        Box {
            // Main content
            content()
            
            // Additional animated darkening layer (for a smoother effect)
            if (animatedDimAmount > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = animatedDimAmount * 0.4f
                        }
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }
    }
}