package com.desarrollodroide.adventurelog.core.model

/**
 * Turns a location name into something safe to write to disk and hand to another app.
 *
 * Only the characters a file system actually objects to are removed. Stripping everything
 * non-ASCII would be safe too, but it mangles the names this app is full of - "Riaño" should not
 * be shared as "Riao".
 */
fun String.toSafeFileName(fallback: String = "location", extension: String? = null): String {
    val cleaned = filter { it.code >= 0x20 && it !in ILLEGAL_FILE_CHARS }
        .trim()
        .trimEnd('.')
        .take(MAX_LENGTH)

    val base = cleaned.ifBlank { fallback }
    return if (extension == null) base else "$base.${extension.trimStart('.')}"
}

private const val MAX_LENGTH = 80
private val ILLEGAL_FILE_CHARS = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
