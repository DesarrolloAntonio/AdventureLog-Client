package com.desarrollodroide.adventurelog.feature.collections

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
        var result: Either<ApiResponse, Checklist> = Either.Right(
            Checklist(id = "k1", user = "u", name = "Packing", createdAt = "", updatedAt = "")
        )

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
        AddEditChecklistViewModel(SaveChecklistUseCase(repo)) to repo

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
    fun editingAnExistingListStartsFromIt() {
        val (vm, _) = viewModel()
        vm.prefill(
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
        )

        assertEquals("Packing", vm.state.value.name)
        assertEquals(2, vm.state.value.lines.size)
        assertEquals(1, vm.state.value.doneCount)
    }
}
