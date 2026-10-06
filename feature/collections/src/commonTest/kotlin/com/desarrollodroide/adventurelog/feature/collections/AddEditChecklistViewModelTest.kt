package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionItemUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.testing.CollectionsRepositoryStub
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.CollectionsRepository
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveChecklistUseCase
import com.desarrollodroide.adventurelog.core.model.Checklist
import com.desarrollodroide.adventurelog.core.model.ChecklistItem
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditChecklistViewModel
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
import kotlin.test.assertTrue

/**
 * The checklist form's own logic - lines added, ticked and removed before anything is saved.
 *
 * The server takes the items inside the checklist body, so what this holds locally is exactly
 * what gets sent. Worth testing on its own: it needs no device and runs in milliseconds.
 */
class AddEditChecklistViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeRepo : CollectionsRepositoryStub() {
        var savedItems: List<Pair<String, Boolean>>? = null
        var savedName: String? = null
        var collection: Either<ApiResponse, Collection> = Either.Right(collectionWithChecklist())
        var result: Either<ApiResponse, Checklist> = Either.Right(
            Checklist(id = "k1", user = "u", name = "Packing", createdAt = "", updatedAt = "")
        )

        override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> = collection

        var updates = 0
        override suspend fun updateChecklist(
            checklistId: String,
            name: String,
            items: List<Pair<String, Boolean>>,
            date: String?,
            isPublic: Boolean
        ): Either<ApiResponse, Checklist> {
            updates++
            return result
        }

