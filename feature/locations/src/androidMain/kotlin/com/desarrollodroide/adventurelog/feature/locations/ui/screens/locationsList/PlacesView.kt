package com.desarrollodroide.adventurelog.feature.locations.ui.screens.locationsList

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.desarrollodroide.adventurelog.feature.ui.components.SearchBarAction

/**
 * How Places lays out the library: rows with a thumbnail, as the Expressive canvas draws it, or
 * the photograph across the whole width, as the list was before - for browsing by picture.
 */
enum class PlacesView { ROWS, PHOTOS }

private const val Prefs = "places_ui"
private const val Key = "view"

/** The view last chosen and how to change it, kept on this phone across launches - a preference of how to look, not data. */
@Composable
internal fun rememberPlacesView(): Pair<PlacesView, (PlacesView) -> Unit> {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences(Prefs, Context.MODE_PRIVATE) }
    var view by remember {
        val saved = prefs.getString(Key, null)
        mutableStateOf(PlacesView.entries.firstOrNull { it.name == saved } ?: PlacesView.ROWS)
    }
    return view to { chosen ->
        view = chosen
        prefs.edit().putString(Key, chosen.name).apply()
    }
}

/** Beside Filters: shows the view it switches to. */
@Composable
internal fun PlacesViewToggle(view: PlacesView, onChange: (PlacesView) -> Unit, modifier: Modifier = Modifier) {
    val (icon, description, next) = when (view) {
        PlacesView.ROWS -> Triple(Icons.Outlined.ViewAgenda, "Show photos", PlacesView.PHOTOS)
        PlacesView.PHOTOS -> Triple(Icons.AutoMirrored.Outlined.ViewList, "Show as a list", PlacesView.ROWS)
    }
    SearchBarAction(icon = icon, contentDescription = description, onClick = { onChange(next) }, modifier = modifier)
}
