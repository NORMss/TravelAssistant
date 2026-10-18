package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Деньги в минорных единицах (копейки, фыни, центы) — чтобы не ловить ошибки double.
 * @param currency код ISO-4217, например "JPY", "CNY", "EUR"
 */
@Serializable
data class Money(val minorUnits: Long, val currency: String) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "нельзя складывать $currency и ${other.currency}" }
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    companion object {
        val ZERO_PLACEHOLDER = Money(0, "")
    }
}

/** Курс на момент поездки. Хранится рядом с поездкой, чтобы бюджет не «плыл» потом. */
@Serializable
data class ExchangeRate(
    val from: String,
    val to: String,
    val rate: Double,
    val fetchedAt: Long = 0L,
)
