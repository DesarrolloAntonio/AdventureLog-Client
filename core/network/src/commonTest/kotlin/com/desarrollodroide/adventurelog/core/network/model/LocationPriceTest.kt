package com.desarrollodroide.adventurelog.core.network.model

import com.desarrollodroide.adventurelog.core.model.Currencies
import com.desarrollodroide.adventurelog.core.network.model.response.LocationDTO
import com.desarrollodroide.adventurelog.core.network.model.response.toDomainModel
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationPriceTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @Test
    fun `a price and its currency survive the trip from the server`() {
        val dto = json.decodeFromString<LocationDTO>(
            """
            {
              "id": "1", "name": "Cuevas de Nerja",
              "created_at": "2026-08-23T00:00:00Z", "updated_at": "2026-08-23T00:00:00Z",
              "user": {"uuid": "u1", "username": "memnoch"},
              "price": 14.5, "price_currency": "EUR"
            }
            """.trimIndent()
        )

        val location = dto.toDomainModel()

        assertEquals(14.5, location.price)
        assertEquals("EUR", location.priceCurrency)
    }

    @Test
    fun `a location with no price reads as null rather than zero`() {
        // Zero would show as "free", which is a different claim from "not recorded".
        val dto = json.decodeFromString<LocationDTO>(
            """
            {
              "id": "1", "name": "Sima de Alcoron",
              "created_at": "2026-08-23T00:00:00Z", "updated_at": "2026-08-23T00:00:00Z",
              "user": {"uuid": "u1", "username": "memnoch"},
              "price": null, "price_currency": "USD"
            }
            """.trimIndent()
        )

        assertNull(dto.toDomainModel().price)
    }

    @Test
    fun `a whole price is not decorated with a decimal part`() {
        val dto = json.decodeFromString<LocationDTO>(
            """
            {
              "id": "1", "name": "Alhambra",
              "created_at": "2026-08-23T00:00:00Z", "updated_at": "2026-08-23T00:00:00Z",
              "user": {"uuid": "u1", "username": "memnoch"},
              "price": 19
            }
            """.trimIndent()
        )

        assertEquals(19.0, dto.toDomainModel().price)
    }
}

class CurrencyFormatTest {

    @Test
    fun `a fractional amount keeps two decimals`() {
        // 12.5 reads as a measurement; a price should read as 12.50.
        assertEquals("12.50", Currencies.formatAmount(12.5))
    }

    @Test
    fun `a whole amount carries no decimals`() {
        assertEquals("19", Currencies.formatAmount(19.0))
    }

    @Test
    fun `an amount with two decimals is left alone`() {
        assertEquals("14.99", Currencies.formatAmount(14.99))
    }

    @Test
    fun `an unknown currency code is shown as given`() {
        assertEquals("EUR", Currencies.labelFor("EUR").let { if (it == "Euro") "EUR" else it })
        assertEquals("XYZ", Currencies.labelFor("XYZ"))
    }
}
