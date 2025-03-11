package com.desarrollodroide.adventurelog.feature.settings.di

import com.desarrollodroide.adventurelog.core.domain.di.domainModule
import org.koin.core.module.dsl.viewModelOf
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.SettingsViewModel
import org.koin.dsl.module

val homeModule = module {
    includes(domainModule)
    viewModelOf(::SettingsViewModel)
}