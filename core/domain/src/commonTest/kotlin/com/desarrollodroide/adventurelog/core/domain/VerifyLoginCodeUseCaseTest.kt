package com.desarrollodroide.adventurelog.core.domain

import com.desarrollodroide.adventurelog.core.common.ApiResponse
import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LoginCodeError
import com.desarrollodroide.adventurelog.core.domain.repository.LoginRepository
import com.desarrollodroide.adventurelog.core.domain.repository.ProviderSignInError
import com.desarrollodroide.adventurelog.core.model.ProviderSignInOptions
import com.desarrollodroide.adventurelog.core.domain.usecase.LoginCodeFailure
import com.desarrollodroide.adventurelog.core.domain.usecase.VerifyLoginCodeUseCase
import com.desarrollodroide.adventurelog.core.model.LoginResult
import com.desarrollodroide.adventurelog.core.model.UserDetails
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VerifyLoginCodeUseCaseTest {

    private class FakeLoginRepository : LoginRepository {
        var codeResult: Either<LoginCodeError, UserDetails> = Either.Right(user())
        val codesSent = mutableListOf<String>()
        var cancelled = 0

        override suspend fun sendLogin(url: String, username: String, password: String): Either<ApiResponse, LoginResult> =
            error("not part of the code step")

        override suspend fun sendLoginCode(url: String, code: String): Either<LoginCodeError, UserDetails> {
            codesSent += code
            return codeResult
        }

        override fun cancelPendingLogin() {
            cancelled++
        }

        override suspend fun getSignInProviders(url: String): ProviderSignInOptions? = error("not part of this step")

        override suspend fun redeemProviderSignIn(
            url: String,
            code: String,
            codeVerifier: String,
            redirectUri: String,
        ): Either<ProviderSignInError, UserDetails> = error("not part of this step")
    }

    private val repository = FakeLoginRepository()
    private val useCase = VerifyLoginCodeUseCase(repository)

    @Test
    fun `the authenticator's code signs in`() = runTest {
        val result = useCase("https://qa.test", "123456")

        assertEquals(Either.Right(user()), result)
        assertEquals(listOf("123456"), repository.codesSent)
    }

    @Test
    fun `an 8-digit recovery code goes to the same place, without the spaces a copy brings`() = runTest {
        useCase("https://qa.test", " 1234 5678 ")

        assertEquals(listOf("12345678"), repository.codesSent)
    }

    @Test
    fun `a code of the wrong shape never reaches the server`() = runTest {
        for (code in listOf("", "12345", "1234567", "12a456", "123456789")) {
            val result = useCase("https://qa.test", code)
            assertEquals(Either.Left(LoginCodeFailure(VerifyLoginCodeUseCase.ENTER_A_CODE)), result, "for '$code'")
        }
        assertTrue(repository.codesSent.isEmpty())
    }

    @Test
    fun `a wrong code lets the user try another`() = runTest {
        repository.codeResult = Either.Left(LoginCodeError.IncorrectCode)

        val failure = (useCase("https://qa.test", "000000") as Either.Left).value

        assertEquals(VerifyLoginCodeUseCase.INCORRECT_CODE, failure.message)
        assertEquals(false, failure.signInAgain)
    }

    @Test
    fun `a sign-in that timed out sends the user back to the password`() = runTest {
        repository.codeResult = Either.Left(LoginCodeError.Expired)

        val failure = (useCase("https://qa.test", "123456") as Either.Left).value

        assertEquals(VerifyLoginCodeUseCase.EXPIRED, failure.message)
        assertTrue(failure.signInAgain)
    }

    @Test
    fun `throttling and an unreachable server keep the code screen`() = runTest {
        repository.codeResult = Either.Left(LoginCodeError.TooManyAttempts)
        assertEquals(false, (useCase("https://qa.test", "123456") as Either.Left).value.signInAgain)

        repository.codeResult = Either.Left(LoginCodeError.CannotReachServer)
        val unreachable = (useCase("https://qa.test", "123456") as Either.Left).value
        assertTrue(unreachable.message.startsWith("Can't reach the server"))
        assertEquals(false, unreachable.signInAgain)
    }

    @Test
    fun `a code accepted with no session is not a sign-in`() = runTest {
        repository.codeResult = Either.Right(user().copy(sessionToken = null))

        val failure = (useCase("https://qa.test", "123456") as Either.Left).value

        assertTrue(failure.message.contains("no session"))
        assertTrue(failure.signInAgain)
    }

    @Test
    fun `cancelling drops the waiting sign-in`() {
        useCase.cancel()
        assertEquals(1, repository.cancelled)
        assertNull(repository.codesSent.firstOrNull())
    }

    private companion object {
        fun user() = UserDetails(
            profilePic = null, uuid = "u", publicProfile = false, username = "claude", email = "",
            firstName = "John", lastName = "Doe", dateJoined = "2024-01-01", isStaff = false, hasPassword = true,
            sessionToken = "session-token", serverUrl = "https://qa.test",
        )
    }
}
