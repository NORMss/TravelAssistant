package com.normno.travelassistant.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.serialization.Serializable

/**
 * Поездка. В отличие от прошлого приложения, где поездка была одна и зашита в APK,
 * здесь их много, они бывают любой длины и могут пересекать несколько стран и часовых поясов.
 */
@Serializable
data class Trip(
    val id: TripId,
    val title: String,
    val subtitle: String = "",
    val startDate: LocalDate,
    val endDate: LocalDate,
    /** Места назначения по порядку. Пустой список допустим: у черновика их ещё нет. */
    val destinations: List<Destination> = emptyList(),
    /** Часовой пояс, в котором пользователь живёт, — для «сколько осталось до вылета». */
    val homeZoneId: String = "UTC",
    /** Валюта, в которой пользователь считает деньги. */
    val homeCurrency: String = "RUB",
    val travellers: List<Traveller> = emptyList(),
    val coverUri: String? = null,
    val status: TripStatus = TripStatus.DRAFT,
    val notes: String = "",
    val sync: SyncMeta = SyncMeta(),
) {
    val nightCount: Int get() = startDate.daysUntil(endDate)
    val dayCount: Int get() = nightCount + 1

    /**
     * Часовой пояс, по которому идёт экран «Сейчас». Прошлое приложение жёстко держало
     * Asia/Shanghai; здесь это первый пункт назначения, а при переезде — тот, в котором
     * пользователь находится по плану.
     */
    fun zoneIdOn(date: LocalDate): String =
        destinations.lastOrNull { it.arrivalDate == null || it.arrivalDate <= date }?.zoneId
            ?: destinations.firstOrNull()?.zoneId
            ?: homeZoneId

    val isMultiCountry: Boolean get() = destinations.map { it.countryCode }.distinct().size > 1
}

enum class TripStatus {
    /** Планируется, даты могут ещё поехать. */
    DRAFT,

    /** План готов, поездка не началась. */
    PLANNED,

    /** Идёт сейчас — приложение открывается на «Сейчас». */
    ACTIVE,

    /** Закончилась, остаётся как дневник. */
    FINISHED,

    ARCHIVED,
}

/**
 * Город или регион внутри поездки. Несёт язык и часовой пояс — то, из чего всё
 * остальное выводится: какой разговорник показывать, по какому времени считать этапы,
 * какие карточные приложения предлагать.
 *
 * @param countryCode ISO 3166-1 alpha-2, например "JP"
 * @param localeTag BCP-47 языка места, например "ja-JP". По нему подбирается разговорник
 *   и направление перевода в LLM-подсказках.
 * @param zoneId IANA, например "Asia/Tokyo"
 */
@Serializable
data class Destination(
    val name: String,
    val countryCode: String,
    val localeTag: String,
    val zoneId: String,
    val currency: String = "",
    val arrivalDate: LocalDate? = null,
    val departureDate: LocalDate? = null,
    val center: GeoPoint? = null,
)

/**
 * Участник поездки. Вынесено в модель потому, что уже в прошлом приложении четверо
 * летели по двум броням и двое оставались на день дольше — это норма, а не исключение.
 */
@Serializable
data class Traveller(
    val id: TravellerId,
    val name: String,
    /** true — это владелец телефона. Его данные подставляются в SOS-карточку. */
    val isSelf: Boolean = false,
    val bookingRef: String = "",
    val note: String = "",
    val sync: SyncMeta = SyncMeta(),
)
