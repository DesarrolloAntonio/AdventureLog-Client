package com.desarrollodroide.adventurelog.feature.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first

/**
 * The camera of an [AppMap], whichever engine draws it. Zoom levels are Google's (and the web
 * tiles'): 5 a country, 12 a city, 15 a street.
 *
 * A move asked for before the map is on screen waits for it, so screens can call these from a
 * `LaunchedEffect` as soon as they know where to look.
 */
@Stable
class AppMapCamera internal constructor(
    /** Where the map opens, before any move. */
    val initialCenter: GeoPoint,
    val initialZoom: Float,
) {
    private var driver by mutableStateOf<Driver?>(null)

    /** Frames [bounds], keeping [padding] clear on every side. */
    suspend fun fit(bounds: GeoBounds, padding: Dp, animate: Boolean = true) =
        awaitDriver().fit(bounds, padding, animate)

    /** Centres the map on [center] at [zoom]. */
    suspend fun moveTo(center: GeoPoint, zoom: Float, animate: Boolean = true) =
        awaitDriver().moveTo(center, zoom, animate)

    private suspend fun awaitDriver(): Driver = snapshotFlow { driver }.filterNotNull().first()

    /** For map backends only: the engine that carries out the moves while it is on screen. */
    fun attach(driver: Driver) {
        this.driver = driver
    }

    fun detach(driver: Driver) {
        if (this.driver === driver) this.driver = null
    }

    /** Implemented by each engine. */
    interface Driver {
        suspend fun fit(bounds: GeoBounds, padding: Dp, animate: Boolean)
        suspend fun moveTo(center: GeoPoint, zoom: Float, animate: Boolean)
    }
}

@Composable
fun rememberAppMapCamera(center: GeoPoint, zoom: Float): AppMapCamera =
    remember { AppMapCamera(center, zoom) }
