package com.desarrollodroide.adventurelog.core.model

/**
 * One address on the account. The server allows several; exactly one is primary, and only a
 * verified address can carry multi-factor authentication.
 */
data class EmailAddress(
    val email: String,
    val verified: Boolean,
    val primary: Boolean
)
