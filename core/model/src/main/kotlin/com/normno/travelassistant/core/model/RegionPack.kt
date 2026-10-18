package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Пакет распознавания для региона: модель плюс список классов.
 *
 * Почему это вообще отдельная сущность. В прошлом проекте автора три
 * классификатора лежали прямо в APK и весили 22, 47 и 48 МБ — 117 МБ на
 * приложение, которое пользователь ставит ради плана поездки. Человеку, едущему
 * в Японию, африканский классификатор не нужен ни разу.
 *
 * Поэтому пакеты скачиваются по одному, по требованию, и удаляются после
 * поездки. Приложение без единого пакета полностью работоспособно — распознавание
 * это дополнение к фотоотчёту, а не условие его работы.
 */
@Serializable
data class RegionPack(
    val id: String,
    val title: String,
    /** Страны ISO 3166-1 alpha-2, для которых пакет осмыслен. */
    val countryCodes: List<String> = emptyList(),
    val sizeBytes: Long = 0,
    val landmarkCount: Int = 0,
    val version: Int = 1,
    /** Откуда качать. Пусто — пакет поставляется механизмом магазина. */
    val url: String = "",
    /** Контрольная сумма: скачанный файл проверяется до того, как попадёт в модель. */
    val sha256: String = "",
)

enum class RegionPackState {
    NOT_INSTALLED,
    QUEUED,
    DOWNLOADING,
    INSTALLED,
    /** Вышла новая версия модели; старая продолжает работать. */
    UPDATE_AVAILABLE,
    FAILED,
}

@Serializable
data class RegionPackStatus(
    val pack: RegionPack,
    val state: RegionPackState = RegionPackState.NOT_INSTALLED,
    /** 0..1, осмысленно только в состоянии DOWNLOADING. */
    val progress: Float = 0f,
    val installedVersion: Int? = null,
    val error: String? = null,
)

/**
 * Совет «что скачать перед поездкой».
 *
 * Смысл в моменте: предложить скачать 47 МБ нужно дома по Wi-Fi, за несколько
 * дней до вылета, а не в роуминге у входа в храм. Поэтому подсказка привязана к
 * поездке и к оставшемуся до неё времени, а не к экрану камеры.
 */
@Serializable
data class RegionPackAdvice(
    val tripId: TripId,
    val missing: List<RegionPack> = emptyList(),
    val daysUntilDeparture: Int = 0,
) {
    val totalBytes: Long get() = missing.sumOf { it.sizeBytes }
    val isEmpty: Boolean get() = missing.isEmpty()

    /** Пора показывать: за неделю до выезда и пока есть что качать. */
    val isDue: Boolean get() = !isEmpty && daysUntilDeparture in 0..7
}
