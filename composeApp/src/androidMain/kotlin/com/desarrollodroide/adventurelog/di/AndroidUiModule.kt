package com.desarrollodroide.adventurelog.di

import coil3.annotation.ExperimentalCoilApi
import coil3.network.CacheStrategy
import coil3.network.NetworkFetcher
import coil3.network.ktor3.asNetworkClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

/** Bindings only the Android Compose UI uses (Coil). Started next to [appModule]. */
@OptIn(ExperimentalCoilApi::class)
val androidUiModule = module {
    single {
        NetworkFetcher.Factory(
            networkClient = { get<HttpClient>().asNetworkClient() },
            cacheStrategy = { CacheStrategy.DEFAULT },
        )
    }
}
