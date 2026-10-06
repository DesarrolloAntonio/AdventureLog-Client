package com.desarrollodroide.adventurelog.feature.ui.util

import androidx.compose.ui.platform.UriHandler

/**
 * Opens [uri] with whatever the device has for it, or returns false when nothing can. The platform
 * handler throws instead: a place's `ftp://` link, which the server accepts, crashed the app (measured).
 */
fun UriHandler.tryOpenUri(uri: String): Boolean = try {
    openUri(uri)
    true
} catch (e: IllegalArgumentException) {
    false
}

const val CANNOT_OPEN_LINK = "Nothing on this device can open this link."
