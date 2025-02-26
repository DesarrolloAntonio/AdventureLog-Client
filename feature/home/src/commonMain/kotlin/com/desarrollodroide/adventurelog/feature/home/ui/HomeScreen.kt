package com.desarrollodroide.adventurelog.feature.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Entry point composable that integrates with navigation
 */
@Composable
fun HomeScreenRoute(
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val homeUiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        homeUiState = homeUiState,
        onAdventuresClick = onAdventuresClick,
        onCollectionsClick = onCollectionsClick,
        onTravelClick = onTravelClick,
        onMapClick = onMapClick,
        onCalendarClick = onCalendarClick
    )
}

/**
 * Main home screen composable that integrates all components
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onAdventuresClick: () -> Unit,
    onCollectionsClick: () -> Unit,
    onTravelClick: () -> Unit,
    onMapClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Main drawer with content
    HomeDrawer(
        drawerState = drawerState,
        homeUiState = homeUiState,
        scope = scope,
        onAdventuresClick = onAdventuresClick,
        onCollectionsClick = onCollectionsClick,
        onTravelClick = onTravelClick,
        onMapClick = onMapClick,
        onCalendarClick = onCalendarClick
    ) {
        // Main scaffold with top bar and content
        Scaffold(
            topBar = {
                HomeTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        ) { innerPadding ->
            // Main content
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
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
        }
    }
}