package com.desarrollodroide.adventurelog.core.data.di

import com.desarrollodroide.adventurelog.core.common.di.AdventureLogDispatchers
import com.desarrollodroide.adventurelog.core.common.di.commonModule
import com.desarrollodroide.adventurelog.core.data.AccountRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.AdventuresRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.CategoriesRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.CollectionsRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.CountriesRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.GeocodeRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.LoginRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.SettingsRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.TransportationRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.DashboardRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.TrailsRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.VisitsRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.UserRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.WikipediaRepositoryImpl
import com.desarrollodroide.adventurelog.core.data.ImagesRepositoryImpl
import com.desarrollodroide.adventurelog.core.domain.repository.AccountRepository
import com.desarrollodroide.adventurelog.core.domain.repository.LocationsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.CategoriesRepository
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.domain.repository.GeocodeRepository
import com.desarrollodroide.adventurelog.core.domain.repository.LoginRepository
import com.desarrollodroide.adventurelog.core.domain.repository.SettingsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.TransportationRepository
import com.desarrollodroide.adventurelog.core.domain.repository.DashboardRepository
import com.desarrollodroide.adventurelog.core.domain.repository.TrailsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.VisitsRepository
import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.domain.repository.WikipediaRepository
import com.desarrollodroide.adventurelog.core.domain.repository.ImagesRepository
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
    single<AccountRepository> {
        AccountRepositoryImpl(
            networkDataSource = get(),
            userRepository = get(),
            ioDispatcher = get(named(AdventureLogDispatchers.IO))
        )
    }
    single<TrailsRepository> {
        TrailsRepositoryImpl(networkDataSource = get())
    }
    single<VisitsRepository> {
        VisitsRepositoryImpl(networkDataSource = get())
    }
    single<DashboardRepository> {
        DashboardRepositoryImpl(networkDataSource = get())
    }
    single<LocationsRepository> {
        AdventuresRepositoryImpl(networkDataSource = get())
    }
    single<CollectionsRepository> {
        CollectionsRepositoryImpl(networkDataSource = get())
    }
    single<CategoriesRepository> {
        CategoriesRepositoryImpl(networkDataSource = get())
    }
    single<CountriesRepository> {
        CountriesRepositoryImpl(networkDataSource = get())
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
    single<TransportationRepository> {
        TransportationRepositoryImpl(
            networkDataSource = get()
        )
    }
    single<ImagesRepository> {
        ImagesRepositoryImpl(
            networkDataSource = get()
        )
    }
}
