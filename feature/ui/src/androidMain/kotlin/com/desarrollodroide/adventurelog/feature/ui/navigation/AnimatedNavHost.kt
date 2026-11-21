package com.desarrollodroide.adventurelog.feature.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

/**
 * An enhanced version of NavHost with predefined animations
 * Applies the navigation animations from NavigationAnimations but allows overrides
 */
@Composable
fun AnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    route: String? = null,
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
        NavigationAnimations.enterTransition,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
        NavigationAnimations.exitTransition,
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) =
        NavigationAnimations.popEnterTransition,
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) =
        NavigationAnimations.popExitTransition,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        route = route,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        builder = builder
    )
}

/**
 * Enhanced version with smarter direction-aware transitions
 * Automatically determines the direction of navigation based on the route index
 */
@Composable
fun AnimatedDirectionalNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier,
    route: String? = null,
    routeToIndexMapper: (String) -> Int = { 0 }, // Map routes to an ordered index
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        route = route,
        enterTransition = {
            val targetIndex = routeToIndexMapper(targetState.destination.route ?: "")
            val initialIndex = routeToIndexMapper(initialState.destination.route ?: "")
            
            if (targetIndex > initialIndex) {
                // Moving forward in navigation
                NavigationAnimations.enterTransition(this)
            } else {
                // Moving backward in navigation
                NavigationAnimations.popEnterTransition(this)
            }
        },
        exitTransition = {
            val targetIndex = routeToIndexMapper(targetState.destination.route ?: "")
            val initialIndex = routeToIndexMapper(initialState.destination.route ?: "")
            
            if (targetIndex > initialIndex) {
                // Moving forward in navigation
                NavigationAnimations.exitTransition(this)
            } else {
                // Moving backward in navigation
                NavigationAnimations.popExitTransition(this)
            }
        },
        popEnterTransition = NavigationAnimations.popEnterTransition,
        popExitTransition = NavigationAnimations.popExitTransition,
        builder = builder
    )
}
