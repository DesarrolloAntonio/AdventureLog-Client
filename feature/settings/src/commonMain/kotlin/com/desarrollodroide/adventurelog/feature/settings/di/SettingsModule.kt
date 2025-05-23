package com.desarrollodroide.adventurelog.feature.settings.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel

val settingsModule = module {
    includes(domainModule)
    viewModelOf(::SettingsViewModel)
}