package com.desarrollodroide.adventurelog.core.network.di

import com.desarrollodroide.adventurelog.BuildConfig
import com.desarrollodroide.adventurelog.core.network.AdventureLogNetworkDataSource
import com.desarrollodroide.adventurelog.core.network.ktor.HAS_IMAGE
import com.desarrollodroide.adventurelog.core.network.ktor.ADVENTURELOG_HOST
import com.desarrollodroide.adventurelog.core.network.ktor.ADVENTURELOG_PATH
import com.desarrollodroide.adventurelog.core.network.ktor.KtorAdventurelogNetwork
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val KEY = "key"

val networkModule = module {
    single<AdventureLogNetworkDataSource> {
        KtorAdventurelogNetwork(
            adventurelogClient = get(named(BuildConfig.APP_NAME)),
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
            install(HttpCookies)

            install(ContentNegotiation) {
                json(get())
            }
            install(HttpCache)
            install(Logging) {
                level = LogLevel.ALL
            }
            install(DefaultRequest) {
                url {
                    protocol = URLProtocol.HTTPS
                    host = ADVENTURELOG_HOST
                    path(ADVENTURELOG_PATH)
                    parameters.append(HAS_IMAGE, true.toString())
                }
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }

    single {
        HttpClient()
    }
}