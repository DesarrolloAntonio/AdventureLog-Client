package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.testing.CollectionsRepositoryStub
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveLodgingUseCase
import com.desarrollodroide.adventurelog.core.model.Lodging
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditLodgingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddEditLodgingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeRepo : CollectionsRepositoryStub() {
        var timezone: String? = "unset"
        var price: String? = "unset"
        var checkIn: String? = "unset"
        var result: Either<ApiResponse, Lodging> = Either.Right(
            Lodging(id = "l1", user = "u", name = "Hotel", createdAt = "", updatedAt = "")
        )

        override suspend fun createLodging(
            collectionId: String,
            name: String,
            type: String,
            description: String,
            checkIn: String?,
            checkOut: String?,
            timezone: String?,
            reservationNumber: String,
            price: String?,
            link: String,
            location: String,
            isPublic: Boolean
        ): Either<ApiResponse, Lodging> {
            this.timezone = timezone
            this.price = price
            this.checkIn = checkIn
            return result
        }
    }

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun theDefaultTypeIsAHotel() {
        val vm = AddEditLodgingViewModel(SaveLodgingUseCase(FakeRepo()))
        assertEquals("hotel", vm.state.value.type)
    }

    @Test
    fun withNoDatesNoTimezoneIsSent() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = AddEditLodgingViewModel(SaveLodgingUseCase(repo))
        vm.onNameChange("Hotel Monasterio")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        // Nothing to qualify, so nothing to send. Blank strings are what the server rejects.
        assertNull(repo.timezone)
        assertNull(repo.price)
        assertNull(repo.checkIn)
    }

    @Test
    fun withDatesTheDevicesZoneIsSentAndItIsNotUTC() = runTest(dispatcher) {
        // "UTC" is not among the server's timezone choices - that is what made the first
        // transportation POST fail with a 400, and lodging carries the same field.
        val repo = FakeRepo()
        val vm = AddEditLodgingViewModel(SaveLodgingUseCase(repo))
        vm.onNameChange("Hotel Monasterio")
        vm.onCheckInChange("2026-10-10")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertNotEquals(null, repo.timezone)
        assertNotEquals("UTC", repo.timezone)
        assertTrue(repo.timezone!!.contains("/"))
        assertEquals("2026-10-10", repo.checkIn)
    }

    @Test
    fun aRefusedSaveKeepsTheForm() = runTest(dispatcher) {
        val repo = FakeRepo().apply { result = Either.Left(ApiResponse.HttpError) }
        val vm = AddEditLodgingViewModel(SaveLodgingUseCase(repo))
        vm.onNameChange("Hotel Monasterio")
        vm.onLocationChange("Cusco, Peru")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertFalse(vm.state.value.saved)
        assertEquals("Cusco, Peru", vm.state.value.location)
    }
}
