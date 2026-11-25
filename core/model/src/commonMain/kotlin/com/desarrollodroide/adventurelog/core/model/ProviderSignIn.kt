package com.desarrollodroide.adventurelog.core.model

/** A sign-in provider the server has set up (OIDC, GitHub…), as its sign-in page lists it. */
data class SignInProvider(
    /** What the server calls it in `/accounts/mobile/start/?provider=`: `github`, `oidc/<id>`. */
    val id: String,
    /** What the button says. */
    val name: String,
)

/** How a server lets the app sign in through a provider. */
data class ProviderSignInOptions(
    /**
     * Where the browser goes: the server's own public address. Not always the one typed in the
     * app: through the web frontend's proxy, `/accounts/` is not forwarded (measured: 404).
     */
    val browserBaseUrl: String,
    val providers: List<SignInProvider>,
)

/**
 * A provider sign-in the browser is busy with. [state] and [codeVerifier] stay in the app: the
 * server gets only the verifier's SHA-256, and the code it hands back is worthless without it.
 */
data class ProviderSignInAttempt(
    /** The server the app signs in to: where the code is traded and every later request goes. */
    val serverUrl: String,
    val state: String,
    val codeVerifier: String,
    val rememberSession: Boolean,
    /** The page the browser opens. */
    val browserUrl: String,
)
