package com.desarrollodroide.adventurelog.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.components.adventures.EmptyStateView
import com.desarrollodroide.adventurelog.feature.home.ui.components.common.ErrorStateView
import com.desarrollodroide.adventurelog.feature.home.ui.components.drawer.HomeDrawer
import com.desarrollodroide.adventurelog.feature.home.ui.components.topbar.HomeTopBar
import com.desarrollodroide.adventurelog.feature.home.ui.screen.HomeContent
import com.desarrollodroide.adventurelog.feature.home.viewmodel.HomeViewModel
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsContent
import com.desarrollodroide.adventurelog.feature.adventures.adventures.AdventureListScreen
import com.desarrollodroide.adventurelog.feature.adventures.adventures.AdventuresViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen types that can be displayed by the HomeScreen
 */
enum class CurrentScreen {
    HOME,
    ADVENTURES,
    COLLECTIONS,
    TRAVEL,
    MAP,
    CALENDAR,
    SETTINGS
}

/**
 * Entry point composable that integrates with navigation
 */
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel()
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Maintain the current screen state inside HomeScreen to control content
    // Initialize with ADVENTURES instead of HOME
    var currentScreen by rememberSaveable { mutableStateOf(CurrentScreen.ADVENTURES) }

    HomeScreenContent(
        homeUiState = homeUiState,
        currentScreen = currentScreen,
        onAdventuresClick = { 
            currentScreen = CurrentScreen.ADVENTURES
        },
        onCollectionsClick = { 
            currentScreen = CurrentScreen.COLLECTIONS
        },
        onTravelClick = { 
            currentScreen = CurrentScreen.TRAVEL
        },
        onMapClick = { 
            currentScreen = CurrentScreen.MAP
        },
        onCalendarClick = { 
            currentScreen = CurrentScreen.CALENDAR
        },
        onSettingsClick = { 
            currentScreen = CurrentScreen.SETTINGS
        },
        onBackToHome = {
            currentScreen = CurrentScreen.HOME
        }
    )
}

/**
 * Main home screen composable that integrates all components and handles internal navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    currentScreen: CurrentScreen,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onBackToHome: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // ViewModels
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val adventuresViewModel = koinViewModel<AdventuresViewModel>()

    // Title based on current screen
    val title = when (currentScreen) {
        CurrentScreen.HOME -> "Adventure Log"
        CurrentScreen.ADVENTURES -> "Adventures"
        CurrentScreen.COLLECTIONS -> "Collections"
        CurrentScreen.TRAVEL -> "Travel"
        CurrentScreen.MAP -> "Map"
        CurrentScreen.CALENDAR -> "Calendar"
        CurrentScreen.SETTINGS -> "Settings"
    }

    // Main drawer with content
    HomeDrawer(
        drawerState = drawerState,
        homeUiState = homeUiState,
        scope = scope,
        currentScreen = currentScreen,
        onAdventuresClick = onAdventuresClick,
        onCollectionsClick = onCollectionsClick,
        onTravelClick = onTravelClick,
        onMapClick = onMapClick,
        onCalendarClick = onCalendarClick,
        onSettingsClick = onSettingsClick
    ) {
        // Main scaffold with top bar and content
        Scaffold(
            topBar = {
                HomeTopBar(
                    title = title,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            // Main content based on current screen
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (currentScreen) {
                    CurrentScreen.HOME -> {
                        when (homeUiState) {
                            is HomeUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is HomeUiState.Empty -> {
                                EmptyStateView()
                            }
                            is HomeUiState.Success -> {
                                HomeContent(
                                    userName = homeUiState.userName,
                                    adventures = homeUiState.recentAdventures
                                )
                            }
                            is HomeUiState.Error -> {
                                ErrorStateView(errorMessage = homeUiState.message)
                            }
                        }
                    }
                    CurrentScreen.ADVENTURES -> {
                        // Show AdventureListScreen from feature/adventures
                        if (homeUiState is HomeUiState.Success) {
                            AdventureListScreen(
                                adventureItems = homeUiState.recentAdventures,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Show loading or empty state as appropriate
                            when (homeUiState) {
                                is HomeUiState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                                is HomeUiState.Empty -> {
                                    EmptyStateView()
                                }
                                is HomeUiState.Error -> {
                                    ErrorStateView(errorMessage = homeUiState.message)
                                }
                                else -> {}
                            }
                        }
                    }
                    CurrentScreen.COLLECTIONS -> {
                        // TODO: Implement collections screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Collections Screen")
                        }
                    }
                    CurrentScreen.TRAVEL -> {
                        // TODO: Implement travel screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Travel Screen")
                        }
                    }
                    CurrentScreen.MAP -> {
                        // TODO: Implement map screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Map Screen")
                        }
                    }
                    CurrentScreen.CALENDAR -> {
                        // TODO: Implement calendar screen
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Calendar Screen")
                        }
                    }
                    CurrentScreen.SETTINGS -> {
                        // Use SettingsContent directly from the settings module
                        val compactView by settingsViewModel.compactView.collectAsStateWithLifecycle()
                        
                        SettingsContent(
                            compactView = compactView,
                            onCompactViewChanged = { isCompact ->
                                settingsViewModel.setCompactView(isCompact)
                            },
                            onLogout = { 
                                settingsViewModel.logout() 
                            },
                            onNavigateToSourceCode = { /* TODO */ },
                            onNavigateToTermsOfUse = { /* TODO */ },
                            onNavigateToPrivacyPolicy = { /* TODO */ },
                            onNavigateToLogs = { /* TODO */ },
                            onViewLastCrash = { /* TODO */ },
                            themeMode = settingsViewModel.themeMode,
                            onThemeModeChanged = { newMode ->
                                settingsViewModel.setThemeMode(newMode)
                            },
                            useDynamicColors = settingsViewModel.useDynamicColors,
                            onDynamicColorsChanged = { useDynamic ->
                                settingsViewModel.setUseDynamicColors(useDynamic)
                            },
                            goToLogin = { /* TODO */ },
                            serverUrl = settingsViewModel.getServerUrl()
                        )
                    }
                }
            }
        }
    }
}