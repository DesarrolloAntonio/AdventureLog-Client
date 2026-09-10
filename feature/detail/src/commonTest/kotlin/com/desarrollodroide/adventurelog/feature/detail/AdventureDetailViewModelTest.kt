package com.desarrollodroide.adventurelog.feature.detail

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.usecase.GetLocationUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.GetShareImageUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ObserveCollectionsUseCase
import com.desarrollodroide.adventurelog.core.model.Attachment
import com.desarrollodroide.adventurelog.core.model.Location
import com.desarrollodroide.adventurelog.core.model.preview.PreviewData
import com.desarrollodroide.adventurelog.feature.detail.domain.FileHandoff
import com.desarrollodroide.adventurelog.feature.detail.domain.Handoff
import com.desarrollodroide.adventurelog.feature.detail.viewmodel.AdventureDetailViewModel
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

/**
 * What the place detail says when a file will not open.
 *
 * These are only writable because the downloader and the platform file handler moved behind
 * FileHandoff - with them in the constructor the view model could not be built at all.
 */
class AdventureDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeHandoff(
        private val openResult: Handoff = Handoff.DONE,
        private val shareResult: Handoff = Handoff.DONE
    ) : FileHandoff {
        var openedUrl: String? = null
        var openedName: String? = null
        var sharedName: String? = null

        override suspend fun open(url: String, fileName: String): Handoff {
            openedUrl = url
            openedName = fileName
            return openResult
        }

        override suspend fun share(bytes: ByteArray, fileName: String): Handoff {
            sharedName = fileName
            return shareResult
        }
    }

    private class Repo(
        private val share: Either<ApiResponse, ByteArray> = Either.Right(ByteArray(4))
    ) : LocationsRepositoryStub() {
        override suspend fun getShareImage(locationId: String, aspect: String) = share
    }

    private val pdf = Attachment(
        id = "a1",
        file = "https://server.test/media/tickets.pdf",
        extension = "pdf",
        name = "Tickets",
        user = "u1"
    )

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(handoff: FileHandoff, repo: Repo = Repo()) = AdventureDetailViewModel(
        getLocationUseCase = GetLocationUseCase(repo),
        fileHandoff = handoff,
        getShareImageUseCase = GetShareImageUseCase(repo),
        observeCollectionsUseCase = ObserveCollectionsUseCase(CollectionsStub())
    )

    @Test
    fun anAttachmentIsFetchedFromItsOwnUrlAndNamedFromItsTitle() = runTest(dispatcher) {
        val handoff = FakeHandoff()
        val vm = viewModel(handoff)

        vm.openAttachment(pdf)
        testScheduler.advanceUntilIdle()

        assertEquals("https://server.test/media/tickets.pdf", handoff.openedUrl)
        // The name it was given, plus the extension - not the mangled filename from the URL.
        assertEquals("Tickets.pdf", handoff.openedName)
        assertNull(vm.attachmentMessage.value)
    }

    @Test
    fun anAttachmentThatWillNotDownloadSaysSo() = runTest(dispatcher) {
        val vm = viewModel(FakeHandoff(openResult = Handoff.COULD_NOT_FETCH))

        vm.openAttachment(pdf)
        testScheduler.advanceUntilIdle()

        assertEquals("Could not download this attachment.", vm.attachmentMessage.value)
    }

    @Test
    fun anAttachmentNothingCanOpenNamesTheKindOfFile() = runTest(dispatcher) {
        val vm = viewModel(FakeHandoff(openResult = Handoff.NOTHING_TAKES_IT))

        vm.openAttachment(pdf)
        testScheduler.advanceUntilIdle()

        assertEquals("Nothing on this device can open a .pdf file.", vm.attachmentMessage.value)
    }

    @Test
    fun theSpinnerClearsWhicheverWayItWent() = runTest(dispatcher) {
        val vm = viewModel(FakeHandoff(openResult = Handoff.COULD_NOT_FETCH))

        vm.openAttachment(pdf)
        testScheduler.advanceUntilIdle()

        assertNull(vm.openingAttachmentId.value)
    }

    @Test
    fun sharingAPlaceSendsAnImageNamedAfterIt() = runTest(dispatcher) {
        val handoff = FakeHandoff()
        val vm = viewModel(handoff)
        val place: Location = PreviewData.locations.first()

        vm.shareLocation(place)
        testScheduler.advanceUntilIdle()

        // A picture the server rendered, not a link: this server is on a private network, so a
        // URL would be no use to whoever receives it.
        assertEquals(true, handoff.sharedName?.endsWith(".png"))
        assertNull(vm.attachmentMessage.value)
    }

    @Test
    fun aShareTheServerRefusesSaysWhy() = runTest(dispatcher) {
        val vm = viewModel(FakeHandoff(), Repo(share = Either.Left(ApiResponse.IOException)))

        vm.shareLocation(PreviewData.locations.first())
        testScheduler.advanceUntilIdle()

        // "No internet connection." here, "Network unavailable" in the note and region use
        // cases: the same condition, worded three ways across the app.
        assertEquals("No internet connection.", vm.attachmentMessage.value)
    }
}
