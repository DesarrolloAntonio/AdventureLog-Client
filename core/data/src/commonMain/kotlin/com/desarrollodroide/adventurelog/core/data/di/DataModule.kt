package com.desarrollodroide.adventurelog.core.data.di

import com.desarrollodroide.adventurelog.core.common.di.AdventureLogDispatchers
import com.desarrollodroide.adventurelog.core.common.di.commonModule
import com.desarrollodroide.adventurelog.core.data.LoginRepository
import com.desarrollodroide.adventurelog.core.data.LoginRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.SettingsRepository
import com.desarrollodroide.adventurelog.core.data.SettingsRepositoryImpl
import com.desarrollodroide.adventurelog.core.network.di.networkModule
import com.russhwolf.settings.Settings
import org.koin.core.qualifier.named
import org.koin.dsl.module


val dataModule = module {
    includes(commonModule, networkModule)
    single<Settings> {
        Settings()
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }
    single<LoginRepository>{
        LoginRepositoryImpl(
            adventureLogNetworkDataSource = get(),
            ioDispatcher =  get(named(AdventureLogDispatchers.IO))
        )
    }
}