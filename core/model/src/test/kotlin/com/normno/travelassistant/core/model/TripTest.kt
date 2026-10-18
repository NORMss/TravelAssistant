package com.normno.travelassistant.core.model

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripTest {

    private val tokyo = Destination(
        name = "Токио",
        countryCode = "JP",
        localeTag = "ja-JP",
        zoneId = "Asia/Tokyo",
        arrivalDate = LocalDate(2026, 4, 10),
    )

    private val seoul = Destination(
        name = "Сеул",
        countryCode = "KR",
        localeTag = "ko-KR",
        zoneId = "Asia/Seoul",
        arrivalDate = LocalDate(2026, 4, 14),
    )

    private val trip = Trip(
        id = TripId("t"),
        title = "Япония и Корея",
        startDate = LocalDate(2026, 4, 10),
        endDate = LocalDate(2026, 4, 18),
        destinations = listOf(tokyo, seoul),
        homeZoneId = "Europe/Moscow",
    )

    @Test
    fun `пояс берётся по дате приезда, а не по первому пункту`() {
        assertEquals("Asia/Tokyo", trip.zoneIdOn(LocalDate(2026, 4, 11)))
        assertEquals("Asia/Seoul", trip.zoneIdOn(LocalDate(2026, 4, 15)))
    }

    @Test
    fun `до первого приезда действует пояс первого пункта`() {
        // Иначе обратный отсчёт до вылета считался бы по домашнему поясу,
        // а время этапов записано по местному.
        assertEquals("Asia/Tokyo", trip.zoneIdOn(LocalDate(2026, 4, 1)))
    }

    @Test
    fun `поездка без пунктов назначения не падает`() {
        val draft = trip.copy(destinations = emptyList())
        assertEquals("Europe/Moscow", draft.zoneIdOn(LocalDate(2026, 4, 11)))
    }

    @Test
    fun `ночи и дни считаются по датам`() {
        assertEquals(8, trip.nightCount)
        assertEquals(9, trip.dayCount)
    }

    @Test
    fun `поездка через две страны отмечается как международная`() {
        assertTrue(trip.isMultiCountry)
        assertFalse(trip.copy(destinations = listOf(tokyo)).isMultiCountry)
    }
}
