package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import app.cash.paging.LoadState
import app.cash.paging.LoadStateLoading
import kotlinx.io.IOException

/**
 * A pull to refresh is over once the load stops, whether it worked or not. Only success used to end
 * it, so with no network the spinner turned for good (measured at 90 s).
 */
internal fun refreshHasEnded(refresh: LoadState): Boolean = refresh !is LoadStateLoading

/** What the list says when places can't be loaded - not the exception's own text. */
internal fun placesLoadErrorMessage(error: Throwable): String = when (error) {
    is IOException -> "Can't reach the server. Check your connection."
    else -> "Your places could not be loaded. Please try again."
}
