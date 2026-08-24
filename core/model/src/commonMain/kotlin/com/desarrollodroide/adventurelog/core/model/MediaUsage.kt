package com.desarrollodroide.adventurelog.core.model

/**
 * How much of the server's media storage this account is using, as the Advanced settings section
 * of the web client reports it.
 */
data class MediaUsage(
    val totalBytes: Long,
    val imagesBytes: Long,
    val attachmentsBytes: Long,
    val profilePicsBytes: Long,
    val imagesFiles: Int,
    val attachmentsFiles: Int,
    val profilePicsFiles: Int,
    /** Null when the server sets no limit, which it reports as "Unlimited". */
    val limitBytes: Long?
) {
    val totalFiles: Int get() = imagesFiles + attachmentsFiles + profilePicsFiles
}

/**
 * Bytes as the web prints them: whole units below a thousand, one decimal above.
 */
fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble() / 1024
    var unit = 0
    while (value >= 1024 && unit < units.lastIndex) {
        value /= 1024
        unit++
    }
    val rounded = kotlin.math.round(value * 10) / 10
    val text = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    return "$text ${units[unit]}"
}
