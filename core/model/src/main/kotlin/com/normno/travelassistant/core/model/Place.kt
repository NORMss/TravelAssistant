package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Место на карте. Живёт отдельно от этапа, потому что одно место бывает нужно в разные
 * дни (отель — каждый вечер), а своё место пользователь ставит долгим нажатием по карте
 * вообще без привязки к плану.
 */
@Serializable
data class Place(
    val id: PlaceId,
    val tripId: TripId?,
    val name: String,
    val localName: String = "",
    val transliteration: String = "",
    val address: Address = Address(),
    val kind: PlaceKind = PlaceKind.POINT,
    val note: String = "",
    val source: PlaceSource = PlaceSource.USER,
    val rating: Int? = null,
    val visited: Boolean = false,
    val sync: SyncMeta = SyncMeta(),
) {
    val point: GeoPoint? get() = address.point
}

enum class PlaceKind {
    POINT,
    HOTEL,
    AIRPORT,
    STATION,
    RESTAURANT,
    CAFE,
    SIGHT,
    MUSEUM,
    PARK,
    SHOP,
    PHARMACY,
    HOSPITAL,
    EMBASSY,
    ATM,
    TOILET,
}

/** Откуда место взялось — влияет на то, можно ли его молча перезаписать при импорте. */
enum class PlaceSource {
    /** Поставил пользователь. Импорт такие места не трогает. */
    USER,

    /** Пришло вместе с планом. */
    PLAN,

    /** Предложила модель. Требует подтверждения перед тем, как попасть в план. */
    SUGGESTED,

    /** Геокодирование по адресу. */
    GEOCODED,
}
