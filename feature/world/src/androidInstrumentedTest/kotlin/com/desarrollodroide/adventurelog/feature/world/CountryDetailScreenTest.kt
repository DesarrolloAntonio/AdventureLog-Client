package com.desarrollodroide.adventurelog.feature.world

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CountriesRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCountriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetVisitedRegionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SetRegionVisitedUseCase
import com.desarrollodroide.adventurelog.core.model.Country
import com.desarrollodroide.adventurelog.core.model.Region
import com.desarrollodroide.adventurelog.core.model.VisitedCity
import com.desarrollodroide.adventurelog.core.model.VisitedRegion
import com.desarrollodroide.adventurelog.feature.world.ui.screen.CountryDetailScreen
import com.desarrollodroide.adventurelog.feature.world.viewmodel.CountryDetailViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Test

/** A country's page, seen the way a screen reader and a thumb meet it (QA 05). */
class CountryDetailScreenTest {

    private class Countries : CountriesRepository {
        private val visited = MutableStateFlow(listOf(VisitedRegion(1, "u", "JP-26", "Kyōto", null, null)))
        override val countriesFlow = MutableStateFlow(listOf(Country(1, "Japan", "JP", "", 2, 1, "Eastern Asia", "Tokyo", null, null)))
        override val visitedRegionsFlow = visited
        override val visitedCitiesFlow = MutableStateFlow(emptyList<VisitedCity>())
        override suspend fun getCountries(): Either<ApiResponse, List<Country>> = Either.Right(countriesFlow.value)
        override suspend fun getRegions(countryCode: String): Either<ApiResponse, List<Region>> = Either.Right(
            listOf(Region("JP-23", "Aichi", "Japan", 70, null, null, 109), Region("JP-26", "Kyōto", "Japan", 24, null, null, 109))
        )
        override suspend fun getVisitedRegions(): Either<ApiResponse, List<VisitedRegion>> = Either.Right(visited.value)
        override suspend fun getVisitedCities(): Either<ApiResponse, List<VisitedCity>> = Either.Right(emptyList())
        override suspend fun refreshVisitedRegions(): Either<ApiResponse, Pair<Int, Int>> = Either.Right(0 to 0)
        override suspend fun markRegionVisited(regionId: String): Either<ApiResponse, VisitedRegion> {
            val record = VisitedRegion(2, "u", regionId, regionId, null, null)
            visited.value = visited.value + record
            return Either.Right(record)
        }
        override suspend fun unmarkRegionVisited(regionId: String): Either<ApiResponse, Unit> {
            visited.value = visited.value.filterNot { it.regionId == regionId }
            return Either.Right(Unit)
        }
        override suspend fun refreshCountries(): Either<ApiResponse, List<Country>> = getCountries()
    }

    private fun viewModel(repo: Countries) = CountryDetailViewModel(
        GetRegionsUseCase(repo), GetVisitedRegionsUseCase(repo), GetCountriesUseCase(repo), SetRegionVisitedUseCase(repo)
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun aRegionRowSaysWhetherItIsTickedAndATapChangesIt() = runComposeUiTest {
        // The rows were buttons with an icon described "Visited": no state a screen reader
        // could announce as ticked or not (WO-03).
        setContent { CountryDetailScreen(countryCode = "JP", onNavigateBack = {}, viewModel = viewModel(Countries())) }
        waitUntil(timeoutMillis = 5_000) { onAllNodes(androidx.compose.ui.test.hasText("Aichi")).fetchSemanticsNodes().isNotEmpty() }

        onNodeWithText("Kyōto").assertIsOn()
        onNodeWithText("Aichi").assertIsOff()
        onNodeWithText("Aichi").performClick()
        onNodeWithText("Aichi").assertIsOn()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun thePageHasAWayBack() = runComposeUiTest {
        // onNavigateBack reached the screen and nothing drew it.
        var back = 0
        setContent { CountryDetailScreen(countryCode = "JP", onNavigateBack = { back++ }, viewModel = viewModel(Countries())) }
        waitUntil(timeoutMillis = 5_000) { onAllNodes(androidx.compose.ui.test.hasText("Aichi")).fetchSemanticsNodes().isNotEmpty() }

        onNodeWithContentDescription("Back").assertIsDisplayed().performClick()

        assertEquals(1, back)
    }
}
