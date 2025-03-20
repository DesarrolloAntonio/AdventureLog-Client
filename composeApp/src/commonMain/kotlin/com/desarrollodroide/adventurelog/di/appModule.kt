package com.desarrollodroide.adventurelog.di

import coil3.annotation.ExperimentalCoilApi
import coil3.network.CacheStrategy
import coil3.network.NetworkFetcher
import coil3.network.ktor3.asNetworkClient
import com.desarrollodroide.adventurelog.feature.adventures.di.adventureModule
import com.desarrollodroide.adventurelog.feature.detail.di.detailModule
import com.desarrollodroide.adventurelog.feature.home.di.homeModule
import com.desarrollodroide.adventurelog.feature.login.di.loginModule
import com.desarrollodroide.adventurelog.feature.settings.di.settingsModule

import io.ktor.client.HttpClient
import org.koin.dsl.module

@OptIn(ExperimentalCoilApi::class)
val appModule = module {
    // Include all feature modules explicitly
    includes(
        loginModule,
        homeModule,
        adventureModule,
        settingsModule,
        detailModule
    )
    
    single {
        NetworkFetcher.Factory(
            networkClient = { get<HttpClient>().asNetworkClient() },
            cacheStrategy = { CacheStrategy.DEFAULT },
        )
    }
}