package com.desarrollodroide.adventurelog.feature.collections

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
        var result: Either<ApiResponse, Note> = Either.Right(
            Note(id = "n1", user = "u", name = "Tickets", createdAt = "", updatedAt = "")
        )

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

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun aNoteWithoutATitleCannotBeSaved() {
        val vm = AddEditNoteViewModel(SaveNoteUseCase(FakeRepo()))
        assertFalse(vm.state.value.canSave)
        vm.onNameChange("Tickets")
        assertTrue(vm.state.value.canSave)
    }

    @Test
    fun anEmptyDateIsSentAsNothingAtAll() = runTest(dispatcher) {
        // The server rejects "" for a date field, the same way it rejected "UTC" as a
        // transportation timezone. Sending null is the difference between saving and a 400.
        val repo = FakeRepo()
        val vm = AddEditNoteViewModel(SaveNoteUseCase(repo))
        vm.onNameChange("Tickets")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertNull(repo.createdWithDate)
    }

    @Test
    fun aNoteThatCameFromTheServerIsUpdatedNotDuplicated() = runTest(dispatcher) {
        val repo = FakeRepo()
        val vm = AddEditNoteViewModel(SaveNoteUseCase(repo))
        vm.prefill(Note(id = "n7", user = "u", name = "Tickets", createdAt = "", updatedAt = ""))

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertEquals("n7", repo.updatedId)
        assertEquals(0, repo.createdCount)
    }

    @Test
    fun aRefusedSaveKeepsTheNoteAndSaysWhy() = runTest(dispatcher) {
        val repo = FakeRepo().apply { result = Either.Left(ApiResponse.IOException) }
        val vm = AddEditNoteViewModel(SaveNoteUseCase(repo))
        vm.onNameChange("Tickets")
        vm.onContentChange("Book the 6am slot")

        vm.save("c1")
        testScheduler.advanceUntilIdle()

        assertFalse(vm.state.value.saved)
        assertEquals("Network unavailable", vm.state.value.error)
        assertEquals("Book the 6am slot", vm.state.value.content)
    }
}
