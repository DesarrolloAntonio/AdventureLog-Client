package com.desarrollodroide.adventurelog.feature.ui.navigation

import androidx.navigation.NavBackStackEntry
import androidx.savedstate.read

/**
 * A string route argument, from whichever of the two places it landed in.
 *
 * Arguments first, saved state second: the country screen was reached with an empty code because
 * only the handle was read, and a route parameter does not always arrive there.
 *
 * Reading it as `arguments?.getString(name)` compiles on Android and nowhere else. `arguments` is
 * a SavedState, which is a typealias for Bundle on Android and a real type everywhere else, so
 * the Bundle method resolved on the one target anyone was building. This reads it through the
 * multiplatform accessor instead.
 */
fun NavBackStackEntry.routeArgument(name: String): String =
    arguments?.read { if (contains(name)) getStringOrNull(name) else null }
        ?: savedStateHandle.get<String>(name)
        ?: ""
