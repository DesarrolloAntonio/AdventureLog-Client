package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.components.drawer.NavigationItemsList
import com.desarrollodroide.adventurelog.feature.home.components.drawer.createNavigationItems
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.CurrentScreen

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
    onSettingsClick: () -> Unit
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

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp)
    ) {
        DrawerHeader(homeUiState = homeUiState)
        Spacer(modifier = Modifier.height(8.dp))
        
        Column {
            // Navigation items
            val navigationItems = createNavigationItems(
                onAdventuresClick = onAdventuresClick,
                onCollectionsClick = onCollectionsClick,
                onTravelClick = onTravelClick,
                onMapClick = onMapClick,
                onCalendarClick = onCalendarClick
            )
            
            NavigationItemsList(
                navigationItems = navigationItems,
                selectedItem = if (selectedItem <= 4) selectedItem else -1, // Only highlight main nav items
                onItemSelected = { selectedItem = it },
                scope = scope,
                onCloseDrawer = { /* Handled by callbacks */ }
            )
            
            // Settings and Help
            ConfigSection(
                onSettingsClick = onSettingsClick,
                onHelpClick = { /* TODO: Implement help */ },
                isSettingsSelected = selectedItem == 5 // Pass selection state to ConfigSection
            )
        }
    }
}