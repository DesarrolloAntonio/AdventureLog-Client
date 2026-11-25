package com.desarrollodroide.adventurelog.feature.home.ui.screen

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import com.desarrollodroide.adventurelog.feature.ui.components.LocalShellActionHost
import com.desarrollodroide.adventurelog.feature.ui.components.LocalShellChromeHiddenByPane
import com.desarrollodroide.adventurelog.feature.ui.components.LocalShellTitleOverlap
import com.desarrollodroide.adventurelog.feature.ui.components.bleedUp
import com.desarrollodroide.adventurelog.feature.ui.navigation.NavigationAnimations

/**
 * The same directive with the scaffold's auto-focus turned off.
 *
 * By default the list-detail scaffold moves focus into the pane it considers current whenever that
 * changes, and focus enters the pane at its first focusable node - on Places and Collections, the
 * search field. On a tablet every visit to either tab raised the keyboard with nothing typed.
 * `copy()` does not carry the flag, so the directive is rebuilt field by field.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun PaneScaffoldDirective.withoutAutoFocus(): PaneScaffoldDirective = PaneScaffoldDirective(
    maxHorizontalPartitions = maxHorizontalPartitions,
    horizontalPartitionSpacerSize = horizontalPartitionSpacerSize,
    maxVerticalPartitions = maxVerticalPartitions,
    verticalPartitionSpacerSize = verticalPartitionSpacerSize,
    defaultPanePreferredWidth = defaultPanePreferredWidth,
    defaultPanePreferredHeight = defaultPanePreferredHeight,
    excludedBounds = excludedBounds,
    shouldAutoFocusCurrentDestination = false,
)

/** The list-detail strategy for screens whose list opens with a text field: no focus moved on its own. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun <T : Any> rememberQuietListDetailSceneStrategy(): ListDetailSceneStrategy<T> =
    rememberListDetailSceneStrategy(
        directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()).withoutAutoFocus()
    )

/**
 * Whether the list-detail strategy is showing both panes right now - read from the same directive
 * the strategy lays out with. "Wider than compact" is not the same thing: a tablet held upright
 * (about 700dp) is medium width yet gets one pane, and screens that thought otherwise opened the
 * first item over their list with no Back arrow to leave it.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun rememberShowsTwoPanes(): Boolean =
    calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()).maxHorizontalPartitions > 1

/**
 * On a phone a chosen page slides in over the list and back out again, as every other page the
 * app opens does. The library's default is a slow cross-fade, which over two see-through screens
 * left the list and the page ghosted on top of each other. Two panes keep the default: there the
 * list stays where it is and only the right-hand pane changes.
 */
internal fun paneTransition(twoPanes: Boolean): AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    if (twoPanes) defaultTransitionSpec() else ({ NavigationAnimations.forward() })

internal fun panePopTransition(twoPanes: Boolean): AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    if (twoPanes) defaultPopTransitionSpec() else ({ NavigationAnimations.backward() })

internal fun panePredictivePopTransition(twoPanes: Boolean): AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform =
    if (twoPanes) defaultPredictivePopTransitionSpec() else ({ NavigationAnimations.backward() })

/**
 * Puts the shell's title and floating toolbar away while [detailShown] - a page with bars of its
 * own on a phone - and takes that job from the page itself. Decided by the pane (its back stack,
 * and whether its list is on show), the chrome starts coming back as Back starts, alongside the
 * slide, instead of popping in once the page has left composition at its end.
 */
@Composable
internal fun PaneHidesShellChrome(detailShown: Boolean, content: @Composable () -> Unit) {
    val host = LocalShellActionHost.current
    if (detailShown && host != null) {
        DisposableEffect(host) {
            host.chromeHiddenBy++
            host.chromeHiddenInPlaceBy++
            onDispose {
                host.chromeHiddenBy--
                host.chromeHiddenInPlaceBy--
            }
        }
    }
    CompositionLocalProvider(LocalShellChromeHiddenByPane provides true, content = content)
}

/**
 * A pane's page on a phone, on the app's own ground: it slides over the list, which would
 * otherwise show through it. Two panes leave it as it is.
 */
@Composable
internal fun PanePage(twoPanes: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .then(
                if (twoPanes) Modifier
                // Up over the shell's title as it fades, so the page is where it will stay.
                else Modifier
                    .bleedUp(LocalShellTitleOverlap.current)
                    .background(MaterialTheme.colorScheme.surface)
            )
            .fillMaxSize()
    ) { content() }
}

/**
 * Whether a pane's list is on show or on its way in - read from the list's own transition, not
 * from the back stack. The back stack changes only when a Back swipe is let go, while the list
 * is already on screen under the page from the start of the drag: driven by the back stack the
 * shell's title was missing over it, and arrived with a jump as the swipe ended.
 */
@Stable
internal class PaneListOnShow {
    var count by mutableIntStateOf(0)

    val isOnShow: Boolean get() = count > 0
}

/** Called first thing in a pane's list entry. */
@Composable
internal fun PaneListOnShow.Mark() {
    val target = LocalNavAnimatedContentScope.current.transition.targetState
    if (target == EnterExitState.Visible) {
        DisposableEffect(this) {
            count++
            onDispose { count-- }
        }
    }
}
