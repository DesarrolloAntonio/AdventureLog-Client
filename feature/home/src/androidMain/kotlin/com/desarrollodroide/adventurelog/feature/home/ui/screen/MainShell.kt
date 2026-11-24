package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.desarrollodroide.adventurelog.feature.ui.components.LocalShellTopOverlay
import com.desarrollodroide.adventurelog.feature.calendar.viewmodel.EventTarget
import androidx.compose.foundation.layout.consumeWindowInsets
import kotlinx.coroutines.withContext
import kotlinx.coroutines.IO
import kotlinx.coroutines.Dispatchers
import com.desarrollodroide.adventurelog.feature.ui.di.LocalImageLoader
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.window.core.layout.WindowWidthSizeClass
import com.desarrollodroide.adventurelog.feature.home.ui.components.MainToolbarRow
import com.desarrollodroide.adventurelog.feature.home.ui.components.rememberToolbarScrollState
import com.desarrollodroide.adventurelog.feature.ui.components.LocalShellActionHost
import com.desarrollodroide.adventurelog.feature.ui.components.LocalShellAppBarExpanded
import com.desarrollodroide.adventurelog.feature.ui.components.ShellActionHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.model.Dashboard
import com.desarrollodroide.adventurelog.core.model.UserStats
import com.desarrollodroide.adventurelog.feature.home.model.HomeUiState
import com.desarrollodroide.adventurelog.feature.home.model.fullName
import com.desarrollodroide.adventurelog.feature.home.ui.components.homeNavigationItems
import com.desarrollodroide.adventurelog.feature.home.ui.components.ProfileMenu
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.CurrentScreen
import com.desarrollodroide.adventurelog.feature.home.viewmodel.HomeViewModel
import com.desarrollodroide.adventurelog.feature.locations.ui.navigation.locationsScreen
import com.desarrollodroide.adventurelog.feature.locations.ui.navigation.LocationsNavigator
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.collectionsScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.CollectionsNavigator
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.transportationsScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.TransportationsNavigator
import com.desarrollodroide.adventurelog.feature.map.navigation.mapScreen
import com.desarrollodroide.adventurelog.feature.settings.navigation.settingsScreen
import com.desarrollodroide.adventurelog.feature.world.navigation.worldGraph
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.desarrollodroide.adventurelog.resources.Res
import androidx.navigation.compose.*
import androidx.navigation.compose.currentBackStackEntryAsState
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations
import com.desarrollodroide.adventurelog.feature.ui.theme.AdventureType
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.navigation.NavBackStackEntry
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.isSectionSwitch
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Transportation
import com.desarrollodroide.adventurelog.feature.calendar.navigation.calendarScreen
import androidx.compose.material.icons.filled.Search
import com.desarrollodroide.adventurelog.feature.home.ui.components.GlobalSearchSheet
import androidx.compose.foundation.layout.BoxWithConstraints
import com.desarrollodroide.adventurelog.feature.ui.components.MaxContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.notesScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.NotesNavigator
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.checklistsScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.ChecklistsNavigator
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.lodgingScreen
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.LodgingNavigator
import com.desarrollodroide.adventurelog.core.model.Lodging

/**
 * Entry point composable that integrates with navigation
 */
@Composable
fun MainShellRoute(
    viewModel: HomeViewModel = koinViewModel(),
    onAdventureClick: (Location) -> Unit = { },
    onOpenLocationById: (String) -> Unit = { },
    onNavigateToLogin: () -> Unit = { }
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userDetails by viewModel.userDetails.collectAsStateWithLifecycle()
    val signedOut by viewModel.signedOut.collectAsStateWithLifecycle()

    val imageLoader = LocalImageLoader.current
    LaunchedEffect(signedOut) {
        if (signedOut) {
            // The account's photos stay in the image cache otherwise, for the next person to sign
            // in on this phone. Before navigating: leaving cancels this effect.
            imageLoader.memoryCache?.clear()
            withContext(Dispatchers.IO) { imageLoader.diskCache?.clear() }
            onNavigateToLogin()
        }
    }

    LifecycleStartEffect(Unit) {
        viewModel.recheckSession()
        onStopOrDispose { }
    }

    HomeScreenContent(
        homeUiState = homeUiState,
        userDetails = userDetails,
        onAdventureClick = { adventure ->
            viewModel.selectLocation(adventure)
            onAdventureClick(adventure)
        },
        onOpenLocationById = onOpenLocationById,
        onRetryDashboard = viewModel::loadDashboard,
        onRefreshDashboard = viewModel::refreshDashboard,
        onLogout = viewModel::logout
    )
}

