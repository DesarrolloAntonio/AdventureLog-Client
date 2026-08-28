package com.desarrollodroide.adventurelog.core.network.di

import com.desarrollodroide.adventurelog.core.network.BuildConfig
import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.KtorAdventureLogNetwork
import com.desarrollodroide.adventurelog.core.network.datasource.WikipediaNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.KtorWikipediaNetwork
import com.desarrollodroide.adventurelog.core.network.ktor.RedactingHttpLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val KEY = "key"

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

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            prettyPrint = true
        }
    }

    single(named(BuildConfig.APP_NAME)) {
        HttpClient {
            install(ContentNegotiation) {
                json(get())
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