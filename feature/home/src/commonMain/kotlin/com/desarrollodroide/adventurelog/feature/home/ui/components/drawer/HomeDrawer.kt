package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.CurrentScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Main drawer component that combines all drawer sections
 */
@Composable
fun HomeDrawer(
    drawerState: DrawerState,
    homeUiState: HomeUiState,
    scope: CoroutineScope,
    currentScreen: CurrentScreen = CurrentScreen.HOME,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // Main drawer with content
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                homeUiState = homeUiState,
                currentScreen = currentScreen,
                onAdventuresClick = {
                    scope.launch { drawerState.close() }
                    onAdventuresClick()
                },
                onCollectionsClick = {
                    scope.launch { drawerState.close() }
                    onCollectionsClick()
                },
                onTravelClick = {
                    scope.launch { drawerState.close() }
                    onTravelClick()
                },
                onMapClick = {
                    scope.launch { drawerState.close() }
                    onMapClick()
                },
                onCalendarClick = {
                    scope.launch { drawerState.close() }
                    onCalendarClick()
                },
                onSettingsClick = {
                    scope.launch { drawerState.close() }
                    onSettingsClick()
                }
            )
        },
        content = content
    )
}