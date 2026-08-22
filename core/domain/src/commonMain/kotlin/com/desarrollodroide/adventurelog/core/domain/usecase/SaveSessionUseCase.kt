package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.domain.repository.UserRepository
import com.desarrollodroide.adventurelog.core.model.UserDetails

/**
 * Use case to establish the user session after a successful login.
 */
class SaveSessionUseCase(
    private val userRepository: UserRepository
) {

    /**
     * Makes the session active for this run, and optionally persists it across app restarts.
     *
     * The session must always be published - screens read the logged-in user from it to render
     * the greeting and to load stats. Only [persist] is tied to the "Remember me" checkbox.
     *
     * @param userDetails User details for the session
     * @param persist Whether to also write the session to disk for auto-login
     */
    suspend operator fun invoke(userDetails: UserDetails, persist: Boolean = true) {
        if (persist) {
            userRepository.saveUserSession(userDetails)
        } else {
            userRepository.setActiveSession(userDetails)
        }
    }
}
