package com.desarrollodroide.adventurelog.core.data.di

import com.desarrollodroide.adventurelog.core.common.di.AdventureLogDispatchers
import com.desarrollodroide.adventurelog.core.common.di.commonModule
import com.desarrollodroide.adventurelog.core.data.AdventuresRepository
import com.desarrollodroide.adventurelog.core.data.AdventuresRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.CollectionsRepository
import com.desarrollodroide.adventurelog.core.data.CollectionsRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.LoginRepository
import com.desarrollodroide.adventurelog.core.data.LoginRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.SettingsRepository
import com.desarrollodroide.adventurelog.core.data.SettingsRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.UserRepository
import com.desarrollodroide.adventurelog.core.data.UserRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.CategoriesRepository
import com.desarrollodroide.adventurelog.core.data.CategoriesRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.GeocodeRepository
import com.desarrollodroide.adventurelog.core.data.GeocodeRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.repository.WikipediaRepository
import com.desarrollodroide.adventurelog.core.data.repository.WikipediaRepositoryImpl
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
    single<UserRepository> {
        UserRepositoryImpl(
            settings = get(),
            networkDataSource = get()
        )
    }
    single<AdventuresRepository> {
        AdventuresRepositoryImpl(networkDataSource = get())
    }
    single<CollectionsRepository> {
        CollectionsRepositoryImpl(networkDataSource = get())
    }
    single<CategoriesRepository> {
        CategoriesRepositoryImpl(networkDataSource = get())
    }
    single<GeocodeRepository> {
        GeocodeRepositoryImpl(
            networkDataSource = get(),
            ioDispatcher = get(named(AdventureLogDispatchers.IO))
        )
    }
    single<WikipediaRepository> {
        WikipediaRepositoryImpl(
            wikipediaDataSource = get()
        )
    }
}