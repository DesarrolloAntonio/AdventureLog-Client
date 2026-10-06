package com.desarrollodroide.adventurelog.core.domain.usecase

import com.desarrollodroide.adventurelog.core.network.datasource.AdventureLogNetwork
import co.touchlab.kermit.Logger

private val logger = Logger.withTag("EndRejectedSessionsUseCase")

/**
 * Ends the session whenever the server turns it away, from whichever screen's request.
 *
 * Only the startup check used to do this. A session that expired or was signed out elsewhere while
 * the app stayed open left every screen to cope alone, and most could not tell: Places read the
 * server's empty answer to an anonymous caller as "No places yet", Collections printed a raw 400.
 * Clearing the session here is enough - the shell watches it and takes the user to Login.
 */
class EndRejectedSessionsUseCase(
    private val networkDataSource: AdventureLogNetwork,
    private val logoutUseCase: LogoutUseCase
) {

    /** Suspends for as long as the caller wants the session watched. */
    suspend operator fun invoke() {
        networkDataSource.sessionRejections.collect {
            logger.w { "The server rejected the session; signing out" }
            logoutUseCase()
        }
    }
}
