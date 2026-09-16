package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCollectionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateCollectionUseCase
import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.testing.CollectionsRepositoryStub
import com.desarrollodroide.adventurelog.feature.collections.ui.screens.addEdit.data.CollectionFormData
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditCollectionViewModel
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The collection form's save (QA 04: CO-08 and CO-12). */
class AddEditCollectionViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private class Repo : CollectionsRepositoryStub() {
        val answer = CompletableDeferred<Either<ApiResponse, Collection>>()
        var sentLink: String? = "never sent"
        val reading = CompletableDeferred<Either<ApiResponse, Collection>>()

        override suspend fun createCollection(
            name: String, description: String, isPublic: Boolean, startDate: String?, endDate: String?, link: String?
        ): Either<ApiResponse, Collection> {
            sentLink = link
            return answer.await()
        }

        override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> = reading.await()
    }

    private fun viewModel(repo: Repo, id: String? = null) = AddEditCollectionViewModel(
        collectionId = id,
        createCollectionUseCase = CreateCollectionUseCase(repo),
        getCollectionDetailUseCase = GetCollectionDetailUseCase(repo),
        updateCollectionUseCase = UpdateCollectionUseCase(repo)
    )

    @Test
    fun aSaveInFlightOrRefusedNeverTakesTheFormAway() = runTest(dispatcher) {
        // The route swaps the form for a spinner while isLoading, and rebuilt it empty afterwards:
        // offline, the name typed was gone.
        val repo = Repo()
        val vm = viewModel(repo)

        vm.saveCollection(CollectionFormData(name = "Peru"))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.isSaving)
        assertFalse(vm.uiState.value.isLoading)

        repo.answer.complete(Either.Left(ApiResponse.IOException))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.isLoading)
        assertFalse(vm.uiState.value.isSaving)
        assertEquals("No internet connection. Please check your network.", vm.uiState.value.errorMessage)
    }

    @Test
    fun readingACollectionToEditStillShowsTheSpinner() = runTest(dispatcher) {
        val vm = viewModel(Repo(), id = "c1")
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isLoading)
    }

    @Test
    fun aLinkTypedWhileCreatingIsSent() = runTest(dispatcher) {
        val repo = Repo()
        val vm = viewModel(repo)

        vm.saveCollection(CollectionFormData(name = "Peru", link = "https://example.org/peru"))
        advanceUntilIdle()

        assertEquals("https://example.org/peru", repo.sentLink)
    }
}
