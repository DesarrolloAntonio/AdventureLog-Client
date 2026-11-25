package com.desarrollodroide.adventurelog.feature.ui.util

/**
 * True when [url] points at the same origin as [serverUrl].
 *
 * Used to decide whether an image request may carry the session token. Media under `images/`,
 * `attachments/` and `activities/` is served behind an auth check and answers 403 without it, but
 * the same code path also loads Wikipedia thumbnails and URLs the user pasted - sending the token
 * there would hand a third party a working credential for the user's server.
 *
 * Scheme and host must match, so a look-alike host cannot attract the token. The port is left
 * out, as cookies leave it out: a server reached through its web frontend (:3445) still serves
 * its photos from the backend's address (:3447), and both are the same machine.
 */
fun isSameOrigin(url: String?, serverUrl: String?): Boolean {
    val target = originOf(url) ?: return false
    val server = originOf(serverUrl) ?: return false
    return target == server
}

/**
 * Normalises a URL to `scheme://host`, without the port and without anything after the host.
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

    // An IPv6 literal keeps its brackets; anything else ends at the port's colon.
    val host = if (authority.startsWith('[')) {
        authority.substringBefore(']', missingDelimiterValue = "").takeIf { it.isNotEmpty() }?.plus(']')
    } else {
        authority.substringBefore(':')
    }
    if (host.isNullOrEmpty()) return null
    return "$scheme://$host"
}
