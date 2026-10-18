package com.normno.travelassistant.core.vision

import com.normno.travelassistant.core.model.RegionPack
import com.normno.travelassistant.core.model.RegionPackAdvice
import com.normno.travelassistant.core.model.RegionPackState
import com.normno.travelassistant.core.model.RegionPackStatus
import com.normno.travelassistant.core.model.Trip
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import javax.inject.Inject

/**
 * Отвечает на вопрос «что скачать перед этой поездкой».
 *
 * Весь смысл — в моменте. Предложить скачать 47 МБ нужно дома, по Wi-Fi, за
 * несколько дней до вылета. Та же подсказка у входа в храм в роуминге —
 * бесполезна и обидна: интернет дорогой, очередь не ждёт, а скачивание займёт
 * минуты.
 *
 * Логика намеренно отделена от загрузчика и от UI: это чистая функция от
 * поездки, каталога и сегодняшней даты, и она покрыта тестами.
 */
class RegionPackAdvisor @Inject constructor() {

    fun advise(
        trip: Trip,
        catalog: List<RegionPackStatus>,
        today: LocalDate,
    ): RegionPackAdvice {
        val countries = trip.destinations.map { it.countryCode.uppercase() }.toSet()

        val missing = catalog
            .filter { it.state.needsDownload() }
            .map { it.pack }
            .filter { pack -> pack.covers(countries) }
            // Сначала то, что покрывает больше стран маршрута: для поездки
            // Япония + Корея один азиатский пакет закрывает обе.
            .sortedByDescending { it.matchCount(countries) }

        return RegionPackAdvice(
            tripId = trip.id,
            missing = missing,
            daysUntilDeparture = today.daysUntil(trip.startDate),
        )
    }

    /**
     * Пакеты, которые можно удалить: поездки, ради которых их ставили, прошли.
     * Освобождает десятки мегабайт, о которых пользователь иначе не вспомнит.
     */
    fun removable(
        trips: List<Trip>,
        catalog: List<RegionPackStatus>,
        today: LocalDate,
    ): List<RegionPack> {
        val stillNeeded = trips
            .filter { it.endDate >= today }
            .flatMap { it.destinations }
            .map { it.countryCode.uppercase() }
            .toSet()

        return catalog
            .filter { it.state == RegionPackState.INSTALLED }
            .map { it.pack }
            .filterNot { it.covers(stillNeeded) }
    }
}

private fun RegionPackState.needsDownload(): Boolean =
    this == RegionPackState.NOT_INSTALLED || this == RegionPackState.FAILED

private fun RegionPack.covers(countries: Set<String>): Boolean =
    countryCodes.any { it.uppercase() in countries }

private fun RegionPack.matchCount(countries: Set<String>): Int =
    countryCodes.count { it.uppercase() in countries }
