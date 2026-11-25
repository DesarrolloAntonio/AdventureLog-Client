package com.desarrollodroide.adventurelog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.desarrollodroide.adventurelog.core.domain.usecase.ProviderSignInUseCase
import com.desarrollodroide.adventurelog.feature.login.ProviderSignInRedirects
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Receives `adventurelog://auth/callback?…`, where the server sends the browser once a provider
 * sign-in is done, and hands it to the sign-in screen.
 *
 * Its own activity rather than an intent filter on [MainActivity]: the browser's Custom Tab sits
 * on top of MainActivity in the app's task, and bringing MainActivity back with CLEAR_TOP closes
 * the tab without changing how MainActivity launches for everything else (as singleTask would).
 * The same pattern AppAuth uses.
 */
class ProviderSignInRedirectActivity : Activity(), KoinComponent {

    private val redirects: ProviderSignInRedirects by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent?.data?.toString()
        if (uri != null && ProviderSignInUseCase.callbackParameters(uri) != null) {
            redirects.deliver(uri)
        }
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finish()
    }
}
