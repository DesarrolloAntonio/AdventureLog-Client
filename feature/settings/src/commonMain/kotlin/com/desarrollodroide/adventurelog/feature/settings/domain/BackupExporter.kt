package com.desarrollodroide.adventurelog.feature.settings.domain

import com.desarrollodroide.adventurelog.feature.ui.util.AuthenticatedFileDownloader
import com.desarrollodroide.adventurelog.feature.ui.util.PlatformFiles

/** What happened when someone asked for a backup. */
sealed interface BackupResult {
    data object Handed : BackupResult
    data object CouldNotDownload : BackupResult
    data object NowhereToPutIt : BackupResult
}

/**
 * Fetching the account's backup and handing it to the platform.
 *
 * An interface because the view model had the downloader and the file handler injected directly,
 * and neither can be stood up in a test - which made the whole of Settings untestable for the
 * sake of one button.
 */
interface BackupExporter {
    suspend fun export(serverUrl: String, fileName: String): BackupResult
}

class RealBackupExporter(
    private val downloader: AuthenticatedFileDownloader,
    private val files: PlatformFiles
) : BackupExporter {
    override suspend fun export(serverUrl: String, fileName: String): BackupResult {
        val bytes = downloader.download("${serverUrl.trimEnd('/')}/api/backup/export/")
            ?: return BackupResult.CouldNotDownload
        return if (files.share(bytes, fileName)) BackupResult.Handed else BackupResult.NowhereToPutIt
    }
}