/** Screens that carry a search field of their own, where the bar's global search is left out. */
private val ScreensWithOwnSearch = setOf(
    CurrentScreen.PLACES, CurrentScreen.COLLECTIONS, CurrentScreen.TRAVEL, CurrentScreen.USERS,
)

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
    onAdventureClick: (Location) -> Unit = { },
    onOpenLocationById: (String) -> Unit = { },
    onRetryDashboard: () -> Unit = {},
    onRefreshDashboard: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val json = remember {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = true
        }
    }

    // User name to display
    val userName = userDetails?.fullName ?: "User"

    // Track current screen
    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }
    var searchOpen by remember { mutableStateOf(false) }

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
                currentBackStackEntry?.savedStateHandle?.get<String>("collectionId") ?: ""
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
                currentBackStackEntry?.savedStateHandle?.get<String>("collectionName")
                    ?: "Collection"
            } else {
                "Collection"
            }
        }
    }

    // Update currentScreen when navigation changes
    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.route?.let { route ->
            currentScreen = CurrentScreen.fromRoute(route)
        }
        // One collapsing app bar serves every destination. Left as it was, a long scroll in
        // Settings opened Home with the bar still folded away - no greeting, no search, no
        // account button - until the user thought to drag the page down (measured).
        resetScrollBehavior(scrollBehavior)
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
    // Set by Home's invitation banner and cleared once Collections has opened on Invites.
    var openCollectionInvites by rememberSaveable { mutableStateOf(false) }

    // The screen on show hands its main action to the phone's toolbar. Moving to another
    // destination takes it back at once: left to the old screen, the "+" lingered for the whole
    // transition and only then shrank away. The new screen hands over its own as it appears.
    val shellActions = remember { ShellActionHost() }

    val navigateTo: (CurrentScreen) -> Unit = { screen ->
        if (screen != currentScreen) shellActions.action = null
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

    if (searchOpen) {
        GlobalSearchSheet(
            // A place's page lives in the root graph, not in this shell's NavHost, so it is
            // reached through the navigator rather than the controller in hand.
            onOpenLocation = onOpenLocationById,
            onOpenCollection = { id, name ->
                navController.navigate(
                    NavigationRoutes.Collections.createDetailRoute(
                        collectionId = id,
                        collectionName = name
                    )
                )
            },
            onDismiss = { searchOpen = false }
        )
    }

    val compact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass ==
        WindowWidthSizeClass.COMPACT
    // Map runs edge to edge, as the canvas draws it: no bar, and a floating search pill instead.
    val mapFullBleed = currentScreen == CurrentScreen.MAP

    // The phone's floating row: it slides away while content scrolls down, and carries the main
    // action of the screen on show ("Add a place") beside the destinations.
    val toolbarScroll = rememberToolbarScrollState()
    LaunchedEffect(currentScreen) { toolbarScroll.show() }
    // A screen with bars of its own scrolls too, and the nested scroll reaches the shell's bars
    // while they are put away: back on the list, the app bar was folded and the toolbar gone.
    LaunchedEffect(shellActions.chromeHiddenBy) {
        if (shellActions.chromeHiddenBy == 0) {
            toolbarScroll.show()
            resetScrollBehavior(scrollBehavior)
        }
    }

    // Read by the screens' pull-to-refresh: a downward drag opens the title first, then refreshes.
    val appBarExpanded = remember(scrollBehavior) { { scrollBehavior.state.heightOffset >= -0.5f } }
    CompositionLocalProvider(
        LocalShellActionHost provides if (compact) shellActions else null,
        LocalShellAppBarExpanded provides appBarExpanded,
    ) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .then(if (compact) Modifier.nestedScroll(toolbarScroll.connection) else Modifier)
    ) {
        // Flat, the redesign's #F5FAFB - the theme's own surface.
        //
        // This was a gradient mixed from the palette's containers, which was the right answer when
        // the palette came from the wallpaper and could be anything. It is the wrong one now: the
        // cards are surfaceContainerLowest, a hair off white, and a background that drifts through
        // three container tones leaves them reading as cards in some corners and as nothing in
        // others. One ground, and the cards sit on it.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )

            // The five destinations are a floating toolbar on a phone (drawn below, over the
            // content) and a rail on anything wider. The suite only ever draws the rail.
            NavigationSuiteScaffold(
                navigationSuiteItems = { homeNavigationItems(currentScreen) { navigateTo(it) } },
                layoutType = if (compact) NavigationSuiteType.None else NavigationSuiteType.NavigationRail,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
            BoxWithConstraints {
            val appBarWidth = maxWidth
            Scaffold(
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .background(Color.Transparent),  // Ensure Scaffold is transparent
                topBar = {
                    // A screen with bars of its own (a place's page) has the shell's put away.
                    if (compact && shellActions.chromeHiddenBy > 0) return@Scaffold
                    if (mapFullBleed) return@Scaffold
                    // The bar is transparent, so insetting it simply moves the title and the
                    // actions onto the same left and right edge as the content below. Without
                    // this the greeting starts at the window edge while everything under it
                    // starts 120dp further in, which is one width too many for one screen.
                    val gutter = ((appBarWidth - MaxContentWidth) / 2).coerceAtLeast(0.dp)
                    TopAppBar(
                        modifier = Modifier.padding(horizontal = gutter),
                        // Back sits in the bar's own navigation slot. Inside the title it was a
                        // clickable icon 28dp wide, and as an IconButton the title's inset still
                        // clipped it to 38dp (measured) - under the 48dp a finger needs.
                        navigationIcon = {
                            if (isCollectionDetail) {
                                IconButton(
                                    onClick = {
                                        // Reset scroll behavior when navigating back from collection detail
                                        // This fixes the issue where the breadcrumb gets stuck as title
                                        // when user has scrolled up and then clicks to go back
                                        resetScrollBehavior(scrollBehavior)
                                        navController.navigateUp()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChevronLeft,
                                        contentDescription = "Back"
                                    )
                                }
                            }
                        },
                        title = {
                            // No fillMaxHeight here: the app bar already centres its title, and
                            // filling the height makes the bar grow to half the screen under
                            // Compose 1.12's looser slot constraints - taking the content with it.
                            Box(
                                contentAlignment = Alignment.CenterStart
                            ) {
                                // If we're in a collection detail, show a simple breadcrumb
                                if (isCollectionDetail) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Clear of Back's 48dp touch target, which reached 6dp into
                                        // this icon's.
                                        Spacer(Modifier.width(8.dp))
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
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
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
                                    // Title and, under it, one line saying where you stand. The
                                    // redesign puts it here rather than in the page, so the
                                    // screen opens on content instead of on a caption.
                                    val screen = CurrentScreen.fromRoute(currentRoute)
                                    if (screen == CurrentScreen.HOME) {
                                        // Home greets in its own headline, so the bar carries the
                                        // app's mark and name, as the canvas draws it.
                                        BrandTitle()
                                    } else {
                                        // The canvas's page title: condensed and heavy.
                                        Text(
                                            text = screen.title,
                                            style = AdventureType.display(34.sp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            // The redesign puts adding a place in the bar on a wide window, where
                            // there is room for a labelled button; on a phone the same action is
                            // the floating button the list screens already carry.
                            if (currentScreen == CurrentScreen.HOME && appBarWidth >= 1000.dp) {
                                Button(
                                    onClick = { navController.navigate(NavigationRoutes.Locations.add) },
                                    shape = RoundedCornerShape(22.dp),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add place", style = MaterialTheme.typography.labelLarge)
                                }
                                Spacer(Modifier.width(12.dp))
                            }

                            // Not on a screen with a search field of its own: two searches one
                            // above the other read as the same thing twice.
                            if (currentScreen !in ScreensWithOwnSearch) Surface(
                                onClick = { searchOpen = true },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search everything",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (currentScreen !in ScreensWithOwnSearch) Spacer(Modifier.width(10.dp))
                            ProfileMenu(
                                user = userDetails,
                                userName = userName,
                                serverUrl = userDetails?.serverUrl.orEmpty(),
                                onSettings = { navigateTo(CurrentScreen.SETTINGS) },
                                onCalendar = { navigateTo(CurrentScreen.CALENDAR) },
                                // A section of its own, like Calendar and Settings. Pushed with a plain
                                // navigate it landed inside whichever section was open, and that
                                // section's saved stack then reopened People: Calendar showed People
                                // until the app restarted (QA RL-10).
                                onUsers = { navigateTo(CurrentScreen.USERS) },
                                onLogout = onLogout
                            )
                        },
                        scrollBehavior = scrollBehavior,
                        colors = TopAppBarDefaults.topAppBarColors(
                            // Make TopBar transparent to see the background
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface,
                            actionIconContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                // Transparent has no content colour of its own, so Material resolves it to
                // unspecified and text falls back to black - invisible on the dark theme's
                // backdrop. The gradient is the background here, so say what sits on it.
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                // Edge to edge on Map: the map itself pads for the bars.
                contentWindowInsets = if (mapFullBleed) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
            ) { innerPadding ->
                val barsPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    // On a phone the content runs on behind the floating toolbar, which slides away
                    // while it scrolls down - so a list's last row is uncovered at the end.
                    bottom = innerPadding.calculateBottomPadding()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(barsPadding)
                        // Said as well as done, so a screen that pads for the status bar itself
                        // (a place's compact bar, which also opens outside this shell) doesn't
                        // pad for it twice in here.
                        .consumeWindowInsets(barsPadding)
                ) {
                    CompositionLocalProvider(LocalShellTopOverlay provides if (mapFullBleed) MapSearchPillClearance else 0.dp) {
                    // Changing section fades through: the five tabs and the account menu's
                    // screens have no order between them, so nothing slides left or right (it
                    // used to, by tab position, and Home alone faded). Going deeper inside a
                    // section - a collection, a country, a form - slides forward and back.
                    fun switching(scope: AnimatedContentTransitionScope<NavBackStackEntry>) =
                        isSectionSwitch(scope.initialState.destination.route, scope.targetState.destination.route)
                    NavHost(
                        navController = navController,
                        startDestination = NavigationRoutes.Home.screen,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = {
                            if (switching(this)) NavigationAnimations.fadeThroughEnter()
                            else NavigationAnimations.enterTransition(this)
                        },
                        exitTransition = {
                            if (switching(this)) NavigationAnimations.fadeThroughExit()
                            else NavigationAnimations.exitTransition(this)
                        },
                        popEnterTransition = {
                            if (switching(this)) NavigationAnimations.fadeThroughEnter()
                            else NavigationAnimations.popEnterTransition(this)
                        },
                        popExitTransition = {
                            if (switching(this)) NavigationAnimations.fadeThroughExit()
                            else NavigationAnimations.popExitTransition(this)
                        },
                    ) {
                        composable(route = NavigationRoutes.Home.screen) {
                            // This entry's lifecycle: started again on returning from another tab, a
                            // place's page, an add screen or the background.
                            LifecycleStartEffect(Unit) {
                                onRefreshDashboard()
                                onStopOrDispose { }
                            }
                            DashboardScreen(
                                modifier = Modifier.fillMaxSize(),
                                homeUiState = homeUiState,
                                onAdventureClick = onAdventureClick,
                                // launchSingleTop on each: a double tap opened the trip twice, and Back
                                // returned to the same trip (measured).
                                onTripClick = { trip ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.createDetailRoute(
                                            collectionId = trip.id,
                                            collectionName = trip.name
                                        )
                                    ) { launchSingleTop = true }
                                },
                                onSeeCalendar = { navigateTo(CurrentScreen.CALENDAR) },
                                onSeeAllPlaces = { navigateTo(CurrentScreen.PLACES) },
                                onSeeInvitations = {
                                    openCollectionInvites = true
                                    navigateTo(CurrentScreen.COLLECTIONS)
                                },
                                onRetry = onRetryDashboard,
                                firstName = userDetails?.firstName,
                                onSeeWorld = { navigateTo(CurrentScreen.TRAVEL) },
                                onOpenEvent = { target ->
                                    when (target) {
                                        is EventTarget.Place -> onOpenLocationById(target.id)
                                        is EventTarget.Collection -> navController.navigate(
                                            NavigationRoutes.Collections.createDetailRoute(
                                                collectionId = target.id,
                                                collectionName = target.name
                                            )
                                        ) { launchSingleTop = true }
                                    }
                                },
                                onAddPlace = {
                                    navController.navigate(NavigationRoutes.Locations.add) { launchSingleTop = true }
                                },
                                onAddPlaceToTrip = { trip ->
                                    navController.navigate(NavigationRoutes.Locations.createAddRoute(trip.id)) { launchSingleTop = true }
                                },
                                onAddCollection = {
                                    navController.navigate(NavigationRoutes.Collections.add) { launchSingleTop = true }
                                }
                            )
                        }

                        // Locations screen with navigator
                        composable(route = NavigationRoutes.Locations.route) {
                            PlacesPane(
                                onAddPlace = {
                                    navController.navigate(NavigationRoutes.Locations.add)
                                },
                                onEditPlace = { location ->
                                    navController.navigate(
                                        NavigationRoutes.Locations.createEditRoute(
                                            location.id,
                                            json.encodeToString(location)
                                        )
                                    )
                                },
                                onAddPhoto = { location ->
                                    navController.navigate(
                                        NavigationRoutes.Locations.createEditRoute(
                                            location.id,
                                            json.encodeToString(location),
                                            openImages = true
                                        )
                                    )
                                },
                                onAddVisit = { location ->
                                    navController.navigate(
                                        NavigationRoutes.Locations.createEditRoute(
                                            location.id,
                                            json.encodeToString(location),
                                            openVisits = true
                                        )
                                    )
                                },
                                onCollectionClick = { collection ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.createDetailRoute(
                                            collectionId = collection.id,
                                            collectionName = collection.name
                                        )
                                    )
                                }
                            )
                        }

                        locationsScreen(
                            registerListRoute = false,
                            navigator = object : LocationsNavigator {
                                override fun navigateToLocationDetail(location: Location) {
                                    onAdventureClick(location)
                                }

                                override fun navigateToAddLocation() {
                                    navController.navigate(NavigationRoutes.Locations.add)
                                }

                                override fun navigateToEditLocation(
                                    locationId: String,
                                    locationJson: String,
                                    openImages: Boolean
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Locations.createEditRoute(
                                            locationId,
                                            locationJson,
                                            openImages
                                        )
                                    )
                                }

                                override fun navigateBack() {
                                    navController.navigateUp()
                                }
                            }
                        )

                        // Collections screen with navigator
                        composable(route = NavigationRoutes.Collections.route) {
                            CollectionsPane(
                                openInvites = openCollectionInvites,
                                onInvitesOpened = { openCollectionInvites = false },
                                onAddCollection = {
                                    navController.navigate(NavigationRoutes.Collections.add)
                                },
                                onEditCollection = { collection ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.createEditRoute(collection.id)
                                    )
                                },
                                onEditCollectionById = { id ->
                                    navController.navigate(NavigationRoutes.Collections.createEditRoute(id))
                                },
                                onAdventureClick = onAdventureClick,
                                onEditAdventure = { adventure ->
                                    navController.navigate(
                                        NavigationRoutes.Locations.createEditRoute(
                                            adventureId = adventure.id,
                                            adventureJson = json.encodeToString(
                                                serializer = Location.serializer(),
                                                value = adventure
                                            )
                                        )
                                    )
                                },
                                onAddPhoto = { adventure ->
                                    navController.navigate(
                                        NavigationRoutes.Locations.createEditRoute(
                                            adventureId = adventure.id,
                                            adventureJson = json.encodeToString(
                                                serializer = Location.serializer(),
                                                value = adventure
                                            ),
                                            openImages = true
                                        )
                                    )
                                },
                                onAddTransportation = { collectionId ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Transportations.createAddRoute(
                                            collectionId = collectionId
                                        )
                                    )
                                },
                                onAddPlace = { collectionId ->
                                    navController.navigate(NavigationRoutes.Locations.createAddRoute(collectionId)) { launchSingleTop = true }
                                },
                                onEditTransportation = { transportation ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Transportations.createEditRoute(
                                            transportationId = transportation.id,
                                            transportationJson = json.encodeToString(
                                                serializer = Transportation.serializer(),
                                                value = transportation
                                            )
                                        )
                                    )
                                },
                                onAddNote = { id ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Notes.createAddRoute(id)
                                    )
                                },
                                onAddLodging = { id ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Lodgings.createAddRoute(id)
                                    )
                                },
                                onEditLodging = { id, stay ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Lodgings.createEditRoute(
                                            collectionId = id,
                                            lodgingId = stay.id,
                                            lodgingJson = json.encodeToString(
                                                serializer = Lodging.serializer(),
                                                value = stay
                                            )
                                        )
                                    )
                                },
                                onAddChecklist = { id ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Checklists.createAddRoute(id)
                                    )
                                },
                                onEditChecklist = { id, list ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Checklists.createEditRoute(
                                            collectionId = id,
                                            checklistId = list.id,
                                            checklistJson = json.encodeToString(
                                                serializer = Checklist.serializer(),
                                                value = list
                                            )
                                        )
                                    )
                                },
                                onEditNote = { id, note ->
                                    navController.navigate(
                                        NavigationRoutes.Collections.Notes.createEditRoute(
                                            collectionId = id,
                                            noteId = note.id,
                                            noteJson = json.encodeToString(
                                                serializer = Note.serializer(),
                                                value = note
                                            )
                                        )
                                    )
                                },
                                onHomeClick = { navigateTo(CurrentScreen.HOME) }
                            )
                        }

                        collectionsScreen(
                            registerListRoute = false,
                            navigator = object : CollectionsNavigator {
                                override fun navigateToCollectionDetail(
                                    collectionId: String,
                                    collectionName: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.createDetailRoute(
                                            collectionId = collectionId,
                                            collectionName = collectionName
                                        )
                                    )
                                }

                                override fun navigateToAddNote(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Notes.createAddRoute(collectionId)
                                    )
                                }

                                override fun navigateToEditNote(
                                    collectionId: String,
                                    noteId: String,
                                    noteJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Notes.createEditRoute(
                                            collectionId, noteId, noteJson
                                        )
                                    )
                                }

                                override fun navigateToAddLodging(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Lodgings.createAddRoute(collectionId)
                                    )
                                }

                                override fun navigateToEditLodging(
                                    collectionId: String,
                                    lodgingId: String,
                                    lodgingJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Lodgings.createEditRoute(
                                            collectionId, lodgingId, lodgingJson
                                        )
                                    )
                                }

                                override fun navigateToAddChecklist(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Checklists.createAddRoute(collectionId)
                                    )
                                }

                                override fun navigateToEditChecklist(
                                    collectionId: String,
                                    checklistId: String,
                                    checklistJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Checklists.createEditRoute(
                                            collectionId, checklistId, checklistJson
                                        )
                                    )
                                }

                                override fun navigateToAddCollection() {
                                    navController.navigate(NavigationRoutes.Collections.add)
                                }

                                override fun navigateToEditCollection(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.createEditRoute(collectionId)
                                    )
                                }

                                override fun navigateToAdventure(location: Location) {
                                    onAdventureClick(location)
                                }

                                override fun navigateToEditAdventure(adventure: Location, openImages: Boolean) {
                                    val adventureJson = json.encodeToString(
                                        serializer = Location.serializer(),
                                        value = adventure
                                    )
                                    navController.navigate(
                                        NavigationRoutes.Locations.createEditRoute(
                                            adventureId = adventure.id,
                                            adventureJson = adventureJson,
                                            openImages = openImages
                                        )
                                    )
                                }

                                override fun navigateToAddPlace(collectionId: String) {
                                    navController.navigate(NavigationRoutes.Locations.createAddRoute(collectionId)) { launchSingleTop = true }
                                }

                                override fun navigateToAddTransportation(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Transportations.createAddRoute(
                                            collectionId = collectionId
                                        )
                                    )
                                }

                                override fun navigateToEditTransportation(
                                    transportationId: String,
                                    transportationJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Transportations.createEditRoute(
                                            transportationId = transportationId,
                                            transportationJson = transportationJson
                                        )
                                    )
                                }

                                override fun navigateToHome() {
                                    navigateToHome()
                                }

                                override fun navigateBack() {
                                    navController.navigateUp()
                                }
                            }
                        )

                        lodgingScreen(
                            navigator = object : LodgingNavigator {
                                override fun navigateToAddLodging(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Lodgings.createAddRoute(collectionId)
                                    )
                                }

                                override fun navigateToEditLodging(
                                    collectionId: String,
                                    lodgingId: String,
                                    lodgingJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Lodgings.createEditRoute(
                                            collectionId, lodgingId, lodgingJson
                                        )
                                    )
                                }

                                override fun navigateBack() {
                                    navController.navigateUp()
                                }
                            }
                        )

                        checklistsScreen(
                            navigator = object : ChecklistsNavigator {
                                override fun navigateToAddChecklist(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Checklists.createAddRoute(collectionId)
                                    )
                                }

                                override fun navigateToEditChecklist(
                                    collectionId: String,
                                    checklistId: String,
                                    checklistJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Checklists.createEditRoute(
                                            collectionId, checklistId, checklistJson
                                        )
                                    )
                                }

                                override fun navigateBack() {
                                    navController.navigateUp()
                                }
                            }
                        )

                        notesScreen(
                            navigator = object : NotesNavigator {
                                override fun navigateToAddNote(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Notes.createAddRoute(collectionId)
                                    )
                                }

                                override fun navigateToEditNote(
                                    collectionId: String,
                                    noteId: String,
                                    noteJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Notes.createEditRoute(
                                            collectionId, noteId, noteJson
                                        )
                                    )
                                }

                                override fun navigateBack() {
                                    navController.navigateUp()
                                }
                            }
                        )

                        // Transportations screen with navigator
                        transportationsScreen(
                            navigator = object : TransportationsNavigator {
                                override fun navigateToAddTransportation(collectionId: String) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Transportations.createAddRoute(
                                            collectionId = collectionId
                                        )
                                    )
                                }

                                override fun navigateToEditTransportation(
                                    transportationId: String,
                                    transportationJson: String
                                ) {
                                    navController.navigate(
                                        NavigationRoutes.Collections.Transportations.createEditRoute(
                                            transportationId = transportationId,
                                            transportationJson = transportationJson
                                        )
                                    )
                                }

                                override fun navigateBack() {
                                    navController.navigateUp()
                                }
                            }
                        )

                        settingsScreen(onLogout = onLogout)

                        worldGraph(navController)

                        mapScreen(
                            navController = navController,
                            // The map only knows an id, which is what onOpenLocationById is for -
                            // the same route the web's pin popup offers behind "Ver detalles".
                            onAdventureClick = onOpenLocationById
                        )

                        calendarScreen(
                            // A visit opens its place; a trip, or anything dated inside one, the trip.
                            onOpenPlace = onOpenLocationById,
                            onOpenCollection = { id, name ->
                                navController.navigate(
                                    NavigationRoutes.Collections.createDetailRoute(
                                        collectionId = id,
                                        collectionName = name
                                    )
                                )
                            }
                        )
                    }
                    }
                    if (mapFullBleed) {
                        // A fade behind the status bar, so the clock never sits on a map label.
                        Box(
                            Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), Color.Transparent)
                                    )
                                )
                        )
                        MapSearchPill(
                            onSearch = { searchOpen = true },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        ) {
                            ProfileMenu(
                                user = userDetails,
                                userName = userName,
                                serverUrl = userDetails?.serverUrl.orEmpty(),
                                onSettings = { navigateTo(CurrentScreen.SETTINGS) },
                                onCalendar = { navigateTo(CurrentScreen.CALENDAR) },
                                onUsers = { navigateTo(CurrentScreen.USERS) },
                                onLogout = onLogout
                            )
                        }
                    }
                }
            }
            }
            }

            if (compact && shellActions.chromeHiddenBy == 0) {
                MainToolbarRow(
                    current = currentScreen,
                    onSelect = { navigateTo(it) },
                    action = shellActions.action,
                    scroll = toolbarScroll,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
    }
    }
}

