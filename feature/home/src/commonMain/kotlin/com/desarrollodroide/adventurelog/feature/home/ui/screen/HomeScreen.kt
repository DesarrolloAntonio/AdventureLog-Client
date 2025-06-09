package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.model.fullName
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.resources.painterResource
import com.desarrollodroide.adventurelog.resources.Res
import com.desarrollodroide.adventurelog.resources.ic_hamburger_alt
import androidx.navigation.compose.*
import androidx.navigation.compose.currentBackStackEntryAsState
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations
import com.desarrollodroide.adventurelog.feature.ui.navigation.AnimatedDirectionalNavHost
import com.desarrollodroide.adventurelog.resources.background_blur_mint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import com.desarrollodroide.adventurelog.core.model.Adventure
import com.desarrollodroide.adventurelog.feature.ui.preview.PreviewImageDependencies

/**
 * Entry point composable that integrates with navigation
 */
@Composable
fun HomeScreenRoute(
    viewModel: HomeViewModel = koinViewModel(),
    settingsViewModel: SettingsViewModel = koinViewModel(),
    onAdventureClick: (Adventure) -> Unit = { },
    onNavigateToLogin: () -> Unit = { }
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userDetails by viewModel.userDetails.collectAsStateWithLifecycle()

    HomeScreenContent(
        homeUiState = homeUiState,
        userDetails = userDetails,
        onAdventureClick = onAdventureClick,
        onLogout = { 
            viewModel.logout()
            onNavigateToLogin()
        }
    )
}

/**
 * Helper function to reset scroll behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
private fun resetScrollBehavior(scrollBehavior: TopAppBarScrollBehavior) {
    scrollBehavior.state.contentOffset = 0f
    scrollBehavior.state.heightOffset = 0f
}

/**
 * Main home screen composable that integrates all components and handles internal navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    userDetails: UserDetails? = null,
    onAdventureClick: (Adventure) -> Unit = { },
    onLogout: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // User name to display
    val userName = userDetails?.fullName ?: "User"

    // Track current screen
    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }

    // Observer for navigation changes to keep currentScreen in sync
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ""

    // Check if we're in a collection detail screen
    val isCollectionDetail by remember(currentRoute) {
        derivedStateOf { currentRoute.startsWith("collection/") }
    }

    // Extract collection ID and name from route parameters
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

    // Extract collection name from route parameters
    val collectionName by remember(currentBackStackEntry) {
        derivedStateOf {
            if (isCollectionDetail) {
                // Get the collection name from the backstack entry
                currentBackStackEntry?.arguments?.getString("collectionName") ?: "Collection"
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

    // Function to navigate to any screen in the app
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
    
    Box(modifier = modifier.fillMaxSize()) {
        // LAYER 1: Background image covering the entire screen
        Image(
            painter = painterResource(Res.drawable.background_blur_mint),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f),
            contentScale = ContentScale.FillBounds
        )
        
        // LAYER 2: Drawer and content
        HomeDrawer(
            drawerState = drawerState,
            homeUiState = homeUiState,
            userDetails = userDetails,
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
            },
            onLogout = onLogout
        ) {
            Scaffold(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .background(Color.Transparent),  // Ensure Scaffold is transparent
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
                                                .clickable {
                                                    // Reset scroll behavior when navigating back from collection detail
                                                    // This fixes the issue where the breadcrumb gets stuck as title
                                                    // when user has scrolled up and then clicks to go back
                                                    resetScrollBehavior(scrollBehavior)
                                                    navController.navigateUp()
                                                }
                                                .padding(end = 4.dp)
                                        )

                                        // Home icon instead of text
                                        Icon(
                                            imageVector = Icons.Default.Home,
                                            contentDescription = "Home",
                                            modifier = Modifier
                                                .clickable {
                                                    // Reset scroll behavior when navigating from collection detail to home
                                                    // This fixes the issue where the breadcrumb gets stuck as title
                                                    resetScrollBehavior(scrollBehavior)
                                                    navigateToHome()
                                                }
                                                .padding(end = 4.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )

                                        // Arrow separator
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                                .width(12.dp),
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
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.mediumTopAppBarColors(
                            // Make TopBar transparent to see the background
                            containerColor = Color.Transparent
                        )
                    )
                },
                // Make Scaffold content transparent
                containerColor = Color.Transparent
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
                                onAdventureClick = onAdventureClick,
                                sessionToken = userDetails?.sessionToken ?: ""
                            )
                        }

                        composable(NavigationRoutes.Adventures.route) {
                            AdventureListScreen(
                                onAdventureClick = onAdventureClick,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        collectionsScreen(
                            onCollectionClick = { collectionId, collectionName ->
                                // No need to reset scroll when navigating TO a collection
                                navController.navigate("collection/$collectionId/$collectionName")
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
                                onLogout = onLogout,
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

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HomeScreenEmptyPreview() {
    PreviewImageDependencies {
        MaterialTheme {
            HomeScreenContent(
                homeUiState = HomeUiState.Empty,
                userDetails = null
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HomeScreenLoadingPreview() {
    PreviewImageDependencies {
        MaterialTheme {
            HomeScreenContent(
                homeUiState = HomeUiState.Loading,
                userDetails = null
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HomeScreenSuccessPreview() {
    val sampleStats = UserStats(
        adventureCount = 12,
        tripsCount = 5,
        visitedCityCount = 0,
        totalCities = 15020,
        visitedRegionCount = 1,
        totalRegions = 5062,
        visitedCountryCount = 1,
        totalCountries = 250
    )
    
    PreviewImageDependencies {
        MaterialTheme {
            HomeScreenContent(
                homeUiState = HomeUiState.Success(
                    userName = "Antonio",
                    recentAdventures = emptyList(),
                    userStats = sampleStats
                ),
                userDetails = UserDetails(
                    id = 123,
                    username = "antonio",
                    firstName = "Antonio",
                    lastName = "García",
                    email = "antonio@example.com",
                    profilePic = null,
                    isStaff = false,
                    dateJoined = "2024-01-01",
                    sessionToken = "token123",
                    uuid = "user-uuid-123",
                    publicProfile = true,
                    hasPassword = "true",
                    serverUrl = "https://example-server.com"
                )
            )
        }
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HomeScreenErrorPreview() {
    PreviewImageDependencies {
        MaterialTheme {
            HomeScreenContent(
                homeUiState = HomeUiState.Error("Failed to load adventures"),
                userDetails = null
            )
        }
    }
}