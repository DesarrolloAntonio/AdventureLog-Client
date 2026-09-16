package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.RemoveLocationFromCollectionUseCase
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testLocation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** "Remove from collection" on a place card (QA 04, CO-10). */
class RemoveLocationFromCollectionUseCaseTest {

    private class Server(private val onServer: List<String>) : LocationsRepositoryStub() {
        var sent: List<String>? = null
        override suspend fun fetchLocation(objectId: String): Either<ApiResponse, Location> =
            Either.Right(testLocation("Machu Picchu", id = objectId).copy(collections = onServer))

        override suspend fun updateLocationCollections(locationId: String, collections: List<String>): Either<ApiResponse, Location> {
            sent = collections
            return Either.Right(testLocation("Machu Picchu", id = locationId).copy(collections = collections))
        }
    }

    @Test
    fun theOneCollectionGoesAndEveryOtherStaysIncludingOnesTheScreenNeverSaw() = runTest {
        // The server replaces the whole list. "c3" was added elsewhere after the screen loaded.
        val server = Server(onServer = listOf("c1", "c2", "c3"))

        RemoveLocationFromCollectionUseCase(server)("p1", "c1")

        assertEquals(listOf("c2", "c3"), server.sent)
    }
}
