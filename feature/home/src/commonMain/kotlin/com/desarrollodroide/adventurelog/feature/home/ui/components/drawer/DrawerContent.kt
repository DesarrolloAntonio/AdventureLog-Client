package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.components.drawer.NavigationItemsList
import com.desarrollodroide.adventurelog.feature.home.components.drawer.createNavigationItems
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.CurrentScreen
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember

/**
 * Reusable drawer content between modal and permanent versions
 */
@Composable
fun DrawerContent(
    homeUiState: HomeUiState,
    currentScreen: CurrentScreen,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    drawerOpen: Boolean // New parameter to control drawer visibility
) {
    // Convert current screen to corresponding navigation index
    val selectedItemIndex = when (currentScreen) {
        CurrentScreen.HOME -> 0
        CurrentScreen.ADVENTURES -> 0
        CurrentScreen.COLLECTIONS -> 1
        CurrentScreen.TRAVEL -> 2
        CurrentScreen.MAP -> 3
        CurrentScreen.CALENDAR -> 4
        CurrentScreen.SETTINGS -> 5  // Settings is now assigned index 5
    }

    // Maintain selection state internally, but initialize it from currentScreen
    var selectedItem by rememberSaveable { mutableStateOf(selectedItemIndex) }
    if (selectedItem != selectedItemIndex) {
        selectedItem = selectedItemIndex
    }
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(drawerOpen) {
        visible = drawerOpen
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                top = 30.dp,
                bottom = 20.dp
            )
    ) {
        key(drawerOpen) {
            AnimatedVisibility(
                visible = visible,
                    enter = slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f)
                    ),
                    exit = slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    )
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier.width(280.dp)
                ) {
                    ModalDrawerSheet {
                        DrawerHeader(homeUiState = homeUiState)
                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            val navigationItems = createNavigationItems(
                                onAdventuresClick = onAdventuresClick,
                                onCollectionsClick = onCollectionsClick,
                                onTravelClick = onTravelClick,
                                onMapClick = onMapClick,
                                onCalendarClick = onCalendarClick
                            )

                            NavigationItemsList(
                                navigationItems = navigationItems,
                                selectedItem = if (selectedItem <= 4) selectedItem else -1,
                                onItemSelected = { selectedItem = it },
                                scope = scope,
                                onCloseDrawer = { /* callbacks */ }
                            )

                            ConfigSection(
                                onSettingsClick = onSettingsClick,
                                onHelpClick = { /* TODO: Implement help */ },
                                isSettingsSelected = selectedItem == 5
                            )
                        }
                    }
                }
            }
        }
    }
}