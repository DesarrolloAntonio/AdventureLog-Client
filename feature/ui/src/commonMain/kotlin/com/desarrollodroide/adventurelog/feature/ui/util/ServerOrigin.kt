package com.desarrollodroide.adventurelog.feature.ui.util

/**
 * True when [url] points at the same origin as [serverUrl].
 *
 * Used to decide whether an image request may carry the session token. Media under `images/`,
 * `attachments/` and `activities/` is served behind an auth check and answers 403 without it, but
 * the same code path also loads Wikipedia thumbnails and URLs the user pasted - sending the token
 * there would hand a third party a working credential for the user's server.
 *
 * Scheme, host and port must all match, so a look-alike host cannot attract the token.
 */
fun isSameOrigin(url: String?, serverUrl: String?): Boolean {
    val target = originOf(url) ?: return false
    val server = originOf(serverUrl) ?: return false
    return target == server
}

/**
 * Normalises a URL to `scheme://host:port`, filling in the default port so that
 * `https://example.com` and `https://example.com:443` compare equal.
 */
private fun originOf(raw: String?): String? {
    if (raw.isNullOrBlank()) return null

    val schemeSeparator = raw.indexOf("://")
    if (schemeSeparator <= 0) return null

    val scheme = raw.substring(0, schemeSeparator).lowercase()
    if (scheme != "http" && scheme != "https") return null

    val authority = raw.substring(schemeSeparator + 3)
        .substringBefore('/')
        .substringBefore('?')
        .substringBefore('#')
        .lowercase()
    if (authority.isEmpty()) return null

    // Reject credentials in the authority - they would make two different hosts look equal.
    if (authority.contains('@')) return null

    val port = authority.substringAfterLast(':', missingDelimiterValue = "")
    return if (port.toIntOrNull() != null) {
        "$scheme://$authority"
    } else {
        "$scheme://$authority:${if (scheme == "https") 443 else 80}"
    }
}
