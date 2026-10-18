package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Идентификаторы — строки, а не автоинкременты. Причина одна: синхронизация.
 * Запись должна получать свой id на телефоне, до любого обращения к серверу, и этот id
 * не должен конфликтовать с id, выданным другим телефоном того же пользователя.
 */
@JvmInline @Serializable value class TripId(val value: String)
@JvmInline @Serializable value class DayId(val value: String)
@JvmInline @Serializable value class StopId(val value: String)
@JvmInline @Serializable value class PlaceId(val value: String)
@JvmInline @Serializable value class ForkId(val value: String)
@JvmInline @Serializable value class PhraseId(val value: String)
@JvmInline @Serializable value class TravellerId(val value: String)
