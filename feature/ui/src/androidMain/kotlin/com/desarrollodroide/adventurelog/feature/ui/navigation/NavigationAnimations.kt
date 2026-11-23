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
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
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
     * Material's fade through, for moving between top-level destinations - sections with no order
     * between them, so nothing slides left or right. The outgoing screen fades out over the first
     * 90ms; the incoming one then fades in and grows from 92% over the remaining 210ms.
     */
    fun fadeThroughEnter(): EnterTransition =
        fadeIn(tween(durationMillis = 210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
            scaleIn(tween(durationMillis = 210, delayMillis = 90, easing = standardEasing), initialScale = 0.92f)

    fun fadeThroughExit(): ExitTransition =
        fadeOut(tween(durationMillis = 90, easing = FastOutLinearInEasing))

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
