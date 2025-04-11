package com.desarrollodroide.adventurelog.feature.home.ui.screen
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.collectionsScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.painterResource
import com.desarrollodroide.adventurelog.resources.Res
import com.desarrollodroide.adventurelog.resources.ic_hamburger_alt
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.compose.currentBackStackEntryAsState
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations
import com.desarrollodroide.adventurelog.feature.ui.navigation.AnimatedDirectionalNavHost
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData

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
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""
    
    // Check if we're in a collection detail screen
    val isCollectionDetail by remember(currentRoute) {
        derivedStateOf { currentRoute.startsWith("collection/") }
    }
    
    // Extract collection ID from route parameters
    val collectionId by remember(currentBackStackEntry) {
        derivedStateOf {
            if (isCollectionDetail) {
                // Get the actual parameter value from the backstack entry
                currentBackStackEntry?.arguments?.getString("collectionId") ?: ""
            } else {
                ""
            }
        }
    }
    
    // Find collection name for the title
    val collectionName by remember(collectionId) {
        derivedStateOf {
            if (collectionId.isNotEmpty()) {
                // Look up the collection by ID to get its name
                PreviewData.collections.find { it.id == collectionId }?.name ?: "Collection $collectionId"
            } else {
                "Collection"
            }
        }
    }
    
    // Update currentScreen when navigation changes
    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.route?.let { route ->
            if (!route.startsWith("collection/")) {
                currentScreen = CurrentScreen.fromRoute(route)
            } else {
                // We're in a collection detail, still mark as COLLECTIONS for drawer highlight
                currentScreen = CurrentScreen.COLLECTIONS
            }
        }
    }
    
    // Navigation actions
    val navigateToHome = {
        navController.navigate(NavigationRoutes.Home.screen) {
            popUpTo(NavigationRoutes.Home.screen) {
                inclusive = true
            }
        }
    }
    
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
                            // If we're in a collection detail, show a simple breadcrumb
                            if (isCollectionDetail) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Chevron Left icon for going back
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Back",
                                        modifier = Modifier
                                            .clickable { navController.navigateUp() }
                                            .padding(end = 4.dp)
                                    )
                                    
                                    // Home icon instead of text
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Home",
                                        modifier = Modifier
                                            .clickable { navigateToHome() }
                                            .padding(end = 4.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    // Arrow separator
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.padding(horizontal = 4.dp).width(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    // Collection name
                                    Text(
                                        text = collectionName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                // For other screens, show the normal title
                                val topBarTitle = when (currentScreen) {
                                    CurrentScreen.COLLECTIONS -> "Collections"
                                    CurrentScreen.ADVENTURES -> "Adventures"
                                    CurrentScreen.SETTINGS -> "Settings"
                                    CurrentScreen.TRAVEL -> "Travel"
                                    CurrentScreen.MAP -> "Map"
                                    CurrentScreen.CALENDAR -> "Calendar"
                                    CurrentScreen.HOME -> "Hi, $userName!"
                                }
                                
                                Text(
                                    text = topBarTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        // Always show the drawer icon
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // NavHost to manage the content on each screen with animations
                AnimatedDirectionalNavHost(
                    navController = navController,
                    startDestination = NavigationRoutes.Home.screen,
                    modifier = Modifier.fillMaxSize(),
                    // Map routes to indices for directional navigation
                    routeToIndexMapper = { route -> 
                        if (route.startsWith("collection/")) {
                            CurrentScreen.COLLECTIONS.index
                        } else {
                            CurrentScreen.fromRoute(route).index 
                        }
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

                    collectionsScreen(
                        onCollectionClick = { collectionId ->
                            navController.navigate("collection/$collectionId")
                        },
                        onHomeClick = navigateToHome,
                        onAdventureClick = onAdventureClick,
                        navController = navController
                    )

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