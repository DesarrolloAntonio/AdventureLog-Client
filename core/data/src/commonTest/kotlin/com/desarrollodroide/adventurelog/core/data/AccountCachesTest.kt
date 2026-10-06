package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.data.di.dataModule
import com.desarrollodroide.adventurelog.core.domain.repository.AccountDataCache
import com.desarrollodroide.adventurelog.core.network.model.response.CountryDTO
import com.desarrollodroide.adventurelog.core.network.model.response.UltraSlimCollectionDTO
import com.desarrollodroide.adventurelog.core.testing.AdventureLogNetworkStub
import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.test.runTest
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

/** What one account leaves in memory for the next one to sign in on the same process. */
class AccountCachesTest {

    private class Server : AdventureLogNetworkStub() {
        var countries = listOf(country(visits = "3"))
        var countryCalls = 0
        var collections = listOf(collection("a-1"), collection("a-2"))
        var collectionCalls = 0

        override suspend fun getCountries(): List<CountryDTO> = countries.also { countryCalls++ }
        override suspend fun getAllCollections(): List<UltraSlimCollectionDTO> = collections.also { collectionCalls++ }
    }

    @Test
    fun `after sign-out the next account gets its countries from its own server`() = runTest {
        val server = Server()
        val repository = CountriesRepositoryImpl(server)
        repository.getCountries()

        repository.clearAccountData()
        server.countries = listOf(country(visits = "0"))
        val next = repository.getCountries() as Either.Right

        assertEquals(0, next.value.single().numVisits, "the previous account's visits were served from memory")
    }

    @Test
    fun `while the same account is signed in the countries are not fetched again`() = runTest {
        val server = Server()
        val repository = CountriesRepositoryImpl(server)

        repository.getCountries()
        repository.getCountries()

        assertEquals(1, server.countryCalls)
    }

    @Test
    fun `after sign-out the next account gets its collections from its own server`() = runTest {
        val server = Server()
        val repository = CollectionsRepositoryImpl(server)
        repository.getAllCollections(forceRefresh = false)

        repository.clearAccountData()
        server.collections = emptyList()
        val next = repository.getAllCollections(forceRefresh = false) as Either.Right

        assertEquals(0, next.value.size, "the previous account's collections were served from memory")
    }

    @Test
    fun `every repository that keeps account data is emptied by sign-out`() {
        val koin = koinApplication {
            allowOverride(true)
            modules(dataModule, module { single<Settings> { MapSettings(mutableMapOf()) } })
        }.koin

        val caches = koin.getAll<AccountDataCache>().map { it::class.simpleName.orEmpty() }.sorted()

        assertEquals(
            listOf("AdventuresRepositoryImpl", "CollectionsRepositoryImpl", "CountriesRepositoryImpl", "UserRepositoryImpl"),
            caches
        )
    }

    private companion object {
        fun country(visits: String) = CountryDTO(
            id = 1, name = "Japan", countryCode = "JP", flagUrl = "", numRegions = "47", numVisits = visits
        )

        fun collection(id: String) = UltraSlimCollectionDTO(
            id = id, name = id, createdAt = "2026-01-01T00:00:00Z", updatedAt = "2026-01-01T00:00:00Z"
        )
    }
}
