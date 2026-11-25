package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.auth.Pkce
import com.desarrollodroide.adventurelog.core.domain.auth.Sha256
import com.desarrollodroide.adventurelog.core.domain.repository.ProviderSignInError
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginUseCase
import com.desarrollodroide.adventurelog.core.domain.usecase.ProviderSignInUseCase
import com.desarrollodroide.adventurelog.core.model.ProviderSignInAttempt
import com.desarrollodroide.adventurelog.core.model.ProviderSignInOptions
import com.desarrollodroide.adventurelog.core.model.SignInProvider
import com.desarrollodroide.adventurelog.core.model.UserDetails
import com.desarrollodroide.adventurelog.core.testing.LoginRepositoryStub
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The app's half of upstream's "Mobile app sign-in" (0.14.0): PKCE as `users/services/mobile_auth.py`
 * checks it (unpadded base64url SHA-256, 43 characters), the start address, and what the browser
 * brings back.
 */
class ProviderSignInUseCaseTest {

    private class Server : LoginRepositoryStub() {
        val trades = mutableListOf<List<String>>()
        var answer: Either<ProviderSignInError, UserDetails> = Either.Right(user("sso-token"))

        override suspend fun redeemProviderSignIn(
            url: String,
            code: String,
            codeVerifier: String,
            redirectUri: String,
        ): Either<ProviderSignInError, UserDetails> {
            trades += listOf(url, code, codeVerifier, redirectUri)
            return answer
        }
    }

    private val server = Server()
    private val useCase = ProviderSignInUseCase(server)
    private val options = ProviderSignInOptions("https://public.test", listOf(GITHUB, OIDC))

    @Test
    fun `sha-256 matches the standard's test vectors`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            Sha256.digest(ByteArray(0)).toHex(),
        )
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Sha256.digest("abc".encodeToByteArray()).toHex(),
        )
        // Two blocks: the padding spills over.
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            Sha256.digest("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray()).toHex(),
        )
    }

    @Test
    fun `the challenge is RFC 7636's S256 of the verifier`() {
        // RFC 7636, appendix B.
        assertEquals(
            "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
            Pkce.challengeOf("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"),
        )
    }

    @Test
    fun `verifiers are long, url-safe and never repeat`() {
        val verifiers = List(50) { Pkce.newVerifier() }
        verifiers.forEach { v ->
            assertEquals(64, v.length)
            assertTrue(v.all { it.isLetterOrDigit() || it == '-' || it == '_' }, v)
            // What the server's CHALLENGE_PATTERN accepts.
            assertTrue(Regex("^[A-Za-z0-9_-]{43}$").matches(Pkce.challengeOf(v)))
        }
        assertEquals(50, verifiers.toSet().size)
        assertNotEquals(Pkce.newState(), Pkce.newState())
    }

    @Test
    fun `start opens the server's public address with the challenge, state and redirect`() {
        val attempt = useCase.start("https://typed.test/", options, OIDC, rememberSession = true)

        assertEquals("https://typed.test", attempt.serverUrl)
        assertTrue(attempt.rememberSession)
        assertTrue(attempt.browserUrl.startsWith("https://public.test/accounts/mobile/start/?"), attempt.browserUrl)
        val query = attempt.browserUrl.substringAfter('?').split('&').associate { it.substringBefore('=') to it.substringAfter('=') }
        assertEquals("oidc%2Fauthelia", query["provider"])
        assertEquals("S256", query["code_challenge_method"])
        assertEquals(Pkce.challengeOf(attempt.codeVerifier), query["code_challenge"])
        assertEquals(attempt.state, query["state"])
        assertEquals("adventurelog%3A%2F%2Fauth%2Fcallback", query["redirect_uri"])
    }

    @Test
    fun `the code goes to the typed server with the verifier and the same redirect`() = runTest {
        val attempt = attempt()

        val result = useCase.finish(attempt, "adventurelog://auth/callback?code=abc-123&state=${attempt.state}")

        assertEquals("sso-token", (result as Either.Right).value.sessionToken)
        assertEquals(listOf(listOf("https://typed.test", "abc-123", "the-verifier", "adventurelog://auth/callback")), server.trades)
    }

    @Test
    fun `an answer for another sign-in is refused without trading its code`() = runTest {
        val result = useCase.finish(attempt(), "adventurelog://auth/callback?code=abc&state=someone-elses")

        assertEquals(Either.Left(ProviderSignInUseCase.OTHER_SIGN_IN), result)
        assertTrue(server.trades.isEmpty())
    }

    @Test
    fun `an answer with no sign-in waiting says to start again`() = runTest {
        val result = useCase.finish(null, "adventurelog://auth/callback?code=abc&state=s")

        assertEquals(Either.Left(ProviderSignInUseCase.NOT_WAITING), result)
        assertTrue(server.trades.isEmpty())
    }

    @Test
    fun `the server's errors are said, decoded`() = runTest {
        val attempt = attempt()
        assertEquals(
            Either.Left(ProviderSignInUseCase.CANCELLED),
            useCase.finish(attempt, "adventurelog://auth/callback?error=access_denied&state=${attempt.state}"),
        )
        assertEquals(
            Either.Left(ProviderSignInUseCase.SERVER_REFUSED + "This sign-in provider is not configured on the server."),
            useCase.finish(
                attempt,
                "adventurelog://auth/callback?error=invalid_request&error_description=This+sign-in+provider+is+not+configured+on+the+server.&state=${attempt.state}",
            ),
        )
        assertTrue(server.trades.isEmpty())
    }

    @Test
    fun `a rejected code and a session-less answer are failures`() = runTest {
        server.answer = Either.Left(ProviderSignInError.CodeRejected)
        assertEquals(Either.Left(ProviderSignInUseCase.CODE_REJECTED), useCase.finish(attempt(), callback()))

        server.answer = Either.Left(ProviderSignInError.CannotReachServer)
        assertEquals(Either.Left(LoginUseCase.CANNOT_REACH_SERVER), useCase.finish(attempt(), callback()))

        server.answer = Either.Right(user(token = null))
        assertEquals(Either.Left(LoginUseCase.NO_SESSION), useCase.finish(attempt(), callback()))
    }

    @Test
    fun `only the app's own redirect is read`() {
        assertNull(ProviderSignInUseCase.callbackParameters("https://evil.test/?code=a&state=b"))
        assertNull(ProviderSignInUseCase.callbackParameters("adventurelog://auth/callbackextra?code=a"))
        assertEquals(
            mapOf("code" to "a b", "state" to "ñ"),
            ProviderSignInUseCase.callbackParameters("adventurelog://auth/callback?code=a+b&state=%C3%B1"),
        )
    }

    private fun attempt() = ProviderSignInAttempt(
        serverUrl = "https://typed.test",
        state = "the-state",
        codeVerifier = "the-verifier",
        rememberSession = false,
        browserUrl = "https://public.test/accounts/mobile/start/",
    )

    private fun callback() = "adventurelog://auth/callback?code=c0de&state=the-state"

    private fun ByteArray.toHex() = joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

    private companion object {
        val GITHUB = SignInProvider("github", "GitHub")
        val OIDC = SignInProvider("oidc/authelia", "Authelia")

        fun user(token: String?) = UserDetails(
            profilePic = null, uuid = "u", publicProfile = false, username = "sso-user", email = "",
            firstName = "John", lastName = "Doe", dateJoined = "2024-01-01", isStaff = false, hasPassword = true,
            sessionToken = token, serverUrl = "https://typed.test",
        )
    }
}
