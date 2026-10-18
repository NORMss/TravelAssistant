package com.normno.travelassistant.core.vision

import com.google.common.truth.Truth.assertThat
import com.normno.travelassistant.core.model.RegionPackState
import com.normno.travelassistant.core.model.RegionPackStatus
import com.normno.travelassistant.core.testing.Fixtures
import kotlinx.datetime.LocalDate
import org.junit.Test

class RegionPackAdvisorTest {

    private val advisor = RegionPackAdvisor()

    private val catalog = RegionPackRepository.BUILT_IN_CATALOG.map {
        RegionPackStatus(pack = it, state = RegionPackState.NOT_INSTALLED)
    }

    /** Поездка в фикстуре — Япония и Корея, обе страны в азиатском пакете. */
    @Test
    fun `предлагает только пакеты стран маршрута`() {
        val advice = advisor.advise(Fixtures.trip, catalog, LocalDate(2026, 4, 5))

        assertThat(advice.missing.map { it.id }).containsExactly("asia-v1")
        assertThat(advice.totalBytes).isEqualTo(47L * 1024 * 1024)
    }

    @Test
    fun `установленные пакеты не предлагаются повторно`() {
        val installed = catalog.map {
            if (it.pack.id == "asia-v1") it.copy(state = RegionPackState.INSTALLED) else it
        }

        val advice = advisor.advise(Fixtures.trip, installed, LocalDate(2026, 4, 5))

        assertThat(advice.isEmpty).isTrue()
        assertThat(advice.isDue).isFalse()
    }

    @Test
    fun `неудачную загрузку предлагает повторить`() {
        val failed = catalog.map {
            if (it.pack.id == "asia-v1") it.copy(state = RegionPackState.FAILED) else it
        }

        assertThat(advisor.advise(Fixtures.trip, failed, LocalDate(2026, 4, 5)).missing)
            .hasSize(1)
    }

    @Test
    fun `подсказка созревает за неделю до вылета и не раньше`() {
        // За месяц — рано: пользователь ещё не думает про поездку.
        assertThat(advisor.advise(Fixtures.trip, catalog, LocalDate(2026, 3, 1)).isDue)
            .isFalse()
        assertThat(advisor.advise(Fixtures.trip, catalog, LocalDate(2026, 4, 5)).isDue)
            .isTrue()
        // Во время поездки — тоже показываем: лучше поздно, чем никогда.
        assertThat(advisor.advise(Fixtures.trip, catalog, LocalDate(2026, 4, 10)).isDue)
            .isTrue()
    }

    @Test
    fun `после поездки пакет можно удалить`() {
        val installed = catalog.map {
            if (it.pack.id == "asia-v1") it.copy(state = RegionPackState.INSTALLED) else it
        }

        val afterTrip = advisor.removable(listOf(Fixtures.trip), installed, LocalDate(2026, 5, 1))
        assertThat(afterTrip.map { it.id }).containsExactly("asia-v1")

        val duringTrip = advisor.removable(listOf(Fixtures.trip), installed, LocalDate(2026, 4, 12))
        assertThat(duringTrip).isEmpty()
    }

    @Test
    fun `пакет, закрывающий больше стран маршрута, идёт первым`() {
        val multi = Fixtures.trip.copy(
            destinations = Fixtures.trip.destinations + Fixtures.tokyo.copy(
                name = "Париж",
                countryCode = "FR",
                localeTag = "fr-FR",
                zoneId = "Europe/Paris",
            ),
        )

        val advice = advisor.advise(multi, catalog, LocalDate(2026, 4, 5))

        assertThat(advice.missing.map { it.id }).containsExactly("asia-v1", "europe-v1").inOrder()
    }
}
