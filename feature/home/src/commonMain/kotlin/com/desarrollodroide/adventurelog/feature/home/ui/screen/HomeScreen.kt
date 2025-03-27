package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.ui.components.adventures.EmptyStateView
import com.desarrollodroide.adventurelog.feature.home.ui.components.common.ErrorStateView
import com.desarrollodroide.adventurelog.feature.home.ui.components.drawer.HomeDrawer
import com.desarrollodroide.adventurelog.feature.home.ui.components.home.HomeContent
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen
import com.desarrollodroide.adventurelog.feature.home.viewmodel.HomeViewModel
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import com.desarrollodroide.adventurelog.feature.settings.ui.screen.SettingsContent
import com.desarrollodroide.adventurelog.feature.adventures.ui.screens.AdventureListScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.painterResource
import com.desarrollodroide.adventurelog.resources.Res
import com.desarrollodroide.adventurelog.resources.ic_hamburger_alt

/**
 * Entry point composable that integrates with navigation
 */
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel(),
    onAdventureClick: (String) -> Unit = {}
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Maintain the current screen state inside HomeScreen to control content
    // Initialize with HOME
    var currentScreen by rememberSaveable { mutableStateOf(CurrentScreen.HOME) }

    HomeScreenContent(
        homeUiState = homeUiState,
        currentScreen = currentScreen,
        userName = "John Doe", // Assuming userName is a property in HomeViewModel
        onAdventureClick = onAdventureClick,
        onHomeClick = {
            currentScreen = CurrentScreen.HOME
        },
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
    userName: String,
    onAdventureClick: (String) -> Unit = {},
    onHomeClick: () -> Unit,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val scrollState = rememberLazyListState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    HomeDrawer(
        drawerState = drawerState,
        homeUiState = homeUiState,
        scope = scope,
        currentScreen = currentScreen,
        onHomeClick = onHomeClick,
        onAdventuresClick = onAdventuresClick,
        onCollectionsClick = onCollectionsClick,
        onTravelClick = onTravelClick,
        onMapClick = onMapClick,
        onCalendarClick = onCalendarClick,
        onSettingsClick = onSettingsClick
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            text = "Hi, $userName!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_hamburger_alt),
                                contentDescription = "Menu"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            }
        ) { innerPadding ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    when (currentScreen) {
                        CurrentScreen.HOME -> {
                            HomeContent(
                                modifier = Modifier.fillMaxSize(),
                                homeUiState = homeUiState
                            )
                        }

                        CurrentScreen.ADVENTURES -> {
                            when (homeUiState) {
                                is HomeUiState.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                is HomeUiState.Empty -> EmptyStateView()
                                is HomeUiState.Success -> AdventureListScreen(
                                    adventureItems = homeUiState.recentAdventures,
                                    onOpenDetails = onAdventureClick,
                                    modifier = Modifier.fillMaxSize()
                                )

                                is HomeUiState.Error -> ErrorStateView(homeUiState.message)
                            }
                        }

                        CurrentScreen.SETTINGS -> {
                            val settingsViewModel = koinViewModel<SettingsViewModel>()
                            val compactView by settingsViewModel.compactView.collectAsStateWithLifecycle()

                            SettingsContent(
                                compactView = compactView,
                                onCompactViewChanged = settingsViewModel::setCompactView,
                                onLogout = settingsViewModel::logout,
                                onNavigateToSourceCode = { /* TODO */ },
                                onNavigateToTermsOfUse = { /* TODO */ },
                                onNavigateToPrivacyPolicy = { /* TODO */ },
                                onNavigateToLogs = { /* TODO */ },
                                onViewLastCrash = { /* TODO */ },
                                themeMode = settingsViewModel.themeMode,
                                onThemeModeChanged = settingsViewModel::setThemeMode,
                                useDynamicColors = settingsViewModel.useDynamicColors,
                                onDynamicColorsChanged = settingsViewModel::setUseDynamicColors,
                                goToLogin = { /* TODO */ },
                                serverUrl = settingsViewModel.getServerUrl()
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${currentScreen.name} Screen")
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(64.dp))
                }
            }
        }
    }
}