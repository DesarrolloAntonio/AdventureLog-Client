package com.desarrollodroide.adventurelog.feature.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
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

    /**
     * Forward and back for a Navigation 3 pane on a phone: the page, opaque, slides in from the
     * right over the list, which drifts a third to the left beneath it; Back slides it off again.
     * The page itself does not fade - fading a covering page lets the list show through it, and
     * on Back it seemed to vanish in a frame or two.
     */
    // No size animation, and so no clip: a page may reach up over the shell's title.
    fun forward(): ContentTransform = ContentTransform(
        targetContentEnter = slideInHorizontally(tween(ANIM_DURATION_MS, easing = standardEasing)) { it },
        initialContentExit = slideOutHorizontally(tween(ANIM_DURATION_MS, easing = standardEasing)) { -it / 3 } +
            fadeOut(tween(ANIM_DURATION_MS, easing = easeOut)),
        sizeTransform = null,
    )

    fun backward(): ContentTransform = ContentTransform(
        targetContentEnter = slideInHorizontally(tween(ANIM_DURATION_MS, easing = standardEasing)) { -it / 3 } +
            fadeIn(tween(ANIM_DURATION_MS, easing = easeIn)),
        initialContentExit = slideOutHorizontally(tween(ANIM_DURATION_MS, easing = standardEasing)) { it },
        sizeTransform = null,
    )

    /** How long [forward] and [backward] take, for anything that moves alongside them. */
    const val SLIDE_DURATION_MS = ANIM_DURATION_MS
}
