package com.desarrollodroide.adventurelog.feature.detail.domain

import com.desarrollodroide.adventurelog.feature.ui.util.AuthenticatedFileDownloader
import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles

/** What happened when a file was handed to the platform. */
enum class Handoff { DONE, COULD_NOT_FETCH, NOTHING_TAKES_IT }

/**
 * Fetching a file from the user's own server and handing it to a viewer or a share sheet.
 *
 * An interface because the two things behind it - a Ktor-backed downloader and the platform's
 * file handling - cannot be stood up in a test, and having them in the view model's constructor
 * meant the detail screen's logic could not be exercised at all.
 */
interface FileHandoff {
    /** Downloads [url] and opens it. */
    suspend fun open(url: String, fileName: String): Handoff

    /** Offers bytes already in hand to the share sheet. */
    suspend fun share(bytes: ByteArray, fileName: String): Handoff
}

class RealFileHandoff(
    private val downloader: AuthenticatedFileDownloader,
    private val files: PlatformFiles
) : FileHandoff {
    override suspend fun open(url: String, fileName: String): Handoff {
        val bytes = downloader.download(url) ?: return Handoff.COULD_NOT_FETCH
        return if (files.open(bytes, fileName)) Handoff.DONE else Handoff.NOTHING_TAKES_IT
    }

    override suspend fun share(bytes: ByteArray, fileName: String): Handoff =
        if (files.share(bytes, fileName)) Handoff.DONE else Handoff.NOTHING_TAKES_IT
}
