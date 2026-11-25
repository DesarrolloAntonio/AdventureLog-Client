package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val status: Int,
    val data: LoginData,
    val meta: LoginMeta
)

@Serializable
data class LoginData(
    val user: LoginUserData,
    val methods: List<LoginMethod>
)

@Serializable
data class LoginUserData(
    val id: Int,
    val uuid: String? = null,
    val display: String,
    @SerialName("has_usable_password")
    val hasUsablePassword: Boolean? = null,
    /** The app client leaves it out when the account has none. */
    val email: String = "",
    val username: String,
    @SerialName("first_name")
    val firstName: String? = null,
    @SerialName("last_name")
    val lastName: String? = null,
    @SerialName("profile_pic")
    val profilePic: String? = null,
    @SerialName("public_profile")
    val publicProfile: Boolean? = null,
    @SerialName("measurement_system")
    val measurementSystem: String? = null,
    @SerialName("date_joined")
    val dateJoined: String? = null,
    @SerialName("is_staff")
    val isStaff: Boolean? = null,
    @SerialName("disable_password")
    val disablePassword: Boolean? = null
)

/**
 * One way the user proved who they are. A password records the username; a second factor records
 * its `type` (totp, recovery_codes) and no username, so nothing past [method] is guaranteed.
 */
@Serializable
data class LoginMethod(
    val method: String,
    val at: Double? = null,
    val username: String? = null,
    val type: String? = null,
)

@Serializable
data class LoginMeta(
    @SerialName("is_authenticated")
    val isAuthenticated: Boolean,
    /** Only the app client sends it; it is the Django session key. */
    @SerialName("session_token")
    val sessionToken: String? = null
)

/**
 * allauth's 401 to a login that is not finished: no user yet, the steps still open in
 * `data.flows`, and - for the app client - the half-done session in `meta.session_token`.
 */
@Serializable
data class PendingLoginResponse(
    val status: Int,
    val data: PendingLoginData = PendingLoginData(),
    val meta: LoginMeta? = null,
) {
    /** The account has two-step verification and the password step is done. */
    val needsSecondFactor: Boolean
        get() = data.flows.any { it.id == MFA_AUTHENTICATE_FLOW && it.isPending }
}

@Serializable
data class PendingLoginData(
    val flows: List<LoginFlow> = emptyList(),
)

@Serializable
data class LoginFlow(
    val id: String,
    @SerialName("is_pending")
    val isPending: Boolean = false,
)

/** A TOTP code or a recovery code: allauth takes both in the same field. */
@Serializable
data class SecondFactorRequest(
    val code: String,
)

const val MFA_AUTHENTICATE_FLOW = "mfa_authenticate"

/** The last step of a provider sign-in: the code the browser brought back, and its PKCE verifier. */
@Serializable
data class MobileTokenRequest(
    val code: String,
    @SerialName("code_verifier")
    val codeVerifier: String,
    @SerialName("redirect_uri")
    val redirectUri: String,
)

/** What `/api/auth/mobile/token/` answers: a session and nothing else, no user. */
@Serializable
data class MobileTokenResponse(
    val meta: LoginMeta,
)
