package com.desarrollodroide.adventurelog.feature.login

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * Where the platform drops the address a provider sign-in came back to
 * (`adventurelog://auth/callback?…`), for the sign-in screen to pick up. One per process: the
 * address can arrive before that screen exists again, after Android has ended the app while the
 * browser was in front.
 */
class ProviderSignInRedirects {
    private val latest = MutableStateFlow<String?>(null)

    /** The address waiting to be handled, if any. */
    val arrivals: StateFlow<String?> = latest

    fun deliver(uri: String) {
        latest.value = uri
    }

    /**
     * The waiting address, and none from then on: its code is traded once at most, since a second
     * trade would end the session the first one made.
     */
    fun take(): String? = latest.getAndUpdate { null }
}
