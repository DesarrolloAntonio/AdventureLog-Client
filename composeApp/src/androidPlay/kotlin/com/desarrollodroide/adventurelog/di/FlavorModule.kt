package com.desarrollodroide.adventurelog.di

import com.desarrollodroide.adventurelog.feature.mapgoogle.GoogleMapsBackend
import com.desarrollodroide.adventurelog.feature.ui.map.GoogleMapBackend
import org.koin.dsl.module

/** The play flavor: Google Maps beside MapLibre, and the "Map engine" setting to choose. */
val flavorModule = module {
    single<GoogleMapBackend> { GoogleMapsBackend() }
}
