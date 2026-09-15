package com.desarrollodroide.adventurelog.core.network.di

import com.desarrollodroide.adventurelog.core.network.BuildConfig
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.KtorAdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.datasource.WikipediaNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.KtorWikipediaNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.RedactingHttpLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val KEY = "key"

/**
 * What the API client writes request bodies with. A null property is left out of the body rather
 * than sent as null - an update then only touches what it names.
 */
internal val apiJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    prettyPrint = true
}

val networkModule = module {
    single<AdventureLogNetwork> {
        KtorAdventureLogNetwork(
            adventurelogClient = get(named(BuildConfig.APP_NAME)),
        )
    }
    
    single<WikipediaNetworkDataSource> {
        KtorWikipediaNetwork(
            httpClient = get(named(BuildConfig.APP_NAME))
        )
    }

    single { apiJson }

    single(named(BuildConfig.APP_NAME)) {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
            }
            // An address that silently drops traffic - a home server seen from outside, a VPN that is
            // off - held the login screen for 72 s on the engine's default (measured), so connecting
            // gets 15 s. A server that accepts and then says nothing held Home on a spinner for two
            // minutes (measured), so a silence of 30 s between bytes ends a request too. There is no
            // limit on a whole request: a photo upload over a slow link can take minutes while bytes
            // keep moving - and backup and file downloads go through the image client, not this one.
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 30_000
            }
            install(Logging) {
                // Bodies are only printed when explicitly opted in at build time, and even then
                // RedactingHttpLogger strips passwords, tokens and session cookies.
                level = if (BuildConfig.HTTP_LOGGING) LogLevel.BODY else LogLevel.NONE
                logger = RedactingHttpLogger()
            }
        }
    }

    single {
        HttpClient()
    }
}