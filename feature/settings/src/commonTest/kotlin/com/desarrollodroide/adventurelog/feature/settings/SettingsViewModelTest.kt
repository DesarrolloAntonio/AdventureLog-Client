package com.desarrollodroide.adventurelog.feature.settings

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.RefreshVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.feature.settings.domain.BackupExporter
import com.desarrollodroide.adventurelog.feature.settings.domain.BackupResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The two Settings actions that talk to the server, exercised through their use cases.
 *
 * SettingsViewModel itself takes six collaborators, so these drive the pieces it delegates to -
 * which is where the behaviour worth protecting lives.
 */
class SettingsActionsTest {

    private class FakeCountries(
        private val sweep: Either<ApiResponse, Pair<Int, Int>>
    ) : CountriesRepository {
        override val countriesFlow: StateFlow<List<Country>> = MutableStateFlow(emptyList())
        override val visitedRegionsFlow: StateFlow<List<VisitedRegion>> = MutableStateFlow(emptyList())
        override val visitedCitiesFlow: StateFlow<List<VisitedCity>> = MutableStateFlow(emptyList())

        override suspend fun getCountries(): Either<ApiResponse, List<Country>> = Either.Right(emptyList())
        override suspend fun getRegions(countryCode: String): Either<ApiResponse, List<Region>> = Either.Right(emptyList())
        override suspend fun getVisitedRegions(): Either<ApiResponse, List<VisitedRegion>> = Either.Right(emptyList())
        override suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>> = Either.Right(emptyList())
        override suspend fun refreshVisitedRegions(): Either<ApiResponse, Pair<Int, Int>> = sweep
        override suspend fun markRegionVisited(regionId: String) = throw AssertionError("not here")
        override suspend fun unmarkRegionVisited(regionId: String): Either<ApiResponse, Unit> = throw AssertionError("not here")
        override suspend fun refreshCountries(): Either<ApiResponse, List<Country>> = Either.Right(emptyList())
    }

    private class FakeExporter(private val result: BackupResult) : BackupExporter {
        var url: String? = null
        var name: String? = null
        override suspend fun export(serverUrl: String, fileName: String): BackupResult {
            url = serverUrl
            name = fileName
            return result
        }
    }

    @Test
    fun aSweepThatFoundNothingStillSucceeds() = kotlinx.coroutines.test.runTest {
        val useCase = RefreshVisitedRegionsUseCase(FakeCountries(Either.Right(0 to 0)))
        val result = useCase()
        assertTrue(result is Either.Right)
        assertEquals(0 to 0, (result as Either.Right).value)
    }

    @Test
    fun aSweepReportsWhatItFound() = kotlinx.coroutines.test.runTest {
        val useCase = RefreshVisitedRegionsUseCase(FakeCountries(Either.Right(3 to 7)))
        val result = useCase() as Either.Right
        assertEquals(3 to 7, result.value)
    }

    @Test
    fun aSweepThatFailsSaysSomethingAPersonCanRead() = kotlinx.coroutines.test.runTest {
        val useCase = RefreshVisitedRegionsUseCase(FakeCountries(Either.Left(ApiResponse.IOException)))
        val result = useCase() as Either.Left
        // The app's one wording for this since QA 05 (WO-04); it said "Network unavailable".
        assertEquals("Can't reach the server. Check your connection.", result.value)
    }

    @Test
    fun theBackupAsksTheServerForTheExportAndNamesTheFile() = kotlinx.coroutines.test.runTest {
        val exporter = FakeExporter(BackupResult.Handed)
        val result = exporter.export("https://example.test:3447/", "adventurelog-backup-2026-09-10.zip")

        assertEquals(BackupResult.Handed, result)
        assertEquals("https://example.test:3447/", exporter.url)
        assertTrue(exporter.name!!.endsWith(".zip"))
    }
}
