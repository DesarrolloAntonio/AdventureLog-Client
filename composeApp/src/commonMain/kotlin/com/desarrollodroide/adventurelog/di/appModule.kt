package com.desarrollodroide.adventurelog.di

import coil3.annotation.ExperimentalCoilApi
import coil3.network.CacheStrategy
import coil3.network.NetworkFetcher
import coil3.network.ktor3.asNetworkClient
import com.desarrollodroide.adventurelog.feature.home.di.homeModule
import com.desarrollodroide.adventurelog.feature.login.di.loginModule

import io.ktor.client.HttpClient
import org.koin.dsl.module

@OptIn(ExperimentalCoilApi::class)
val appModule = module {
    includes(loginModule, homeModule)
    single {
        NetworkFetcher.Factory(
            networkClient = { get<HttpClient>().asNetworkClient() },
            cacheStrategy = { CacheStrategy.DEFAULT },
        )
    }
}