        override suspend fun createChecklist(
            collectionId: String,
            name: String,
            items: List<Pair<String, Boolean>>,
            date: String?,
            isPublic: Boolean
        ): Either<ApiResponse, Checklist> {
            savedName = name
            savedItems = items
            return result
        }
    }

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeRepo = FakeRepo()) =
        AddEditChecklistViewModel(SaveChecklistUseCase(repo), GetCollectionItemUseCase(GetCollectionDetailUseCase(repo))) to repo

    @Test
    fun aListWithoutATitleCannotBeSaved() {
        val (vm, _) = viewModel()
        assertFalse(vm.state.value.canSave)
        vm.onNameChange("Packing")
        assertTrue(vm.state.value.canSave)
    }

    @Test
    fun addingALineClearsTheDraft() {
        val (vm, _) = viewModel()
        vm.onDraftChange("Hiking boots")
        vm.addLine()

        assertEquals(1, vm.state.value.lines.size)
        assertEquals("Hiking boots", vm.state.value.lines.first().name)
        assertEquals("", vm.state.value.draft)
    }

    @Test
    fun anEmptyDraftAddsNothing() {
        val (vm, _) = viewModel()
        vm.onDraftChange("   ")
        vm.addLine()
        assertTrue(vm.state.value.lines.isEmpty())
    }

    @Test
    fun tickingALineCountsIt() {
        val (vm, _) = viewModel()
        vm.onDraftChange("Boots"); vm.addLine()
        vm.onDraftChange("Tablets"); vm.addLine()

        assertEquals(0, vm.state.value.doneCount)
        vm.toggleLine(0)
        assertEquals(1, vm.state.value.doneCount)
        vm.toggleLine(0)
        assertEquals(0, vm.state.value.doneCount)
    }

    @Test
    fun removingALineLeavesTheRest() {
        val (vm, _) = viewModel()
        vm.onDraftChange("Boots"); vm.addLine()
        vm.onDraftChange("Tablets"); vm.addLine()
        vm.removeLine(0)

        assertEquals(listOf("Tablets"), vm.state.value.lines.map { it.name })
    }

    @Test
    fun savingSendsEveryLineWithItsState() = runTest(dispatcher) {
        val (vm, repo) = viewModel()
        vm.onNameChange("Packing")
        vm.onDraftChange("Boots"); vm.addLine()
        vm.onDraftChange("Tablets"); vm.addLine()
        vm.toggleLine(0)

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertEquals("Packing", repo.savedName)
        assertEquals(listOf("Boots" to true, "Tablets" to false), repo.savedItems)
        assertTrue(vm.state.value.saved)
    }

    @Test
    fun aRefusedSaveKeepsTheFormAndSaysWhy() = runTest(dispatcher) {
        val repo = FakeRepo().apply { result = Either.Left(ApiResponse.HttpError) }
        val (vm, _) = viewModel(repo)
        vm.onNameChange("Packing")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertFalse(vm.state.value.saved)
        assertEquals("Could not save that, try again later", vm.state.value.error)
    }

    @Test
    fun editingAnExistingListStartsFromWhatTheServerHas() = runTest(dispatcher) {
        val (vm, repo) = viewModel()
        repo.collection = Either.Right(collectionWithChecklist(
            Checklist(
                id = "k1",
                user = "u",
                name = "Packing",
                createdAt = "",
                updatedAt = "",
                items = listOf(
                    ChecklistItem("i1", "u", "Boots", true, "k1", "", ""),
                    ChecklistItem("i2", "u", "Tablets", false, "k1", "", "")
                )
            )
        ))
        vm.load("c1", "k1")
        testScheduler.advanceUntilIdle()

        assertEquals("Packing", vm.state.value.name)
        assertEquals(2, vm.state.value.lines.size)
        assertEquals(1, vm.state.value.doneCount)
    }

    @Test
    fun aListThatCannotBeReadGivesNoFormToSave() = runTest(dispatcher) {
        val (vm, repo) = viewModel()
        repo.collection = Either.Left(ApiResponse.IOException)

        vm.load("c1", "k1")
        testScheduler.advanceUntilIdle()

        assertEquals("", vm.state.value.name)
        assertEquals("Can't reach the server. Check your connection.", vm.state.value.loadError)
    }

    private fun packing() = collectionWithChecklist(
        Checklist(
            id = "k1", user = "u", name = "Packing", createdAt = "", updatedAt = "",
            items = listOf(ChecklistItem("i1", "u", "Boots", true, "k1", "", ""))
        )
    )

    @Test
    fun savingAChecklistThatWasNotChangedSendsNothing() = runTest(dispatcher) {
        // QA 04, CO-04: every save made the server delete and recreate each item.
        val (vm, repo) = viewModel()
        repo.collection = Either.Right(packing())
        vm.load("c1", "k1")
        testScheduler.advanceUntilIdle()

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertTrue(vm.state.value.saved)
        assertEquals(0, repo.updates)
    }

    @Test
    fun untickingOneLineIsStillSaved() = runTest(dispatcher) {
        val (vm, repo) = viewModel()
        repo.collection = Either.Right(packing())
        vm.load("c1", "k1")
        testScheduler.advanceUntilIdle()

        vm.toggleLine(0)
        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertTrue(vm.state.value.saved)
        assertEquals(1, repo.updates)
    }

    // QA RL-09: Cancel threw a filled-in list away without asking. These decide whether it asks.

    private suspend fun kotlinx.coroutines.test.TestScope.loadedPacking(): AddEditChecklistViewModel {
        val (vm, repo) = viewModel()
        repo.collection = Either.Right(collectionWithChecklist(
            Checklist(
                id = "k1", user = "u", name = "Packing", createdAt = "", updatedAt = "",
                items = listOf(ChecklistItem("i1", "u", "Boots", false, "k1", "", ""))
            )
        ))
        vm.load("c1", "k1")
        testScheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun aNewListWithSomethingTypedHasChangesToLose() {
        val (vm, _) = viewModel()
        vm.onNameChange("Packing")
        assertTrue(vm.hasChanges())
    }

    @Test
    fun anItemTypedButNotAddedYetCountsAsAChange() {
        val (vm, _) = viewModel()
        vm.onDraftChange("Passport")
        assertTrue(vm.hasChanges())
    }

    @Test
    fun tickingAnItemOfALoadedListIsAChange() = runTest(dispatcher) {
        val vm = loadedPacking()
        vm.toggleLine(0)
        assertTrue(vm.hasChanges())
    }

    @Test
    fun anUntouchedFormHasNothingToLose() = runTest(dispatcher) {
        assertFalse(viewModel().first.hasChanges())
        assertFalse(loadedPacking().hasChanges())
    }

}

private fun collectionWithChecklist(vararg lists: Checklist) = Collection(
    id = "c1", description = "", userId = "u", name = "QA_Col", isPublic = false, locations = emptyList(),
    createdAt = "", startDate = null, endDate = null, transportations = emptyList(), notes = emptyList(),
    updatedAt = "", checklists = lists.toList(), isArchived = false, sharedWith = emptyList(), link = "",
    lodging = emptyList()
)
