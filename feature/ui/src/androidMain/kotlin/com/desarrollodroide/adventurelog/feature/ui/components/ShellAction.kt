package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector

/** A screen's main action ("Add a place"), drawn by the shell beside its floating toolbar. */
@Immutable
class ShellAction(
    val icon: ImageVector,
    val description: String,
    val onClick: () -> Unit,
)

/** Where a screen hands its main action to the shell. Holds the action of the screen on show. */
@Stable
class ShellActionHost {
    var action: ShellAction? by mutableStateOf(null)

    /** How many shown screens draw their own top and bottom bars; the shell draws neither while > 0. */
    var chromeHiddenBy: Int by mutableIntStateOf(0)
}

/** Provided by the shell only where it draws a floating toolbar (a phone); null elsewhere. */
val LocalShellActionHost = staticCompositionLocalOf<ShellActionHost?> { null }

/**
 * Where a screen's main action goes on a wide window: the top of the navigation rail. Kept apart
 * from [LocalShellActionHost], whose presence also means "a phone" (the shell hides its chrome for
 * pages with bars of their own only there).
 */
val LocalShellRailActionHost = staticCompositionLocalOf<ShellActionHost?> { null }

/**
 * How far below the status bar a screen's own floating controls must start: on a screen the shell
 * draws edge to edge (Map), its floating search pill sits at the top. 0 everywhere else.
 */
val LocalShellTopOverlay = compositionLocalOf { 0.dp }

/**
 * Hands this screen's main action to the shell - its floating toolbar on a phone, the top of its
 * rail on a wider window - for as long as the screen is shown. Returns false when the shell has
 * neither (a preview, a test), and the screen then draws its own button as before.
 */
@Composable
fun rememberShellAction(icon: ImageVector, description: String, onClick: () -> Unit): Boolean {
    val host = LocalShellActionHost.current ?: LocalShellRailActionHost.current ?: return false
    val latestOnClick by rememberUpdatedState(onClick)
    DisposableEffect(host, icon, description) {
        val action = ShellAction(icon, description) { latestOnClick() }
        host.action = action
        // During a transition the next screen may already have registered its own: only clear
        // the slot if it still holds this one.
        onDispose { if (host.action === action) host.action = null }
    }
    return true
}

/**
 * For a screen that brings its own top and bottom bars - a place's page, with its photo and its
 * toolbar - shown inside the shell on a phone: the shell's app bar and floating toolbar step aside
 * while it is on screen. Elsewhere (a tablet, or outside the shell) it does nothing.
 */
@Composable
fun HideShellChrome() {
    val host = LocalShellActionHost.current ?: return
    DisposableEffect(host) {
        host.chromeHiddenBy++
        onDispose { host.chromeHiddenBy-- }
    }
}

/**
 * Whether the shell's large title is fully open. The shell's app bar takes a downward drag only
 * after the screen's own pull-to-refresh has had it, so without this a slow drag back to the top
 * started a refresh while the title was still folded away. True outside the shell.
 */
val LocalShellAppBarExpanded = staticCompositionLocalOf<() -> Boolean> { { true } }

/** For a screen's pull-to-refresh `enabled`: only once the shell's title is fully open. */
@Composable
fun shellAppBarExpanded(): Boolean {
    val expanded = LocalShellAppBarExpanded.current
    return remember(expanded) { derivedStateOf(expanded) }.value
}
