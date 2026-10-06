package com.desarrollodroide.adventurelog.feature.collections.ui.state

import com.desarrollodroide.adventurelog.core.domain.usecase.CANT_REACH_SERVER
import kotlinx.io.IOException

/**
 * What the collections list says when a page can't be loaded - not the exception's own text. Offline
 * it printed "Unable to resolve host "…": No address associated with hostname", the server's address
 * and all, where every other screen says it can't reach the server (QA, 2026-10-06).
 */
internal fun collectionsLoadErrorMessage(error: Throwable): String = when (error) {
    is IOException -> CANT_REACH_SERVER
    else -> "Your collections could not be loaded. Please try again."
}
