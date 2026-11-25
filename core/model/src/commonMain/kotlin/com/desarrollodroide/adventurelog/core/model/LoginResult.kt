package com.desarrollodroide.adventurelog.core.model

/** Where a sign-in stands once the server has checked the username and password. */
sealed interface LoginResult {
    data class SignedIn(val user: UserDetails) : LoginResult

    /** The account has two-step verification: the sign-in waits for a code from its authenticator. */
    data object CodeRequired : LoginResult
}
