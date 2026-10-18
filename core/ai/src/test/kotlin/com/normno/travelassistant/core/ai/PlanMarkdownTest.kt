package com.normno.travelassistant.core.ai

import com.normno.travelassistant.core.ai.exchange.PlanMarkdown
import com.normno.travelassistant.core.testing.Fixtures
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Экспорт — половина кругового обхода, и именно та половина, от которой зависит
 * вторая: если якорь не уехал к модели, вернуть правку вместо замены уже нельзя.
 */
class PlanMarkdownTest {

    @Test
    fun `у каждой записи есть якорь`() {
        val markdown = PlanMarkdown.exportDay(Fixtures.day1)

        assertThat(markdown).contains("{#${Fixtures.day1Id.value}}")
        assertThat(markdown).contains("{#${Fixtures.sensoji.id.value}}")
        assertThat(markdown).contains("{#${Fixtures.lunch.id.value}}")
    }

    @Test
    fun `местное написание и адрес уезжают к модели`() {
        val markdown = PlanMarkdown.exportDay(Fixtures.day1)

        assertThat(markdown).contains("浅草寺")
        assertThat(markdown).contains("東京都台東区浅草2-3-1")
        assertThat(markdown).contains("35.71480, 139.79670")
    }

    @Test
    fun `дата дня обязательна в выгрузке`() {
        // Без даты вернувшийся день некуда поставить на таймлайн, и импорт его отклонит.
        assertThat(PlanMarkdown.exportDay(Fixtures.day1)).contains("Дата: 2026-04-10")
    }

    @Test
    fun `пустые поля не засоряют выгрузку`() {
        val markdown = PlanMarkdown.exportStop(Fixtures.lunch)

        assertThat(markdown).doesNotContain("Адрес:")
        assertThat(markdown).doesNotContain("Координаты:")
        assertThat(markdown).contains("Тип: FOOD")
    }

    @Test
    fun `инструкция запрещает выдумывать якоря`() {
        assertThat(PlanMarkdown.INSTRUCTIONS).contains("не выдумывай новые")
    }
}
