package com.normno.travelassistant.core.common.time

import com.normno.travelassistant.core.model.Trip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Время в приложении про путешествия двусмысленно: телефон живёт в одном поясе,
 * план — в другом, а напоминание о регистрации должно сработать по третьему.
 * Поэтому «сейчас» всегда спрашивают здесь и всегда с указанием пояса.
 *
 * Прошлое приложение решало это проще — жёстко прошитым `Asia/Shanghai`. Для одной
 * поездки этого хватало; для поездки через три страны уже нет.
 */
interface TripClock {
    fun now(): Instant

    fun nowIn(zoneId: String): LocalDateTime =
        now().toLocalDateTime(zoneOf(zoneId))

    /** Текущее местное время в точке плана на заданную дату. */
    fun nowInTrip(trip: Trip, onDate: LocalDate): LocalDateTime =
        nowIn(trip.zoneIdOn(onDate))

    /** Тикает каждую секунду — для обратного отсчёта до следующего этапа. */
    fun ticker(period: Duration = 1.seconds): Flow<Instant>

    companion object {
        /**
         * Неизвестный или сломанный идентификатор пояса не должен ронять экран:
         * план мог прийти из импорта, где зону вписали руками.
         */
        fun zoneOf(zoneId: String): TimeZone =
            runCatching { TimeZone.of(zoneId) }.getOrDefault(TimeZone.UTC)
    }
}

internal fun zoneOf(zoneId: String): TimeZone = TripClock.zoneOf(zoneId)

class SystemTripClock : TripClock {
    override fun now(): Instant = Clock.System.now()

    override fun ticker(period: Duration): Flow<Instant> = flow {
        while (true) {
            emit(now())
            kotlinx.coroutines.delay(period)
        }
    }
}
