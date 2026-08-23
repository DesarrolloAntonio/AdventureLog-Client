package com.desarrollodroide.adventurelog.feature.ui.util

import coil3.PlatformContext

/**
 * Hands a downloaded file to whatever the platform uses to view or share it.
 *
 * Anything served from the user's own AdventureLog sits behind an auth check, so a file cannot
 * simply be handed over by URL: a viewer or a browser has no session and gets a 403. The bytes
 * are fetched with the signed-in client first and written somewhere the platform can reach.
 */
interface PlatformFiles {

    /** @return false when nothing on the device could open this kind of file. */
    suspend fun open(bytes: ByteArray, fileName: String): Boolean

    /** Offers the file to the platform's share sheet. @return false when sharing is unavailable. */
    suspend fun share(bytes: ByteArray, fileName: String): Boolean
}

expect fun createPlatformFiles(platformContext: PlatformContext): PlatformFiles

/**
 * Best-effort media type from a file extension, for the handful the app actually attaches.
 * Unknown types fall back to a generic binary, which still lets a chooser appear.
 */
internal fun mimeTypeFor(extension: String): String = when (extension.lowercase().trimStart('.')) {
    "pdf" -> "application/pdf"
    "png" -> "image/png"
    "jpg", "jpeg" -> "image/jpeg"
    "gif" -> "image/gif"
    "webp" -> "image/webp"
    "heic" -> "image/heic"
    "txt", "md" -> "text/plain"
    "csv" -> "text/csv"
    "json" -> "application/json"
    "gpx" -> "application/gpx+xml"
    "kml" -> "application/vnd.google-earth.kml+xml"
    "geojson" -> "application/geo+json"
    "zip" -> "application/zip"
    "doc" -> "application/msword"
    "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    else -> "application/octet-stream"
}
