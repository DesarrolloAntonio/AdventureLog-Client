package com.desarrollodroide.adventurelog.feature.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry

/**
 * Navigation animations for transitions between screens
 */
object NavigationAnimations {
    
    // Duration for animations
    private const val ANIM_DURATION_MS = 300
    
    // Standard easing curves
    private val easeIn = EaseIn
    private val easeOut = EaseOut
    private val standardEasing = FastOutSlowInEasing
    
    /**
     * Default enter transition for standard horizontal navigation
     */
    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = standardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeIn
            )
        )
    }
    
    /**
     * Default exit transition for standard horizontal navigation
     */
    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 3 },
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = standardEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeOut
            )
        )
    }
    
    /**
     * Default pop enter transition (when navigating back)
     */
    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 3 },
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = standardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeIn
            )
        )
    }
    
    /**
     * Default pop exit transition (when navigating back)
     */
    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = standardEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeOut
            )
        )
    }
    
    /**
     * Vertical enter transition for dialogs or bottom sheets
     */
    val enterTransitionVertical: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = standardEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeIn
            )
        )
    }
    
    /**
     * Vertical exit transition for dialogs or bottom sheets
     */
    val exitTransitionVertical: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = standardEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeOut
            )
        )
    }
    
    /**
     * Fade-only enter transition for overlays
     */
    val enterTransitionFade: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeIn
            )
        )
    }
    
    /**
     * Fade-only exit transition for overlays
     */
    val exitTransitionFade: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(
            animationSpec = tween(
                durationMillis = ANIM_DURATION_MS,
                easing = easeOut
            )
        )
    }
}
