package com.desarrollodroide.adventurelog.core.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /api/server-info/`: only 0.14.0 and later answer it. */
@Serializable
internal data class ServerInfoDTO(
    @SerialName("public_url")
    val publicUrl: String? = null,
    val capabilities: List<String> = emptyList(),
)

/** `GET /api/auth/config/`, the part the sign-in screen needs. */
@Serializable
internal data class AuthConfigDTO(
    @SerialName("social_providers")
    val socialProviders: List<SocialProviderDTO> = emptyList(),
)

@Serializable
internal data class SocialProviderDTO(
    val id: String,
    val provider: String = "",
    val name: String = "",
)

/** The providers a server offers the app, and where its browser pages are. */
data class SignInProvidersDTO(
    val browserBaseUrl: String,
    val providers: List<Provider>,
) {
    data class Provider(val id: String, val name: String)

    companion object {
        fun none(url: String) = SignInProvidersDTO(url, emptyList())
    }
}
