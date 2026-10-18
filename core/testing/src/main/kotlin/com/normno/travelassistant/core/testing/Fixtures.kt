package com.normno.travelassistant.core.testing

import com.normno.travelassistant.core.model.Address
import com.normno.travelassistant.core.model.DayId
import com.normno.travelassistant.core.model.Destination
import com.normno.travelassistant.core.model.GeoPoint
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.StopCategory
import com.normno.travelassistant.core.model.StopId
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripId
import com.normno.travelassistant.core.model.TripStatus
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Готовые данные для тестов. Один набор на весь проект: тесты, которые собирают
 * поездку каждый сам, расходятся в деталях, и падение одного ничего не говорит о другом.
 *
 * Поездка намеренно через две страны и два часовых пояса — именно на этом ломается
 * логика, написанная в расчёте на один город.
 */
object Fixtures {

    val tripId = TripId("trip-1")
    val day1Id = DayId("day-1")
    val day2Id = DayId("day-2")

    val tokyo = Destination(
        name = "Токио",
        countryCode = "JP",
        localeTag = "ja-JP",
        zoneId = "Asia/Tokyo",
        currency = "JPY",
        arrivalDate = LocalDate(2026, 4, 10),
        center = GeoPoint(35.6762, 139.6503),
    )

    val seoul = Destination(
        name = "Сеул",
        countryCode = "KR",
        localeTag = "ko-KR",
        zoneId = "Asia/Seoul",
        currency = "KRW",
        arrivalDate = LocalDate(2026, 4, 14),
        center = GeoPoint(37.5665, 126.9780),
    )

    val trip = Trip(
        id = tripId,
        title = "Япония и Корея",
        startDate = LocalDate(2026, 4, 10),
        endDate = LocalDate(2026, 4, 18),
        destinations = listOf(tokyo, seoul),
        homeZoneId = "Europe/Moscow",
        homeCurrency = "RUB",
        status = TripStatus.PLANNED,
    )

    val sensoji = Stop(
        id = StopId("stop-1"),
        tripId = tripId,
        dayId = day1Id,
        orderIndex = 0,
        title = "Храм Сэнсо-дзи",
        localTitle = "浅草寺",
        transliteration = "Sensō-ji",
        startTime = LocalTime(9, 30),
        durationMinutes = 90,
        category = StopCategory.SIGHT,
        address = Address(
            local = "東京都台東区浅草2-3-1",
            display = "Асакуса, Тайто",
            transliteration = "Taito, Asakusa 2-3-1",
            point = GeoPoint(35.7148, 139.7967),
        ),
        tag = "вход свободный",
    )

    val lunch = Stop(
        id = StopId("stop-2"),
        tripId = tripId,
        dayId = day1Id,
        orderIndex = 1,
        title = "Обед",
        startTime = LocalTime(12, 0),
        category = StopCategory.FOOD,
    )

    val day1 = ItineraryDay(
        id = day1Id,
        tripId = tripId,
        date = LocalDate(2026, 4, 10),
        title = "Прилёт и Асакуса",
        label = "День 1",
        stops = listOf(sensoji, lunch),
    )
}
