package com.desarrollodroide.adventurelog.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desarrollodroide.adventurelog.feature.adventures.navigation.adventuresGraph
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.Home
import com.desarrollodroide.adventurelog.feature.home.ui.navigation.homeGraph
import com.desarrollodroide.adventurelog.feature.login.login.navigation.Login
import com.desarrollodroide.adventurelog.feature.login.login.navigation.loginGraph

/**
 * Gráfico de navegación principal de la aplicación
 * Ahora simplificado: settings y otras pantallas se manejan internamente en el HomeScreen
 */
@Composable
fun AdventureLogNavGraph(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState,
    startDestination: Any = Login,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        startDestination = startDestination,
        navController = navController,
    ) {
        loginGraph(
            navController = navController,
            onBackClick = navController::navigateUp,
            onNavigateToHome = {
                navController.navigate(Home) {
                    popUpTo(Login) { inclusive = true }
                }
            }
        )

        homeGraph(
            navController = navController,
            onBackClick = navController::navigateUp,
            onAdventureClick = { adventureId ->
                // Este callback ya no navegará a otra pantalla, sino que se manejará internamente en Home
            }
        )

        adventuresGraph(
            navController = navController,
            onBackClick = navController::navigateUp
        )
    }
}