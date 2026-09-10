package com.desarrollodroide.adventurelog.feature.settings

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.SharingRepository
import com.desarrollodroide.adventurelog.core.model.PublicUser
import com.desarrollodroide.adventurelog.feature.settings.viewmodel.UsersViewModel
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

class UsersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeSharing(
        private var users: Either<String, List<PublicUser>>
    ) : SharingRepository {
        var calls = 0
        override suspend fun getPublicUsers(): Either<String, List<PublicUser>> {
            calls++
            return users
        }
        override suspend fun share(collectionId: String, userUuid: String) = unused()
        override suspend fun unshare(collectionId: String, userUuid: String) = unused()
        override suspend fun revokeInvite(collectionId: String, userUuid: String) = unused()
        private fun unused(): Nothing = throw AssertionError("not part of this test")
    }

    private val people = listOf(
        PublicUser("2", "zoe", "Zoe", "Adams"),
        PublicUser("1", "ana", "Ana", "Beltrán"),
        PublicUser("3", "nobody")
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun peopleArriveSortedByTheNameYouSee() = runTest(dispatcher) {
        val vm = UsersViewModel(FakeSharing(Either.Right(people)))
        testScheduler.advanceUntilIdle()

        // Sorted on displayName lowercased, and displayName falls back to the username when
        // there is no real name - so "nobody" sorts among the real names, not after them.
        assertEquals(listOf("Ana Beltrán", "nobody", "Zoe Adams"), vm.uiState.value.users.map { it.displayName })
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun searchMatchesEitherTheNameOrTheUsername() = runTest(dispatcher) {
        val vm = UsersViewModel(FakeSharing(Either.Right(people)))
        testScheduler.advanceUntilIdle()

        vm.onQueryChange("belt")
        assertEquals(listOf("Ana Beltrán"), vm.uiState.value.filtered.map { it.displayName })

        vm.onQueryChange("zoe")
        assertEquals(listOf("Zoe Adams"), vm.uiState.value.filtered.map { it.displayName })

        vm.onQueryChange("")
        assertEquals(3, vm.uiState.value.filtered.size)
    }

    @Test
    fun aFailureIsShownAndCanBeRetried() = runTest(dispatcher) {
        val repo = FakeSharing(Either.Left("Network unavailable"))
        val vm = UsersViewModel(repo)
        testScheduler.advanceUntilIdle()

        assertEquals("Network unavailable", vm.uiState.value.error)
        assertTrue(vm.uiState.value.users.isEmpty())

        vm.load()
        testScheduler.advanceUntilIdle()
        assertEquals(2, repo.calls)
    }
}
