package com.desarrollodroide.adventurelog.core.data

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.DeleteLocationUseCase
import com.desarrollodroide.adventurelog.core.network.ktor.HttpException
import com.desarrollodroide.adventurelog.core.testing.AdventureLogNetworkStub
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A 403 is the server refusing one record, not the session (QA 09, MC-03).
 *
 * Measured with a collection shared from one account to another: the guest deleting the owner's
 * place got "Session expired. Please log in again." on a perfectly good session, because every
 * repository answered 401 and 403 alike.
 */
class RefusalIsNotASessionTest {

    private class Server(private val status: Int) : AdventureLogNetworkStub() {
        override suspend fun deleteAdventure(adventureId: String) {
            throw HttpException(status, "refused")
        }
        override suspend fun getAdventures(page: Int, pageSize: Int) = throw HttpException(status, "refused")
    }

    @Test
    fun `deleting a place that belongs to someone else is refused as that and not as a session`() = runTest {
        val repository = AdventuresRepositoryImpl(Server(403))

        assertEquals(Either.Left(ApiResponse.Forbidden), repository.deleteLocation("owner-place"))
        assertEquals(
            Either.Left("That place belongs to someone else, so it cannot be deleted from here."),
            DeleteLocationUseCase(repository)("owner-place")
        )
    }

    @Test
    fun `a session the server no longer accepts still says so`() = runTest {
        val repository = AdventuresRepositoryImpl(Server(401))

        assertEquals(Either.Left(ApiResponse.InvalidCredentials), repository.deleteLocation("any"))
        assertEquals(Either.Left("Session expired. Please log in again."), DeleteLocationUseCase(repository)("any"))
    }

    @Test
    fun theMapsListOfPlacesTellsARefusalFromAnEndedSession() = runTest {
        // Written as a separate "403 ->" line, this one was missed when 401 and 403 were first
        // told apart (found in the QA 06 round).
        assertEquals(Either.Left(ApiResponse.Forbidden), AdventuresRepositoryImpl(Server(403)).getAllLocations())
        assertEquals(Either.Left(ApiResponse.InvalidCredentials), AdventuresRepositoryImpl(Server(401)).getAllLocations())
    }

}
