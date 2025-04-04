package com.desarrollodroide.adventurelog.feature.home.ui.screen
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
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
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.painterResource
import com.desarrollodroide.adventurelog.resources.Res
import com.desarrollodroide.adventurelog.resources.ic_hamburger_alt
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.currentBackStackEntryAsState
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations
import com.desarrollodroide.adventurelog.feature.ui.navigation.AnimatedDirectionalNavHost

/**
 * Entry point composable that integrates with navigation
 */
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel(),
    onAdventureClick: (String) -> Unit = {}
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        homeUiState = homeUiState,
        userName = "John Doe", // Assuming userName is a property in HomeViewModel
        onAdventureClick = onAdventureClick,
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
    userName: String,
    onAdventureClick: (String) -> Unit = {},
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    // Track current screen
    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }

    // Observer for navigation changes to keep currentScreen in sync
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Update currentScreen when navigation changes
    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.route?.let { route ->
            currentScreen = CurrentScreen.fromRoute(route)
        }
    }
    
    // Navigation actions
    val navigateTo: (CurrentScreen) -> Unit = { screen ->
        navController.navigate(screen.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(NavigationRoutes.Home.screen) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
        
        // Update current screen
        currentScreen = screen
    }

    // Drawer menu with navigation
    HomeDrawer(
        drawerState = drawerState,
        homeUiState = homeUiState,
        scope = scope,
        currentScreen = currentScreen,
        onHomeClick = {
            navigateTo(CurrentScreen.HOME)
        },
        onAdventuresClick = {
            navigateTo(CurrentScreen.ADVENTURES)
        },
        onCollectionsClick = {
            navigateTo(CurrentScreen.COLLECTIONS)
        },
        onTravelClick = {
            navigateTo(CurrentScreen.TRAVEL)
        },
        onMapClick = {
            navigateTo(CurrentScreen.MAP)
        },
        onCalendarClick = {
            navigateTo(CurrentScreen.CALENDAR)
        },
        onSettingsClick = {
            navigateTo(CurrentScreen.SETTINGS)
        }
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MediumTopAppBar(
                    title = {
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Hi, $userName!",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                state = rememberLazyListState(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    // NavHost to manage the content on each screen with animations
                    AnimatedDirectionalNavHost(
                        navController = navController,
                        startDestination = NavigationRoutes.Home.screen,
                        modifier = Modifier.fillMaxSize(),
                        // Map routes to indices for directional navigation
                        routeToIndexMapper = { route -> 
                            CurrentScreen.fromRoute(route).index 
                        }
                    ) {
                        composable(
                            route = NavigationRoutes.Home.screen,
                            // Individual animations can be overridden for specific routes
                            enterTransition = NavigationAnimations.enterTransitionFade,
                            exitTransition = NavigationAnimations.exitTransitionFade
                        ) {
                            HomeContent(
                                modifier = Modifier.fillMaxSize(),
                                homeUiState = homeUiState,
                                onAdventureClick = onAdventureClick
                            )
                        }

                        composable(NavigationRoutes.Adventures.route) {
                            AdventureListScreen(
                                onAdventureClick = onAdventureClick,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        composable(
                            route = NavigationRoutes.Settings.route,
                            // Settings appears from bottom for a distinctive style
                            enterTransition = NavigationAnimations.enterTransitionVertical,
                            exitTransition = NavigationAnimations.exitTransitionVertical,
                        ) {
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

                        composable(NavigationRoutes.Collections.route) {
                            PlaceholderScreen("Collections")
                        }

                        composable(NavigationRoutes.Travel.route) {
                            PlaceholderScreen("Travel")
                        }

                        composable(NavigationRoutes.Map.route) {
                            PlaceholderScreen("Map")
                        }

                        composable(NavigationRoutes.Calendar.route) {
                            PlaceholderScreen("Calendar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("$title Screen")
    }
}