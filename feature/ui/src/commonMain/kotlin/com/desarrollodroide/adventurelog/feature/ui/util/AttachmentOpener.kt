package com.desarrollodroide.adventurelog.feature.ui.util

import coil3.PlatformContext

/**
 * Hands a downloaded attachment to whatever the platform uses to view it.
 *
 * Attachments live under the server's protected media paths, so the file cannot simply be opened
 * by URL: a viewer or a browser has no session and gets a 403. The bytes are fetched with the
 * signed-in client first and written somewhere the viewer can reach.
 */
interface AttachmentOpener {

    /** @return false when nothing on the device could open this kind of file. */
    suspend fun open(bytes: ByteArray, fileName: String): Boolean
}

expect fun createAttachmentOpener(platformContext: PlatformContext): AttachmentOpener

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
