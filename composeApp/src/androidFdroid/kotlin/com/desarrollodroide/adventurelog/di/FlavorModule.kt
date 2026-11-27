package com.desarrollodroide.adventurelog.di

import org.koin.dsl.module

/**
 * The fdroid flavor registers nothing: without a Google Maps engine every map is MapLibre, and
 * Settings hides the "Map engine" choice.
 */
val flavorModule = module { }
