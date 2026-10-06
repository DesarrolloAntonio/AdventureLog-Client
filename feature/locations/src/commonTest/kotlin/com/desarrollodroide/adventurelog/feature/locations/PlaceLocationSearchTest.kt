package com.desarrollodroide.adventurelog.feature.locations

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ReverseGeocodeUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchWikipediaImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationImagesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationTrailsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationVisitsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UploadImageUseCase
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.testing.CategoriesRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.FakeUserRepository
import com.desarrollodroide.adventurelog.core.testing.GeocodeRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.HeldGeocodeRepository
import com.desarrollodroide.adventurelog.core.testing.ImagesRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.TrailsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.VisitsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.WikipediaRepositoryStub
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.AddEditAdventureViewModel
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.LOCATION_SEARCH_DEBOUNCE_MS
import com.desarrollodroide.adventurelog.feature.ui.util.ImageBytesProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/** QA RL-05: the place form's location search showed the results of an older query that answered last. */
class PlaceLocationSearchTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun before() = Dispatchers.setMain(dispatcher)
    @AfterTest fun after() = Dispatchers.resetMain()

    private object NoImages : ImageBytesProvider {
        override fun getImageBytes(uri: String): ByteArray? = null
        override fun getFileName(uri: String): String = uri
        override suspend fun downloadImageFromUrl(url: String): ByteArray? = null
    }

    private fun viewModel(geocoder: HeldGeocodeRepository): AddEditAdventureViewModel {
        val places = object : LocationsRepositoryStub() {}
        val images = object : ImagesRepositoryStub() {}
        return AddEditAdventureViewModel(
            createLocationUseCase = CreateLocationUseCase(places),
            updateLocationUseCase = UpdateLocationUseCase(places),
            getLocationUseCase = GetLocationUseCase(places),
            getCategoriesUseCase = GetCategoriesUseCase(object : CategoriesRepositoryStub() {
                override suspend fun getCategories(): Either<ApiResponse, List<Category>> = Either.Right(emptyList())
            }),
            generateDescriptionUseCase = GenerateDescriptionUseCase(places),
            searchLocationsUseCase = SearchLocationsUseCase(geocoder),
            reverseGeocodeUseCase = ReverseGeocodeUseCase(object : GeocodeRepositoryStub() {}),
            searchWikipediaImageUseCase = SearchWikipediaImageUseCase(object : WikipediaRepositoryStub() {}),
            createCategoryUseCase = CreateCategoryUseCase(object : CategoriesRepositoryStub() {}),
            uploadImageUseCase = UploadImageUseCase(images),
            syncLocationVisitsUseCase = SyncLocationVisitsUseCase(object : VisitsRepositoryStub() {}),
            syncLocationTrailsUseCase = SyncLocationTrailsUseCase(object : TrailsRepositoryStub() {}),
            syncLocationImagesUseCase = SyncLocationImagesUseCase(images),
            imageBytesProvider = NoImages,
            userRepository = FakeUserRepository()
        )
    }

    private fun AddEditAdventureViewModel.shown() = uiState.value.locationSearchResults.map { it.name }

    @Test
    fun `an older query that answers last does not replace the results of the newer one`() = runTest(dispatcher) {
        val geocoder = HeldGeocodeRepository()
        val vm = viewModel(geocoder)
        advanceUntilIdle()

        vm.searchLocations("Valen")
        advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS + 1)
        runCurrent()
        vm.searchLocations("Valencia")
        advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS + 1)
        runCurrent()

        geocoder.answer("Valencia", "Valencia")
        runCurrent()
        geocoder.answer("Valen", "Valen")
        advanceUntilIdle()

        assertEquals(listOf("Valencia"), vm.shown())
        assertFalse(vm.uiState.value.isSearchingLocation)
    }

    @Test
    fun `the results of the query in the field are shown`() = runTest(dispatcher) {
        val geocoder = HeldGeocodeRepository()
        val vm = viewModel(geocoder)
        advanceUntilIdle()

        vm.searchLocations("Valencia")
        advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS + 1)
        runCurrent()
        geocoder.answer("Valencia", "Valencia", "Valencia de Alcántara")
        advanceUntilIdle()

        assertEquals(listOf("Valencia", "Valencia de Alcántara"), vm.shown())
        assertFalse(vm.uiState.value.isSearchingLocation)
    }

    @Test
    fun `typing quickly sends only the last query`() = runTest(dispatcher) {
        val geocoder = HeldGeocodeRepository()
        val vm = viewModel(geocoder)
        advanceUntilIdle()

        listOf("Val", "Vale", "Valen", "Valenc").forEach {
            vm.searchLocations(it)
            advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS / 3)
        }
        advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS + 1)
        runCurrent()

        assertEquals(listOf("Valenc"), geocoder.asked)
    }
}
