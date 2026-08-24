package com.desarrollodroide.adventurelog.core.network.model.response

import com.desarrollodroide.adventurelog.core.model.EmailAddress
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * allauth headless wraps every payload in `{status, data}`.
 */
@Serializable
data class EmailAddressListDTO(
    @SerialName("status")
    val status: Int = 200,

    @SerialName("data")
    val data: List<EmailAddressDTO> = emptyList()
)

@Serializable
data class EmailAddressDTO(
    @SerialName("email")
    val email: String,

    @SerialName("verified")
    val verified: Boolean = false,

    @SerialName("primary")
    val primary: Boolean = false
)

fun EmailAddressDTO.toDomainModel(): EmailAddress = EmailAddress(
    email = email,
    verified = verified,
    primary = primary
)
