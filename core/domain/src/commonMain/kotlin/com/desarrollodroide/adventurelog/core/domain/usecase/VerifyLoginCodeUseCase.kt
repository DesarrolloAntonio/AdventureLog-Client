package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.common.Either
import com.desarrollodroide.adventurelog.core.domain.repository.LoginCodeError
import com.desarrollodroide.adventurelog.core.domain.repository.LoginRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails

/** Why a code did not sign the user in, said for the screen. */
data class LoginCodeFailure(
    val message: String,
    /** The waiting sign-in is gone: back to the password, not another code. */
    val signInAgain: Boolean = false,
)

/**
 * The second step of signing in to an account with two-step verification: a 6-digit code from
 * the authenticator app, or an 8-digit recovery code.
 */
class VerifyLoginCodeUseCase(
    private val loginRepository: LoginRepository
) {
    companion object {
        const val ENTER_A_CODE = "Enter the 6-digit code from your authenticator app, or an 8-digit recovery code."
        const val INCORRECT_CODE = "That code isn't right. Codes change every 30 seconds - try the current one."
        const val EXPIRED = "The sign-in timed out before the code arrived. Sign in again."
        const val TOO_MANY_ATTEMPTS = "Too many attempts. Wait a minute and try again."
        const val FAILED = "Couldn't check the code, try again later."

        /** What a code looks like once spaces are gone: the authenticator's 6 digits or a recovery code's 8. */
        fun isWellFormed(code: String): Boolean = code.length in setOf(6, 8) && code.all { it.isDigit() }
    }

    suspend operator fun invoke(url: String, code: String): Either<LoginCodeFailure, UserDetails> {
        val clean = code.filterNot { it.isWhitespace() }
        if (!isWellFormed(clean)) return Either.Left(LoginCodeFailure(ENTER_A_CODE))
        return when (val result = loginRepository.sendLoginCode(url, clean)) {
            is Either.Left -> Either.Left(
                when (result.value) {
                    LoginCodeError.IncorrectCode -> LoginCodeFailure(INCORRECT_CODE)
                    LoginCodeError.Expired -> LoginCodeFailure(EXPIRED, signInAgain = true)
                    LoginCodeError.TooManyAttempts -> LoginCodeFailure(TOO_MANY_ATTEMPTS)
                    LoginCodeError.CannotReachServer -> LoginCodeFailure(LoginUseCase.CANNOT_REACH_SERVER)
                    LoginCodeError.Failed -> LoginCodeFailure(FAILED)
                }
            )
            is Either.Right -> when (val checked = LoginUseCase.withSession(result.value)) {
                is Either.Left -> Either.Left(LoginCodeFailure(checked.value, signInAgain = true))
                is Either.Right -> checked
            }
        }
    }

    /** Leaves the code step: the half-done sign-in is dropped. */
    fun cancel() = loginRepository.cancelPendingLogin()
}
