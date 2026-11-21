package com.desarrollodroide.adventurelog.di

import com.desarrollodroide.adventurelog.core.data.di.dataModule
import com.desarrollodroide.adventurelog.feature.locations.di.locationsModule
import com.desarrollodroide.adventurelog.feature.calendar.di.calendarModule
import com.desarrollodroide.adventurelog.feature.collections.di.collectionsModule
import com.desarrollodroide.adventurelog.feature.detail.di.detailModule
import com.desarrollodroide.adventurelog.feature.home.di.homeModule
import com.desarrollodroide.adventurelog.feature.login.di.loginModule
import com.desarrollodroide.adventurelog.feature.map.di.mapModule
import com.desarrollodroide.adventurelog.feature.settings.di.settingsModule
import com.desarrollodroide.adventurelog.feature.ui.di.imageLoaderModule
import com.desarrollodroide.adventurelog.feature.world.di.worldModule
import org.koin.dsl.module

/**
 * Everything both apps share. Swift starts Koin with this alone (doInitKoin); Android adds
 * androidUiModule for what only its Compose UI uses.
 */
val appModule = module {
    // Include all feature modules explicitly
    includes(
        dataModule,
        loginModule,
        homeModule,
        locationsModule,
        settingsModule,
        detailModule,
        collectionsModule,
        calendarModule,
        worldModule,
        mapModule,
        imageLoaderModule
    )
}