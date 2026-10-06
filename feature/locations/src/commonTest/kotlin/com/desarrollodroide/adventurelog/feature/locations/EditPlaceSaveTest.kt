package com.desarrollodroide.adventurelog.feature.locations

import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.locationFormOf
import com.desarrollodroide.adventurelog.core.testing.testCategory
import com.desarrollodroide.adventurelog.core.model.Visit
import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateCategoryUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.CreateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GenerateDescriptionUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetCategoriesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ReverseGeocodeUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchLocationsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SearchWikipediaImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationImagesUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationTrailsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.SyncLocationVisitsUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UpdateLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.UploadImageUseCase
import com.desarrollodroide.adventurelog.core.model.Category
import com.desarrollodroide.adventurelog.core.model.ContentImage
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.VisitFormData
import com.desarrollodroide.adventurelog.core.testing.CategoriesRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.FakeUserRepository
import com.desarrollodroide.adventurelog.core.testing.GeocodeRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.ImagesRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.LocationsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.TrailsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.VisitsRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.WikipediaRepositoryStub
import com.desarrollodroide.adventurelog.core.testing.testLocation
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.data.LocationFormData
import com.desarrollodroide.adventurelog.feature.locations.ui.screens.addEdit.formImageOf
import com.desarrollodroide.adventurelog.feature.locations.viewmodel.AddEditAdventureViewModel
import com.desarrollodroide.adventurelog.feature.ui.data.ImageFormData
import com.desarrollodroide.adventurelog.feature.ui.data.ImageType
import com.desarrollodroide.adventurelog.feature.ui.util.ImageBytesProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Editing a place: what the form starts from, and what saving it does to the photos. */
class EditPlaceSaveTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun before() = Dispatchers.setMain(dispatcher)
    @AfterTest fun after() = Dispatchers.resetMain()

    private fun photo(id: String, primary: Boolean = false) =
        ContentImage(id = id, image = "https://qa.test/media/images/$id.webp", isPrimary = primary, user = "u1")

    private class Server(
        var onServer: Either<ApiResponse, Location>,
        val listCopy: Location
    ) : LocationsRepositoryStub() {
        override suspend fun getLocation(objectId: String): Either<ApiResponse, Location> = Either.Right(listCopy)
        override suspend fun fetchLocation(objectId: String): Either<ApiResponse, Location> = onServer
        val updated = mutableListOf<String>()
        var created = 0
        var createdInto: List<String>? = null
        /** Holds a create on the wire, as the network does, until the test lets it through. */
        var createGate: CompletableDeferred<Unit>? = null
        override suspend fun updateLocation(
            adventureId: String, name: String, description: String, category: Category?, rating: Double,
            link: String, location: String, latitude: String?, longitude: String?, isPublic: Boolean,
            tags: List<String>, collections: List<String>?, visits: List<VisitFormData>, price: Double?,
            priceCurrency: String?
        ): Either<ApiResponse, Location> = Either.Right(testLocation(name, id = adventureId)).also { updated += adventureId }
        override suspend fun createLocation(
            name: String, description: String, category: Category, rating: Double, link: String,
            location: String, latitude: String?, longitude: String?, isPublic: Boolean,
            visits: List<VisitFormData>, price: Double?, priceCurrency: String?,
            activityTypes: List<String>, collectionIds: List<String>
        ): Either<ApiResponse, Location> {
            createGate?.await()
            createdInto = collectionIds
            return Either.Right(testLocation(name, id = "new${++created}"))
        }
    }

    private class Visits : VisitsRepositoryStub() {
        val updated = mutableListOf<String>()
        override suspend fun updateVisit(visitId: String, locationId: String, visit: VisitFormData) =
            Either.Right(Visit(id = visitId, location = locationId, createdAt = "", updatedAt = "")).also { updated += visitId }
    }

    private class Photos(private val failOnce: MutableSet<String> = mutableSetOf()) : ImagesRepositoryStub() {
        val uploaded = mutableListOf<String>()
        val deleted = mutableListOf<String>()
        val madePrimary = mutableListOf<String>()
        override suspend fun uploadImage(contentType: String, objectId: String, imageBytes: ByteArray, fileName: String) =
            if (failOnce.remove(fileName)) Either.Left(ApiResponse.HttpError) else Either.Right(Unit).also { uploaded += fileName }
        override suspend fun deleteImage(imageId: String) = Either.Right(Unit).also { deleted += imageId }
        override suspend fun setPrimaryImage(imageId: String) = Either.Right(Unit).also { madePrimary += imageId }
    }

    /** A private place's photos can't be downloaded without the session - as on the real server. */
    private object Device : ImageBytesProvider {
        override fun getImageBytes(uri: String): ByteArray = byteArrayOf(1, 2, 3)
        override fun getFileName(uri: String): String = uri.substringAfterLast('/')
        override suspend fun downloadImageFromUrl(url: String): ByteArray? = null
    }

    private fun viewModel(
        server: Server,
        photos: Photos = Photos(),
        adventureId: String? = "p1",
        visits: Visits = Visits()
    ) = AddEditAdventureViewModel(
        createLocationUseCase = CreateLocationUseCase(server),
        updateLocationUseCase = UpdateLocationUseCase(server),
        getLocationUseCase = GetLocationUseCase(server),
        getCategoriesUseCase = GetCategoriesUseCase(object : CategoriesRepositoryStub() {
            override suspend fun getCategories(): Either<ApiResponse, List<Category>> = Either.Right(emptyList())
        }),
        generateDescriptionUseCase = GenerateDescriptionUseCase(server),
        searchLocationsUseCase = SearchLocationsUseCase(object : GeocodeRepositoryStub() {}),
        reverseGeocodeUseCase = ReverseGeocodeUseCase(object : GeocodeRepositoryStub() {}),
        searchWikipediaImageUseCase = SearchWikipediaImageUseCase(object : WikipediaRepositoryStub() {}),
        createCategoryUseCase = CreateCategoryUseCase(object : CategoriesRepositoryStub() {}),
        uploadImageUseCase = UploadImageUseCase(photos),
        syncLocationVisitsUseCase = SyncLocationVisitsUseCase(visits),
        syncLocationTrailsUseCase = SyncLocationTrailsUseCase(object : TrailsRepositoryStub() {}),
        syncLocationImagesUseCase = SyncLocationImagesUseCase(photos),
        imageBytesProvider = Device,
        userRepository = FakeUserRepository(),
        adventureId = adventureId,
        existingLocation = server.listCopy.takeIf { adventureId != null }
    )

    private fun formWithPhotos(vararg images: ImageFormData) = LocationFormData(name = "QA_Place", images = images.toList())

    private fun onServer(image: ContentImage) = formImageOf(image)

    @Test
    fun `the edit form starts from the place as the server has it now`() = runTest(dispatcher) {
        val listCopy = testLocation("QA_Place", id = "p1").copy(description = "as the list loaded it")
        val server = Server(Either.Right(listCopy.copy(description = "changed on the web")), listCopy)

        val vm = viewModel(server)
        testScheduler.advanceUntilIdle()

        assertEquals("changed on the web", vm.uiState.value.existingLocation?.description)
    }

    @Test
    fun `a place that cannot be loaded gives no form to save`() = runTest(dispatcher) {
        val listCopy = testLocation("QA_Place", id = "p1").copy(description = "as the list loaded it")
        val vm = viewModel(Server(Either.Left(ApiResponse.IOException), listCopy))
        testScheduler.advanceUntilIdle()

        assertNull(vm.uiState.value.existingLocation, "the list's copy was offered for editing")
        assertEquals("Can't reach the server. Check your connection.", vm.uiState.value.loadError)
    }

    @Test
    fun `saving does not upload the photos already on the server`() = runTest(dispatcher) {
        val kept = photo("i1", primary = true)
        val place = testLocation("QA_Place", id = "p1", images = listOf(kept))
        val photos = Photos()
        val vm = viewModel(Server(Either.Right(place), place), photos)
        testScheduler.advanceUntilIdle()

        vm.saveLocation(formWithPhotos(onServer(kept)))
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList(), photos.uploaded)
        assertTrue(vm.uiState.value.isSaved, "save did not finish: ${vm.uiState.value.errorMessage}")
    }

    @Test
    fun `a photo added in the form is uploaded and one removed is deleted`() = runTest(dispatcher) {
        val kept = photo("i1", primary = true)
        val removed = photo("i2")
        val place = testLocation("QA_Place", id = "p1", images = listOf(kept, removed))
        val photos = Photos()
        val vm = viewModel(Server(Either.Right(place), place), photos)
        testScheduler.advanceUntilIdle()

        vm.saveLocation(formWithPhotos(onServer(kept), ImageFormData("content://picked/new.jpg", ImageType.LOCAL_FILE)))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("new.jpg"), photos.uploaded)
        assertEquals(listOf("i2"), photos.deleted)
    }

    @Test
    fun `choosing another photo as primary is sent`() = runTest(dispatcher) {
        val first = photo("i1", primary = true)
        val second = photo("i2")
        val place = testLocation("QA_Place", id = "p1", images = listOf(first, second))
        val photos = Photos()
        val vm = viewModel(Server(Either.Right(place), place), photos)
        testScheduler.advanceUntilIdle()

        vm.saveLocation(formWithPhotos(onServer(first).copy(isPrimary = false), onServer(second).copy(isPrimary = true)))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("i2"), photos.madePrimary)
        assertEquals(emptyList(), photos.deleted)
    }

    @Test
    fun `a second tap while a place is being created does not create another`() = runTest(dispatcher) {
        val server = Server(Either.Left(ApiResponse.HttpError), testLocation("unused"))
        server.createGate = CompletableDeferred()
        val vm = viewModel(server, adventureId = null)
        testScheduler.advanceUntilIdle()
        val form = LocationFormData(name = "QA_Place_New", category = testCategory("Nature"))

        vm.saveLocation(form)
        testScheduler.advanceUntilIdle()   // the first create is on its way
        vm.saveLocation(form)
        testScheduler.advanceUntilIdle()
        server.createGate?.complete(Unit)
        testScheduler.advanceUntilIdle()

        assertEquals(1, server.created)
    }

    @Test
    fun `Create again after a photo failed updates the place it made`() = runTest(dispatcher) {
        val server = Server(Either.Left(ApiResponse.HttpError), testLocation("unused"))
        val photos = Photos(failOnce = mutableSetOf("b.jpg"))
        val vm = viewModel(server, photos, adventureId = null)
        testScheduler.advanceUntilIdle()
        val form = formWithPhotos(
            ImageFormData("content://picked/a.jpg", ImageType.LOCAL_FILE),
            ImageFormData("content://picked/b.jpg", ImageType.LOCAL_FILE)
        ).copy(category = testCategory("Nature"))

        vm.saveLocation(form)
        testScheduler.advanceUntilIdle()
        vm.saveLocation(form)
        testScheduler.advanceUntilIdle()

        assertEquals(1, server.created, "a second place was created")
        assertEquals(listOf("new1"), server.updated)
        assertEquals(listOf("a.jpg", "b.jpg"), photos.uploaded)
        assertTrue(vm.uiState.value.isSaved)
    }

    private val loadedVisits = listOf(
        Visit(id = "v1", location = "p1", startDate = "2025-07-01T10:30:15Z", endDate = "2025-07-01T18:45:30Z",
            timezone = "America/New_York", notes = "QA timed NY", createdAt = "", updatedAt = ""),
        Visit(id = "v2", location = "p1", startDate = "2025-08-02T09:00:00Z", endDate = "2025-08-02T23:59:00Z",
            timezone = null, notes = "QA no tz", createdAt = "", updatedAt = "")
    )

    @Test
    fun `saving leaves the visits nobody touched alone`() = runTest(dispatcher) {
        val place = testLocation("QA_Place", id = "p1", visits = loadedVisits)
        val visits = Visits()
        val vm = viewModel(Server(Either.Right(place), place), visits = visits)
        testScheduler.advanceUntilIdle()

        vm.saveLocation(locationFormOf(place, defaultCurrency = "EUR"))
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList(), visits.updated)
    }

    @Test
    fun `saving sends the visit that was changed`() = runTest(dispatcher) {
        val place = testLocation("QA_Place", id = "p1", visits = loadedVisits)
        val visits = Visits()
        val vm = viewModel(Server(Either.Right(place), place), visits = visits)
        testScheduler.advanceUntilIdle()
        val form = locationFormOf(place, defaultCurrency = "EUR")

        vm.saveLocation(form.copy(visits = form.visits.map { if (it.id == "v2") it.copy(notes = "changed") else it }))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("v2"), visits.updated)
    }

    // QA HM-09: the trip card's "Add a place" made a place that belonged to no trip.

    @Test
    fun `a place started from a trip is created in that trip`() = runTest(dispatcher) {
        val server = Server(Either.Left(ApiResponse.HttpError), testLocation("unused"))
        val vm = viewModel(server, adventureId = null)
        testScheduler.advanceUntilIdle()

        vm.saveLocation(LocationFormData(name = "QA_Place_InTrip", category = testCategory("Nature")), intoCollectionId = "trip-peru")
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("trip-peru"), server.createdInto)
    }

    @Test
    fun `a place started from nowhere is created in no trip`() = runTest(dispatcher) {
        val server = Server(Either.Left(ApiResponse.HttpError), testLocation("unused"))
        val vm = viewModel(server, adventureId = null)
        testScheduler.advanceUntilIdle()

        vm.saveLocation(LocationFormData(name = "QA_Place_Loose", category = testCategory("Nature")))
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList(), server.createdInto)
    }

}
