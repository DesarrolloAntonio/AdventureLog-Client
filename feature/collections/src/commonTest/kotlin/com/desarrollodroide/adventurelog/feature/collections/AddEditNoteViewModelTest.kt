package com.desarrollodroide.adventurelog.feature.collections

import com.desarrollodroide.adventurelog.core.model.Collection
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionItemUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCollectionDetailUseCase
import com.desarrollodroide.adventurelog.core.testing.CollectionsRepositoryStub
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.SaveNoteUseCase
import com.desarrollodroide.adventurelog.core.model.Note
import com.desarrollodroide.adventurelog.feature.collections.viewmodel.AddEditNoteViewModel
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AddEditNoteViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeRepo : CollectionsRepositoryStub() {
        var createdWithDate: String? = null
        var createdCount = 0
        var updatedId: String? = null
        var collection: Either<ApiResponse, Collection> = Either.Right(collectionWith())
        var result: Either<ApiResponse, Note> = Either.Right(
            Note(id = "n1", user = "u", name = "Tickets", createdAt = "", updatedAt = "")
        )

        override suspend fun getCollection(collectionId: String): Either<ApiResponse, Collection> = collection

        override suspend fun createNote(
            collectionId: String,
            name: String,
            content: String,
            date: String?,
            isPublic: Boolean
        ): Either<ApiResponse, Note> {
            createdCount++
            createdWithDate = date
            return result
        }

        override suspend fun updateNote(
            noteId: String,
            name: String,
            content: String,
            date: String?,
            isPublic: Boolean
        ): Either<ApiResponse, Note> {
            updatedId = noteId
            return result
        }
    }

    private fun viewModel(repo: FakeRepo) = AddEditNoteViewModel(
        SaveNoteUseCase(repo),
        GetCollectionItemUseCase(GetCollectionDetailUseCase(repo))
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun aNoteWithoutATitleCannotBeSaved() {
        val vm = viewModel(FakeRepo())
        assertFalse(vm.state.value.canSave)
        vm.onNameChange("Tickets")
        assertTrue(vm.state.value.canSave)
    }

    @Test
    fun anEmptyDateIsSentAsNothingAtAll() = runTest(dispatcher) {
        // The server rejects "" for a date field, the same way it rejected "UTC" as a
        // transportation timezone. Sending null is the difference between saving and a 400.
        val repo = FakeRepo()
        val vm = viewModel(repo)
        vm.onNameChange("Tickets")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertNull(repo.createdWithDate)
    }

    @Test
    fun aNoteThatCameFromTheServerIsUpdatedNotDuplicated() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = viewModel(repo)
        repo.collection = Either.Right(collectionWith(note("n7", "Tickets")))
        vm.load("c1", "n7")
        testScheduler.advanceUntilIdle()

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertEquals("n7", repo.updatedId)
        assertEquals(0, repo.createdCount)
    }

    @Test
    fun aRefusedSaveKeepsTheNoteAndSaysWhy() = runTest(dispatcher) {
        val repo = FakeRepo().apply { result = Either.Left(ApiResponse.IOException) }
        val vm = viewModel(repo)
        vm.onNameChange("Tickets")
        vm.onContentChange("Book the 6am slot")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertFalse(vm.state.value.saved)
        assertEquals("Network unavailable", vm.state.value.error)
        assertEquals("Book the 6am slot", vm.state.value.content)
    }

    @Test
    fun theFormStartsFromTheNoteTheServerHasNow() = runTest(dispatcher) {
        // The route carries a copy made when the collection was opened; saving it put that copy
        // back over a change made elsewhere since (measured).
        val repo = FakeRepo()
        repo.collection = Either.Right(collectionWith(note("n7", "Tickets", content = "changed on the web")))
        val vm = viewModel(repo)

        vm.load("c1", "n7")
        testScheduler.advanceUntilIdle()

        assertEquals("changed on the web", vm.state.value.content)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun aNoteThatCannotBeReadGivesNoFormToSave() = runTest(dispatcher) {
        val repo = FakeRepo().apply { collection = Either.Left(ApiResponse.IOException) }
        val vm = viewModel(repo)

        vm.load("c1", "n7")
        testScheduler.advanceUntilIdle()

        assertEquals("", vm.state.value.name)
        assertEquals("Network unavailable", vm.state.value.loadError)
    }
}

private fun note(id: String, name: String, content: String = "") =
    Note(id = id, user = "u", name = name, content = content, createdAt = "", updatedAt = "")

private fun collectionWith(vararg notes: Note) = Collection(
    id = "c1", description = "", userId = "u", name = "QA_Col", isPublic = false, locations = emptyList(),
    createdAt = "", startDate = null, endDate = null, transportations = emptyList(), notes = notes.toList(),
    updatedAt = "", checklists = emptyList(), isArchived = false, sharedWith = emptyList(), link = "",
    lodging = emptyList()
)
