package com.desarrollodroide.adventurelog.core.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The body /api/lodging/ expects.
 *
 * Every nullable field is omitted when null rather than sent empty: the server rejects "" for a
 * date and for a decimal, the same way it rejected "UTC" as a transportation timezone.
 */
@Serializable
data class LodgingRequest(
    @SerialName("name")
    val name: String,
    @SerialName("type")
    val type: String,
    // These carry "" when the user empties them. With a default of "" they were left out of the
    // body, and clearing a field left the old text on the server (measured).
    @SerialName("description")
    val description: String,
    @SerialName("check_in")
    val checkIn: String? = null,
    @SerialName("check_out")
    val checkOut: String? = null,
    @SerialName("timezone")
    val timezone: String? = null,
    @SerialName("reservation_number")
    val reservationNumber: String,
    @SerialName("price")
    val price: String? = null,
    // Sent with the price: given a price and no currency, the server rewrites the currency to the
    // account's default, so an 80 EUR stay silently became 80 USD on the next save (measured).
    @SerialName("price_currency")
    val priceCurrency: String? = null,
    @SerialName("link")
    val link: String,
    @SerialName("location")
    val location: String,
    @SerialName("is_public")
    val isPublic: Boolean,
    @SerialName("collection")
    val collection: String? = null
)
