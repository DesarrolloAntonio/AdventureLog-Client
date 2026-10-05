package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.domain.usecase.CreateTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetTransportationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchWikipediaImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateTransportationUseCase
import com.desarrollodroide.adventurelog.core.testing.CollectionsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.HeldGeocodeRepository
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.TransportationRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.WikipediaRepositoryStub
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditTransportationViewModel
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.LOCATION_SEARCH_DEBOUNCE_MS
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

/** QA RL-05: the from/to search showed the results of an older query that answered last. */
class TransportLocationSearchTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun before() = Dispatchers.setMain(dispatcher)
    @AfterTest fun after() = Dispatchers.resetMain()

    private fun viewModel(geocoder: HeldGeocodeRepository): AddEditTransportationViewModel {
        val transports = object : TransportationRepositoryStub() {}
        return AddEditTransportationViewModel(
            createTransportationUseCase = CreateTransportationUseCase(transports),
            updateTransportationUseCase = UpdateTransportationUseCase(transports),
            getTransportationUseCase = GetTransportationUseCase(transports),
            generateDescriptionUseCase = GenerateDescriptionUseCase(object : LocationsRepositoryStub() {}),
            searchLocationsUseCase = SearchLocationsUseCase(geocoder),
            searchWikipediaImageUseCase = SearchWikipediaImageUseCase(object : WikipediaRepositoryStub() {}),
            getCollectionDetailUseCase = GetCollectionDetailUseCase(object : CollectionsRepositoryStub() {})
        )
    }

    private fun AddEditTransportationViewModel.shown() = uiState.value.locationSearchResults.map { it.name }

    @Test
    fun `an older query that answers last does not replace the results of the newer one`() = runTest(dispatcher) {
        val geocoder = HeldGeocodeRepository()
        val vm = viewModel(geocoder)

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

        listOf("Val", "Vale", "Valen", "Valenc").forEach {
            vm.searchLocations(it)
            advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS / 3)
        }
        advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS + 1)
        runCurrent()

        assertEquals(listOf("Valenc"), geocoder.asked)
    }

    @Test
    fun `closing the search drops a request still in flight`() = runTest(dispatcher) {
        val geocoder = HeldGeocodeRepository()
        val vm = viewModel(geocoder)

        vm.searchLocations("Valencia")
        advanceTimeBy(LOCATION_SEARCH_DEBOUNCE_MS + 1)
        runCurrent()
        vm.clearLocationSearch()
        geocoder.answer("Valencia", "Valencia")
        advanceUntilIdle()

        assertEquals(emptyList(), vm.shown())
        assertFalse(vm.uiState.value.isSearchingLocation)
    }
}
