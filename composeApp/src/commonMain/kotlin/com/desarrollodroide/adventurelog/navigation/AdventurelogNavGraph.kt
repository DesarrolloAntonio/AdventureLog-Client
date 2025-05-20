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
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.DetailNavigator
import com.desarrollodroide.adventurelog.feature.detail.ui.navigation.detailNavGraph
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.HomeNavigator
import com.desarrollodroide.adventurelog.feature.ui.navigation.AnimatedNavHost
import com.desarrollodroide.adventurelog.core.model.Adventure
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

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
        override fun goToDetail(adventure: Adventure) {
            val adventureJson = json.encodeToString(adventure)
            navController.navigate("detail?adventureJson=$adventureJson")
        }
    }
    
    val detailNavigator = object : DetailNavigator {
        override fun navigateUp() {
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
    }
}