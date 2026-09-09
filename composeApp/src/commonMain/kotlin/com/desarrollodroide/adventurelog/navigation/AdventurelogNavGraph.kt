package com.desarrollodroide.adventurelog.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.homeNavGraph
import com.desarrollodroide.adventurelog.feature.login.ui.navigation.LoginNavigator
import com.desarrollodroide.adventurelog.feature.login.ui.navigation.loginNavGraph
import com.desarrollodroide.adventurelog.core.common.navigation.NavigationRoutes
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.CollectionsNavigator
import com.desarrollodroide.adventurelog.feature.collections.ui.navigation.collectionsScreen
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.DetailNavigator
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.detailNavGraph
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.HomeNavigator
import com.desarrollodroide.adventurelog.feature.ui.navigation.AnimatedNavHost
import com.desarrollodroide.adventurelog.core.model.Location
import kotlinx.serialization.json.Json

@Composable
fun AdventureLogNavGraph(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController = rememberNavController()
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }
    
    val loginNavigator = object : LoginNavigator {
        override fun goToHome() {
            navController.navigate(NavigationRoutes.Home.graph) {
                popUpTo(NavigationRoutes.Login.graph) { inclusive = true }
            }
        }
    }
    
    val homeNavigator = object : HomeNavigator {
        override fun goToDetail(location: Location) {
            navController.navigate("detail/${location.id}")
        }
        
        override fun goToDetailById(locationId: String) {
            navController.navigate("detail/$locationId")
        }

        override fun goToLogin() {
            navController.navigate(NavigationRoutes.Login.graph) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    val detailNavigator = object : DetailNavigator {
        override fun navigateUp() {
            navController.navigateUp()
        }

        override fun navigateToCollection(collectionId: String, collectionName: String) {
            navController.navigate(
                NavigationRoutes.Collections.createDetailRoute(collectionId, collectionName)
            )
        }
    }

    // A place's detail screen lives on this top-level controller, not MainShell's own nested
    // one, so its "belongs to" chip needs the collection screens registered here too - the
    // Collections tab keeps its own copy of these same routes on MainShell's controller for the
    // primary flow. Editing here is a rare side door (an unrelated adventure or transportation
    // reached this way), so those actions fall back to going home rather than duplicating the
    // add/edit destinations a second time.
    val collectionsFromDetailNavigator = object : CollectionsNavigator {
        override fun navigateToCollectionDetail(collectionId: String, collectionName: String) {
            navController.navigate(
                NavigationRoutes.Collections.createDetailRoute(collectionId, collectionName)
            )
        }
        override fun navigateToAddCollection() {
            navigateToHome()
        }
        override fun navigateToEditCollection(collectionId: String) {
            navigateToHome()
        }
        override fun navigateToAdventure(location: Location) {
            navController.navigate("detail/${location.id}")
        }
        override fun navigateToEditAdventure(adventure: Location) {
            navigateToHome()
        }
        override fun navigateToAddTransportation(collectionId: String) {
            navigateToHome()
        }
        override fun navigateToEditTransportation(transportationId: String, transportationJson: String) {
            navigateToHome()
        }
        override fun navigateToAddNote(collectionId: String) {
            navigateToHome()
        }
        override fun navigateToEditNote(collectionId: String, noteId: String, noteJson: String) {
            navigateToHome()
        }
        override fun navigateToAddChecklist(collectionId: String) {
            navigateToHome()
        }
        override fun navigateToAddLodging(collectionId: String) {
            navigateToHome()
        }
        override fun navigateToEditLodging(
            collectionId: String,
            lodgingId: String,
            lodgingJson: String
        ) {
            navigateToHome()
        }
        override fun navigateToEditChecklist(
            collectionId: String,
            checklistId: String,
            checklistJson: String
        ) {
            navigateToHome()
        }
        override fun navigateToHome() {
            navController.popBackStack(NavigationRoutes.Home.graph, inclusive = false)
        }
        override fun navigateBack() {
            navController.navigateUp()
        }
    }

    AnimatedNavHost(
        modifier = modifier,
        startDestination = NavigationRoutes.Login.graph,
        navController = navController
    ) {
        loginNavGraph(navigator = loginNavigator)
        homeNavGraph(navigator = homeNavigator)
        detailNavGraph(navigator = detailNavigator)
        collectionsScreen(navigator = collectionsFromDetailNavigator)
    }
}