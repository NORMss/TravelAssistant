package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Всегда WGS-84. Преобразования под локальные системы координат — дело слоя карт. */
@Serializable
data class GeoPoint(val lat: Double, val lng: Double) {
    init {
        require(lat in -90.0..90.0) { "широта вне диапазона: $lat" }
        require(lng in -180.0..180.0) { "долгота вне диапазона: $lng" }
    }

    /** Расстояние по большому кругу, метры. Хватает для «что рядом». */
    fun distanceTo(other: GeoPoint): Double {
        val r = 6_371_008.8
        val dLat = Math.toRadians(other.lat - lat)
        val dLng = Math.toRadians(other.lng - lng)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat)) * cos(Math.toRadians(other.lat)) *
            sin(dLng / 2) * sin(dLng / 2)
        return 2 * r * asin(sqrt(a))
    }
}

/**
 * Адрес в двух языках сразу. Главный урок прошлого приложения: таксисту нужны не
 * наши буквы, а местные, и копировать их надо одним касанием.
 *
 * @param local адрес на языке места — то, что попадает в буфер обмена
 * @param display понятная пользователю подпись
 * @param transliteration чтение вслух (пиньинь, ромадзи, латиница)
 */
@Serializable
data class Address(
    val local: String = "",
    val display: String = "",
    val transliteration: String = "",
    val point: GeoPoint? = null,
) {
    val isEmpty: Boolean get() = local.isBlank() && display.isBlank()
    val copyText: String get() = local.ifBlank { display }
}
