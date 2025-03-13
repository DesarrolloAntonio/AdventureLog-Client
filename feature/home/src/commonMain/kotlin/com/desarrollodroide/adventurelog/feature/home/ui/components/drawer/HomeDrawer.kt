package com.desarrollodroide.adventurelog.feature.home.ui.components.drawer

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.desarrollodroide.adventurelog.feature.home.components.drawer.NavigationItemsList
import com.desarrollodroide.adventurelog.feature.home.components.drawer.createNavigationItems
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
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
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // State to track the selected navigation item
    var selectedItem by remember { mutableStateOf(0) }

    // Create navigation items list
    val navigationItems = createNavigationItems(
        onAdventuresClick = onAdventuresClick,
        onCollectionsClick = onCollectionsClick,
        onTravelClick = onTravelClick,
        onMapClick = onMapClick,
        onCalendarClick = onCalendarClick
    )

    // Main drawer with content
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AnimatedDrawerContent(drawerState = drawerState, drawerWidth = 300) {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp)
                ) {
                    DrawerHeader(homeUiState = homeUiState)
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationItemsList(
                        navigationItems = navigationItems,
                        selectedItem = selectedItem,
                        onItemSelected = { selectedItem = it },
                        scope = scope,
                        onCloseDrawer = { scope.launch { drawerState.close() } }
                    )
                    ConfigSection(
                        onSettingsClick = {
                            scope.launch { drawerState.close() }
                            onSettingsClick()
                        },
                        onHelpClick = {
                            scope.launch { drawerState.close() }
                            onHelpClick()
                        }
                    )
                }
            }
        },
        content = content
    )
}