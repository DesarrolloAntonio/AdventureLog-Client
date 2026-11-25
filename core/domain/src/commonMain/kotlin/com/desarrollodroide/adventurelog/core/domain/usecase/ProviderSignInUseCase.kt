package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.auth.Pkce
import com.desarrollodroide.adventurelog.core.domain.repository.LoginRepository
import com.desarrollodroide.adventurelog.core.domain.repository.ProviderSignInError
import com.desarrollodroide.adventurelog.core.model.ProviderSignInAttempt
import com.desarrollodroide.adventurelog.core.model.ProviderSignInOptions
import com.desarrollodroide.adventurelog.core.model.SignInProvider
import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * Signing in through one of the server's providers (OIDC, GitHub…), as upstream's API.md ("Mobile
 * app sign-in", 0.14.0) lays it out. The app never talks to the provider:
 *
 * 1. [start]: the system browser opens the server's `/accounts/mobile/start/` with a PKCE
 *    challenge, a random state and [REDIRECT_URI];
 * 2. the browser signs in the usual way and the server sends it to
 *    `adventurelog://auth/callback?code=…&state=…`, which the app receives;
 * 3. [finish]: the code and the verifier become a session at `/api/auth/mobile/token/`.
 */
class ProviderSignInUseCase(
    private val loginRepository: LoginRepository,
) {
    companion object {
        /**
         * Where the server sends the browser back. It has to be in the server's
         * `MOBILE_AUTH_REDIRECT_URIS`, whose default is exactly this.
         */
        const val REDIRECT_URI = "adventurelog://auth/callback"

        internal const val START_PATH = "/accounts/mobile/start/"

        const val CANCELLED = "Sign-in was cancelled in the browser."
        const val NOT_WAITING = "That sign-in is no longer waiting. Start it again."
        const val OTHER_SIGN_IN = "The browser answered for another sign-in. Start it again."
        const val CODE_REJECTED = "The sign-in took too long to come back, or was already used. Start it again."
        const val TOO_MANY_ATTEMPTS = "Too many attempts. Wait a minute and try again."
        const val FAILED = "Couldn't finish signing in, try again later."
        const val SERVER_REFUSED = "The server refused the sign-in: "

        /** The query of [uri] if it is the app's own redirect, else null. */
        fun callbackParameters(uri: String): Map<String, String>? {
            if (!uri.startsWith(REDIRECT_URI)) return null
            val rest = uri.removePrefix(REDIRECT_URI)
            if (rest.isNotEmpty() && !rest.startsWith("?")) return null
            return rest.removePrefix("?").substringBefore('#')
                .split('&')
                .filter { it.isNotEmpty() }
                .associate { pair ->
                    decode(pair.substringBefore('=')) to decode(pair.substringAfter('=', ""))
                }
        }

        private fun encode(value: String): String = buildString {
            for (byte in value.encodeToByteArray()) {
                val c = byte.toInt().toChar()
                if (c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c in "-._~") {
                    append(c)
                } else {
                    append('%')
                    append(HEX[(byte.toInt() shr 4) and 0xf])
                    append(HEX[byte.toInt() and 0xf])
                }
            }
        }

        /** Form decoding, as Django's urlencode writes it: `+` is a space. */
        private fun decode(value: String): String {
            val bytes = ArrayList<Byte>(value.length)
            var i = 0
            while (i < value.length) {
                val c = value[i]
                when {
                    c == '+' -> { bytes += ' '.code.toByte(); i++ }
                    c == '%' && i + 2 <= value.lastIndex -> {
                        val hex = value.substring(i + 1, i + 3).toIntOrNull(16)
                        if (hex != null) { bytes += hex.toByte(); i += 3 } else { bytes += c.code.toByte(); i++ }
                    }
                    else -> { bytes += c.toString().encodeToByteArray().toList(); i++ }
                }
            }
            return bytes.toByteArray().decodeToString()
        }

        private const val HEX = "0123456789ABCDEF"
    }

    /** The providers the server at [url] offers, or null when it offers none. */
    suspend fun providers(url: String): ProviderSignInOptions? = loginRepository.getSignInProviders(url)

    /** A new sign-in through [provider]: what the browser opens, and what the app keeps for [finish]. */
    fun start(
        serverUrl: String,
        options: ProviderSignInOptions,
        provider: SignInProvider,
        rememberSession: Boolean,
    ): ProviderSignInAttempt {
        val verifier = Pkce.newVerifier()
        val state = Pkce.newState()
        val query = listOf(
            "provider" to provider.id,
            "code_challenge" to Pkce.challengeOf(verifier),
            "code_challenge_method" to "S256",
            "state" to state,
            "redirect_uri" to REDIRECT_URI,
        ).joinToString("&") { (k, v) -> "$k=${encode(v)}" }
        return ProviderSignInAttempt(
            serverUrl = serverUrl.trimEnd('/'),
            state = state,
            codeVerifier = verifier,
            rememberSession = rememberSession,
            browserUrl = "${options.browserBaseUrl.trimEnd('/')}$START_PATH?$query",
        )
    }

    /**
     * Finishes [attempt] with the address the browser came back to. The code is traded at most
     * once, and only for the attempt whose state it carries: a second trade of the same code
     * would end the session the first one made.
     */
    suspend fun finish(attempt: ProviderSignInAttempt?, callbackUri: String): Either<String, UserDetails> {
        val params = callbackParameters(callbackUri) ?: return Either.Left(FAILED)
        if (attempt == null) return Either.Left(NOT_WAITING)
        if (params["state"] != attempt.state) return Either.Left(OTHER_SIGN_IN)
        params["error"]?.let { error ->
            return Either.Left(
                when {
                    error == "access_denied" -> CANCELLED
                    !params["error_description"].isNullOrBlank() -> SERVER_REFUSED + params.getValue("error_description")
                    else -> FAILED
                }
            )
        }
        val code = params["code"]?.takeIf { it.isNotBlank() } ?: return Either.Left(FAILED)
        return when (val result = loginRepository.redeemProviderSignIn(attempt.serverUrl, code, attempt.codeVerifier, REDIRECT_URI)) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    ProviderSignInError.CodeRejected -> CODE_REJECTED
                    ProviderSignInError.TooManyAttempts -> TOO_MANY_ATTEMPTS
                    ProviderSignInError.CannotReachServer -> LoginUseCase.CANNOT_REACH_SERVER
                    ProviderSignInError.Failed -> FAILED
                }
            )
            is Either.Right -> LoginUseCase.withSession(result.value)
        }
    }
}
