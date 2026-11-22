package com.desarrollodroide.adventurelog.feature.ui.components

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
 * Hands this screen's main action to the shell's floating toolbar, for as long as the screen is
 * shown. Returns false when there is no toolbar (a tablet, a preview, a test), and the screen
 * then draws its own button as before.
 */
@Composable
fun rememberShellAction(icon: ImageVector, description: String, onClick: () -> Unit): Boolean {
    val host = LocalShellActionHost.current ?: return false
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
