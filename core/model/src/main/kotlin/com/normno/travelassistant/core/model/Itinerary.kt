package com.normno.travelassistant.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/** День плана. Может быть пустым — «свободный день» это тоже решение. */
@Serializable
data class ItineraryDay(
    val id: DayId,
    val tripId: TripId,
    val date: LocalDate,
    val title: String = "",
    /** Короткая подпись для вкладки/чипа: «День 3», «Мутяньюй». */
    val label: String = "",
    val lede: String = "",
    val stops: List<Stop> = emptyList(),
    val forks: List<Fork> = emptyList(),
    val sync: SyncMeta = SyncMeta(),
) {
    val isEmpty: Boolean get() = stops.isEmpty()
}

/**
 * Этап дня: переезд, вход, еда, прогулка. Единица, вокруг которой строится весь экран
 * «Сейчас», и единственная сущность, которую пользователь правит руками чаще всего.
 *
 * @param title название на понятном пользователю языке
 * @param localTitle название на языке места — то, что показывают местным
 * @param transliteration чтение вслух
 * @param startTime местное время места, не телефона
 */
@Serializable
data class Stop(
    val id: StopId,
    val tripId: TripId,
    val dayId: DayId,
    val orderIndex: Int,
    val title: String,
    val localTitle: String = "",
    val transliteration: String = "",
    val startTime: LocalTime? = null,
    val durationMinutes: Int? = null,
    val category: StopCategory = StopCategory.SIGHT,
    val placeId: PlaceId? = null,
    val address: Address? = null,
    val note: String = "",
    /** Короткая метка-чип: «800 CNY на всех», «по брони», «нужен паспорт». */
    val tag: String = "",
    val estimatedCost: Money? = null,
    val bookingRef: String = "",
    val url: String = "",
    val reminder: Reminder? = null,
    val done: Boolean = false,
    val sync: SyncMeta = SyncMeta(),
) {
    val hasCoordinates: Boolean get() = address?.point != null

    /** Чем подписать этап, если местное название есть, а перевода нет (и наоборот). */
    val displayTitle: String get() = title.ifBlank { localTitle }
}

/**
 * Категория задаёт не иконку, а набор действий под этапом: у перелёта — номер брони и
 * онлайн-регистрация, у еды — меню и аллергии, у входа — билет и очередь.
 */
enum class StopCategory {
    FLIGHT,
    TRAIN,
    TRANSIT,
    CHECK_IN,
    HOTEL,
    SIGHT,
    MUSEUM,
    NATURE,
    ACTIVITY,
    FOOD,
    SHOPPING,
    TICKET,
    MEETING,
    FREE_TIME,
    OTHER,
}

/**
 * Будильник на жёсткий срок. Регистрация закрывается в 05:15 — это не напоминание,
 * которое можно пропустить.
 */
@Serializable
data class Reminder(
    val minutesBefore: Int = 0,
    /** true — точный будильник, пробивающий «не беспокоить». */
    val critical: Boolean = false,
    val text: String = "",
)

/**
 * Развилка дня — обобщённый «план Б» из прошлого приложения. Вопрос, который задаёт
 * реальность (дождь? очередь? ноги?), и варианты, которые на него отвечают.
 */
@Serializable
data class Fork(
    val id: ForkId,
    val tripId: TripId,
    val dayId: DayId,
    val question: String,
    val localQuestion: String = "",
    val options: List<ForkOption> = emptyList(),
    /** Этапы, которые эта развилка решает, — чтобы показать её в нужный момент. */
    val stopIds: List<StopId> = emptyList(),
    val categories: List<StopCategory> = emptyList(),
    /** Что выбрал пользователь. Это пометка для своих, план она не меняет. */
    val chosenKey: String? = null,
    val sync: SyncMeta = SyncMeta(),
)

@Serializable
data class ForkOption(
    val key: String,
    val kind: ForkOutcome = ForkOutcome.AS_PLANNED,
    val text: String = "",
    /** Названия на языке места внутри ответа: запасной ресторан часто назван только так. */
    val localTerms: List<String> = emptyList(),
)

enum class ForkOutcome {
    /** Идём по плану. */
    AS_PLANNED,

    /** Обходной путь. */
    DETOUR,

    /** Часть дня отменяется. */
    CANCEL,
}
