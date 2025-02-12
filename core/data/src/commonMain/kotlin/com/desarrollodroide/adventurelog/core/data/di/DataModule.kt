package com.desarrollodroide.adventurelog.core.data.di

import com.desarrollodroide.adventurelog.core.common.di.commonModule
import com.desarrollodroide.adventurelog.core.data.SettingsRepository
import com.desarrollodroide.adventurelog.core.data.SettingsRepositoryImpl
import com.desarrollodroide.adventurelog.core.network.di.networkModule
import com.russhwolf.settings.Settings
import org.koin.dsl.module


val dataModule = module {
    includes(commonModule, networkModule)
    single<Settings> {
        Settings()
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }
}