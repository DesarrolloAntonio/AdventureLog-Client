package com.desarrollodroide.adventurelog.core.network.ktor

import io.ktor.client.plugins.logging.Logger

/**
 * Values that must never reach the log, whatever the log level is.
 *
 * Ktor's [io.ktor.client.plugins.logging.LogLevel.BODY] prints request bodies verbatim, which for
 * `/auth/browser/v1/auth/login` means the user's plaintext password, and for every authenticated
 * call means the session cookie. Both used to land in logcat, readable by any app holding
 * `READ_LOGS` or by anyone with adb access to the device.
 */
private const val REDACTED = "\"***\""

// session_token: the app client's login answer carries the session in its body, and a login
// waiting for its second factor carries the half-done one.
private val jsonSecrets = listOf("password", "new_password", "current_password", "token", "session_token", "key")

private val jsonSecretRegexes = jsonSecrets.map { field ->
    Regex("(\"$field\"\\s*:\\s*)\"[^\"]*\"", RegexOption.IGNORE_CASE)
} + listOf(
    // A second-factor code. Only digits: allauth's error codes ("code": "incorrect_code") stay readable.
    Regex("(\"code\"\\s*:\\s*)\"\\d+\"", RegexOption.IGNORE_CASE),
    // A provider sign-in's one-time code (43 url-safe characters) and its PKCE verifier. An
    // error code is lowercase words ("username_password_mismatch"): one capital or digit is enough
    // to tell the code apart.
    Regex("(\"code\"\\s*:\\s*)\"(?=[A-Za-z0-9_-]*[A-Z0-9])[A-Za-z0-9_-]{20,}\""),
    Regex("(\"code_verifier\"\\s*:\\s*)\"[^\"]*\"", RegexOption.IGNORE_CASE),
)

private val cookieValueRegex =
    Regex("((?:sessionid|csrftoken)=)[^;,\\s]+", RegexOption.IGNORE_CASE)

private val headerValueRegex =
    Regex("((?:X-Session-Token|X-API-Key|Authorization|Set-Cookie|Cookie)\\s*[:=]\\s*)\\[?[^\\]\\n]+",
        RegexOption.IGNORE_CASE)

/**
 * Strips credentials and session material out of an HTTP log line.
 *
 * Redaction happens before the message is handed to the platform logger, so there is no window in
 * which the raw value exists in the log buffer.
 */
internal fun redactSecrets(message: String): String {
    var sanitized = message
    jsonSecretRegexes.forEach { regex ->
        sanitized = regex.replace(sanitized, "$1$REDACTED")
    }
    sanitized = cookieValueRegex.replace(sanitized, "$1***")
    sanitized = headerValueRegex.replace(sanitized, "$1***")
    return sanitized
}

/**
 * Logger that redacts secrets and chunks long messages so they survive logcat's per-line limit.
 */
internal class RedactingHttpLogger(
    private val sink: (String) -> Unit = ::println
) : Logger {
    override fun log(message: String) {
        val safe = redactSecrets(message)
        if (safe.length > CHUNK_SIZE) {
            safe.chunked(CHUNK_SIZE).forEach(sink)
        } else {
            sink("HTTP Log: $safe")
        }
    }

    private companion object {
        const val CHUNK_SIZE = 3000
    }
}
