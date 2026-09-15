package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationCollectionsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationUseCase
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.UltraSlimCollection
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.testing.CollectionsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testLocation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Which saves of a place may change the collections it belongs to. `updateLocation` is refused by the
 * stub unless overridden, so Manage collections rewriting the whole place fails here.
 */
class UpdateLocationCollectionsTest {

    private class Server : LocationsRepositoryStub() {
        var collectionsSent: List<String>? = listOf("not called")
        var wholePlaceSent = false
        override suspend fun updateLocationCollections(locationId: String, collections: List<String>): Either<ApiResponse, Location> {
            collectionsSent = collections
            return Either.Right(testLocation("QA_Place", id = locationId))
        }
        override suspend fun updateLocation(
            adventureId: String, name: String, description: String, category: Category?, rating: Double,
            link: String, location: String, latitude: String?, longitude: String?, isPublic: Boolean,
            tags: List<String>, collections: List<String>?, visits: List<VisitFormData>, price: Double?,
            priceCurrency: String?
        ): Either<ApiResponse, Location> {
            collectionsSent = collections
            wholePlaceSent = true
            return Either.Right(testLocation(name, id = adventureId))
        }
    }

    @Test
    fun `saving the edit form leaves the place's collections alone`() = runTest {
        val server = Server()

        // The arguments the edit screen passes (AddEditLocationViewModel.saveLocation).
        UpdateLocationUseCase(server)(
            locationId = "p1", name = "QA_Place", description = "", category = null, rating = 4.0, link = "",
            location = "", latitude = null, longitude = null, isPublic = false, tags = emptyList(),
            price = 25.0, priceCurrency = "EUR"
        )

        assertNull(server.collectionsSent)
    }

    @Test
    fun `manage collections sends the collections chosen and nothing else`() = runTest {
        val server = Server()

        UpdateLocationCollectionsUseCase(server, object : CollectionsRepositoryStub() {
            override suspend fun refreshCollections(): Either<ApiResponse, List<UltraSlimCollection>> = Either.Right(emptyList())
        })(locationId = "p1", collectionIds = listOf("c1"))

        assertEquals(listOf("c1"), server.collectionsSent)
        assertFalse(server.wholePlaceSent, "manage collections sent the whole place back")
    }
}
