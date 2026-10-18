package com.normno.travelassistant.core.common.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.toJavaLocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Форматирование без ICU-шаблонов. Причина историческая и дорогая: JVM принимает
 * конструкции, которые движок ICU на Android отвергает, и приложение падает уже
 * на телефоне, пройдя сборку на ноутбуке.
 */
object Formatting {

    fun time(value: LocalTime): String =
        "%02d:%02d".format(value.hour, value.minute)

    fun date(value: LocalDate): String = with(value.toJavaLocalDate()) {
        "%02d.%02d.%04d".format(dayOfMonth, monthValue, year)
    }

    fun dayMonth(value: LocalDate): String = with(value.toJavaLocalDate()) {
        "%d %s".format(dayOfMonth, MONTHS_GENITIVE[monthValue - 1])
    }

    /** «2 ч 15 мин», «15 мин», «меньше минуты» — то, что читается в обратном отсчёте. */
    fun duration(value: Duration): String {
        val totalMinutes = value.inWholeMinutes
        return when {
            totalMinutes <= 0L -> "меньше минуты"
            totalMinutes < 60L -> "$totalMinutes мин"
            totalMinutes % 60L == 0L -> "${totalMinutes / 60} ч"
            else -> "${totalMinutes / 60} ч ${totalMinutes % 60} мин"
        }
    }

    fun minutes(value: Int): String = duration(value.minutes)

    private val MONTHS_GENITIVE = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря",
    )
}
