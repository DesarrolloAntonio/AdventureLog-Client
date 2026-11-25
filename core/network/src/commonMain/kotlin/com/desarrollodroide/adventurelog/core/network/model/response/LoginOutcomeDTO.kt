package com.desarrollodroide.adventurelog.core.network.model.response

/** What the server said to a username and password. */
sealed interface LoginOutcomeDTO {
    data class SignedIn(val user: UserDetailsDTO) : LoginOutcomeDTO

    /**
     * The password was right and the account has two-step verification: the sign-in waits for a
     * code. The half-done session stays inside the network layer, never in a screen's state.
     */
    data object SecondFactorRequired : LoginOutcomeDTO
}
