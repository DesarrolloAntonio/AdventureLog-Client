package com.desarrollodroide.adventurelog.core.model

/**
 * The currencies a price can be recorded in - the same set, in the same order, that the web
 * client offers, so a price entered on either side reads the same on the other.
 */
object Currencies {

    const val DEFAULT = "USD"

    val options: List<Pair<String, String>> = listOf(
        "USD" to "US Dollar",
        "EUR" to "Euro",
        "GBP" to "British Pound",
        "JPY" to "Japanese Yen",
        "AUD" to "Australian Dollar",
        "CAD" to "Canadian Dollar",
        "CHF" to "Swiss Franc",
        "CNY" to "Chinese Yuan",
        "HKD" to "Hong Kong Dollar",
        "SGD" to "Singapore Dollar",
        "SEK" to "Swedish Krona",
        "NOK" to "Norwegian Krone",
        "DKK" to "Danish Krone",
        "NZD" to "New Zealand Dollar",
        "INR" to "Indian Rupee",
        "MXN" to "Mexican Peso",
        "BRL" to "Brazilian Real",
        "ZAR" to "South African Rand",
        "AED" to "UAE Dirham",
        "TRY" to "Turkish Lira"
    )

    fun labelFor(code: String?): String =
        options.firstOrNull { it.first == code }?.second ?: code.orEmpty()

    /**
     * Money reads with two decimals or none - "12.5" looks like a measurement, not a price. A
     * whole amount keeps no decimals at all, which is how the server stores it.
     */
    fun formatAmount(amount: Double): String {
        if (amount % 1.0 == 0.0) return amount.toLong().toString()

        val cents = kotlin.math.round(amount * 100).toLong()
        val whole = cents / 100
        val fraction = (if (cents < 0) -cents else cents) % 100
        return "$whole.${fraction.toString().padStart(2, '0')}"
    }
}