// Previews
@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HomeScreenLoadingPreview() {
    MaterialTheme {
        HomeScreenContent(
            homeUiState = HomeUiState.Loading,
            userDetails = null
        )
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HomeScreenSuccessPreview() {
    val sampleStats = UserStats(
        locationCount = 75,
        visitedLocationCount = 2,
        tripsCount = 33,
        visitedCityCount = 0,
        totalCities = 153728,
        visitedRegionCount = 8,
        totalRegions = 5322,
        visitedCountryCount = 1,
        totalCountries = 250
    )

    MaterialTheme {
        HomeScreenContent(
            homeUiState = HomeUiState.Success(
                userName = "Antonio",
                dashboard = Dashboard(stats = sampleStats)
            ),
            userDetails = UserDetails(
                pk = 123,
                username = "antonio",
                firstName = "Antonio",
                lastName = "Garcia",
                email = "antonio@example.com",
                profilePic = null,
                isStaff = false,
                dateJoined = "2024-01-01",
                uuid = "user-uuid-123",
                publicProfile = true,
                hasPassword = true,
                serverUrl = "https://example-server.com"
            )
        )
    }
}

@org.jetbrains.compose.ui.tooling.preview.Preview
@Composable
private fun HomeScreenErrorPreview() {
    MaterialTheme {
        HomeScreenContent(
            homeUiState = HomeUiState.Error("Could not load your dashboard. Please try again."),
            userDetails = null
        )
    }
}


/** The app's mark - a mountain on the primary colour - and its name. */
@Composable
private fun BrandTitle() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Terrain,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = "AdventureLog",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1
        )
    }
}

/** The floating search pill's height and the gap under it: where Map's own controls start. */
private val MapSearchPillClearance = 72.dp

/**
 * The Map tab's search, floating over the map as the canvas draws it: the whole pill opens the
 * search, and the account button sits at its end where the bar used to have it.
 */
@Composable
private fun MapSearchPill(
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Surface(
        onClick = onSearch,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 6.dp,
        modifier = modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .height(56.dp),
    ) {
        Row(Modifier.padding(start = 18.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Search everything",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            trailing()
        }
    }
}
