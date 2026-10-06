package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetRecommendationsUseCase
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.Recommendation
import com.desarrollodroide.adventurelog.core.model.RecommendationCategory
import com.desarrollodroide.adventurelog.core.testing.CategoriesRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testCategory
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.RecommendationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RecommendationsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun before() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun after() = Dispatchers.resetMain()

    private fun recommendation(
        id: String,
        name: String = id,
        primaryType: String? = null,
        types: List<String> = emptyList(),
        website: String? = null
    ) = Recommendation(
        id = id,
        name = name,
        description = null,
        latitude = 1.0,
        longitude = 2.0,
        address = null,
        distanceKm = 0.5,
        primaryType = primaryType,
        types = types,
        website = website,
        phoneNumber = null,
        openingHours = emptyList()
    )

    /** Records what the create call was asked for, which is the whole point of most of these. */
    private class RecordingLocations(
        val results: Either<ApiResponse, List<Recommendation>> = Either.Right(emptyList())
    ) : LocationsRepositoryStub() {
        var lastCategory: Category? = null
        var lastCollectionIds: List<String>? = null
        var lastLatitude: Double? = null
        var lastPlace: String? = null
        var lastRadius: Int? = null
        var lastSearchCategory: RecommendationCategory? = null

        override suspend fun getRecommendations(
            latitude: Double?,
            longitude: Double?,
            place: String?,
            category: RecommendationCategory,
            radiusMetres: Int
        ): Either<ApiResponse, List<Recommendation>> {
            lastLatitude = latitude
            lastPlace = place
            lastRadius = radiusMetres
            lastSearchCategory = category
            return results
        }

        override suspend fun createLocation(
            name: String,
            description: String,
            category: Category,
            rating: Double,
            link: String,
            location: String,
            latitude: String?,
            longitude: String?,
            isPublic: Boolean,
            visits: List<com.desarrollodroide.adventurelog.core.model.VisitFormData>,
            price: Double?,
            priceCurrency: String?,
            activityTypes: List<String>,
            collectionIds: List<String>
        ): Either<ApiResponse, Location> {
            lastCategory = category
            lastCollectionIds = collectionIds
            return Either.Right(testLocation(name))
        }
    }

    private class Categories(private val categories: List<Category>) : CategoriesRepositoryStub() {
        override suspend fun getCategories(): Either<ApiResponse, List<Category>> =
            Either.Right(categories)
    }

    private fun viewModel(
        locations: RecordingLocations,
        categories: List<Category> = listOf(testCategory("General"))
    ) = RecommendationsViewModel(
        getRecommendationsUseCase = GetRecommendationsUseCase(locations),
        createLocationUseCase = CreateLocationUseCase(locations),
        getCategoriesUseCase = GetCategoriesUseCase(Categories(categories))
    )

    @Test
    fun aSearchWithNeitherAPointNorANameIsRefusedBeforeItLeaves() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val model = viewModel(locations)

        model.search(anchor = null)
        advanceUntilIdle()

        assertNull(locations.lastRadius, "the repository should not have been called")
        assertEquals(
            "Choose a place to look around, or type where to look",
            model.uiState.value.errorMessage
        )
    }

    @Test
    fun anAnchorSendsCoordinatesAndAQuerySendsAName() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val model = viewModel(locations)

        model.search(anchor = 40.0 to -3.0)
        advanceUntilIdle()
        assertEquals(40.0, locations.lastLatitude)

        model.onQueryChanged("Kyoto")
        model.search(anchor = null)
        advanceUntilIdle()
        assertEquals("Kyoto", locations.lastPlace)
        assertNull(locations.lastLatitude)
    }

    @Test
    fun theCategoryAndRadiusThatWereChosenAreTheOnesSent() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val model = viewModel(locations)

        model.onCategorySelected(RecommendationCategory.FOOD)
        model.onRadiusSelected(25000)
        model.search(anchor = 1.0 to 2.0)
        advanceUntilIdle()

        assertEquals(RecommendationCategory.FOOD, locations.lastSearchCategory)
        assertEquals(25000, locations.lastRadius)
    }

    @Test
    fun aFailedSearchClearsTheResultsRatherThanLeavingTheOldOnesUnderAnError() =
        runTest(dispatcher) {
            val locations = RecordingLocations(Either.Right(listOf(recommendation("a"))))
            val model = viewModel(locations)
            model.search(anchor = 1.0 to 2.0)
            advanceUntilIdle()
            assertEquals(1, model.uiState.value.results.size)

            val failing = object : LocationsRepositoryStub() {
                override suspend fun getRecommendations(
                    latitude: Double?,
                    longitude: Double?,
                    place: String?,
                    category: RecommendationCategory,
                    radiusMetres: Int
                ): Either<ApiResponse, List<Recommendation>> = Either.Left(ApiResponse.HttpError)
            }
            val second = RecommendationsViewModel(
                getRecommendationsUseCase = GetRecommendationsUseCase(failing),
                createLocationUseCase = CreateLocationUseCase(locations),
                getCategoriesUseCase = GetCategoriesUseCase(Categories(emptyList()))
            )
            second.search(anchor = 1.0 to 2.0)
            advanceUntilIdle()
            assertTrue(second.uiState.value.results.isEmpty())
            assertEquals("Could not search there. Try somewhere else.", second.uiState.value.errorMessage)
        }

    @Test
    fun aSearchThatFoundNothingIsNotTheSameAsNoSearchYet() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val model = viewModel(locations)
        assertEquals(false, model.uiState.value.hasSearched)

        model.search(anchor = 1.0 to 2.0)
        advanceUntilIdle()
        assertEquals(true, model.uiState.value.hasSearched)
        assertTrue(model.uiState.value.results.isEmpty())
    }

    @Test
    fun addingOneJoinsItToTheCollectionInTheSameCall() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val model = viewModel(locations)
        var added: Location? = null

        model.addAsPlace(recommendation("a", "Ichiran"), "collection-1") { added = it }
        advanceUntilIdle()

        assertEquals(listOf("collection-1"), locations.lastCollectionIds)
        assertEquals("Ichiran", added?.name)
        assertTrue("a" in model.uiState.value.added)
    }

    @Test
    fun addingTheSameOneTwiceOnlyReachesTheServerOnce() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val model = viewModel(locations)
        var addedTimes = 0

        model.addAsPlace(recommendation("a"), "c") { addedTimes++ }
        advanceUntilIdle()
        model.addAsPlace(recommendation("a"), "c") { addedTimes++ }
        advanceUntilIdle()

        assertEquals(1, addedTimes)
    }

    @Test
    fun theCategoryIsMatchedFromTheAccountsOwnListNotInventedFromTheSourcesWord() =
        runTest(dispatcher) {
            val locations = RecordingLocations()
            val model = viewModel(
                locations,
                categories = listOf(testCategory("General"), testCategory("Restaurant"))
            )

            model.addAsPlace(recommendation("a", primaryType = "restaurant"), "c") {}
            advanceUntilIdle()

            assertEquals("Restaurant", locations.lastCategory?.displayName)
        }

    @Test
    fun aTypeTheAccountHasNoCategoryForFallsBackRatherThanCreatingOne() = runTest(dispatcher) {
        val locations = RecordingLocations()
        val model = viewModel(locations, categories = listOf(testCategory("General")))

        model.addAsPlace(recommendation("a", primaryType = "archaeological_site"), "c") {}
        advanceUntilIdle()

        assertEquals("General", locations.lastCategory?.displayName)
    }
}
