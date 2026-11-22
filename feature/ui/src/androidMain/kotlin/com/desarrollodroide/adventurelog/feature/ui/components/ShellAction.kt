package com.desarrollodroide.adventurelog.feature.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
