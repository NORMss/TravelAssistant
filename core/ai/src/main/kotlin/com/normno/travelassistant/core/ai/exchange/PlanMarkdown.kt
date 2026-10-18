package com.normno.travelassistant.core.ai.exchange

import com.normno.travelassistant.core.common.time.Formatting
import com.normno.travelassistant.core.model.Fork
import com.normno.travelassistant.core.model.ForkOutcome
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.Trip

/**
 * План как Markdown — формат обмена с моделью.
 *
 * Почему не JSON. Проверено на прошлом приложении: весь план в JSON — 240 КБ,
 * один день в Markdown — 6–14 КБ. Разница не только в размере. JSON ломается от одной
 * пропущенной запятой и модель его правит неуверенно; Markdown она правит уверенно,
 * и его видно глазами до того, как применять.
 *
 * Якоря `{#d2s1}` — то, что превращает круговой обход в правку, а не в замену:
 * запись с якорем обновляется, без якоря — добавляется, пропавшая — удаляется.
 *
 * Формат сознательно устойчив к небрежности модели: неизвестные строки игнорируются,
 * порядок полей не важен, лишние пробелы допустимы.
 */
object PlanMarkdown {

    /** Вся поездка. Для длинных поездок лучше отдавать по дню — см. [exportDay]. */
    fun exportTrip(trip: Trip, days: List<ItineraryDay>): String = buildString {
        appendLine("# ${trip.title} {#${trip.id.value}}")
        appendLine("Даты: ${trip.startDate} — ${trip.endDate}")
        if (trip.destinations.isNotEmpty()) {
            appendLine(
                "Места: " + trip.destinations.joinToString(", ") {
                    "${it.name} (${it.countryCode}, ${it.zoneId})"
                },
            )
        }
        if (trip.subtitle.isNotBlank()) appendLine("Подзаголовок: ${trip.subtitle}")
        appendLine()
        days.forEach { day ->
            append(exportDay(day))
            appendLine()
        }
    }

    fun exportDay(day: ItineraryDay): String = buildString {
        val heading = day.title.ifBlank { day.label.ifBlank { "День" } }
        appendLine("## $heading {#${day.id.value}}")
        appendLine("Дата: ${day.date}")
        if (day.label.isNotBlank()) appendLine("Подпись: ${day.label}")
        if (day.lede.isNotBlank()) {
            appendLine()
            appendLine(day.lede)
        }
        appendLine()
        day.stops.sortedBy { it.orderIndex }.forEach { stop ->
            append(exportStop(stop))
            appendLine()
        }
        day.forks.forEach { fork ->
            append(exportFork(fork))
            appendLine()
        }
    }

    fun exportStop(stop: Stop): String = buildString {
        val time = stop.startTime?.let { Formatting.time(it) } ?: "--:--"
        val name = listOf(stop.localTitle, stop.title).filter { it.isNotBlank() }
            .joinToString(" · ")
            .ifBlank { "Этап" }
        appendLine("### $time · $name {#${stop.id.value}}")
        field("Подпись", stop.title)
        field("Тип", stop.category.name)
        field("Длительность", stop.durationMinutes?.let { "$it мин" })
        field("Адрес", stop.address?.local)
        field(
            "Координаты",
            stop.address?.point?.let { "%.5f, %.5f".format(it.lat, it.lng) },
        )
        field("Чтение", stop.transliteration)
        field("Метка", stop.tag)
        field("Бронь", stop.bookingRef)
        field("Ссылка", stop.url)
        field(
            "Напоминание",
            stop.reminder?.let { "за ${it.minutesBefore} мин${if (it.critical) ", важное" else ""}" },
        )
        if (stop.note.isNotBlank()) {
            appendLine()
            appendLine(stop.note)
        }
    }

    fun exportFork(fork: Fork): String = buildString {
        appendLine("##### ${fork.question} {#${fork.id.value}}")
        field("Местное", fork.localQuestion)
        field("Этапы", fork.stopIds.joinToString(", ") { it.value }.ifBlank { null })
        field("Категории", fork.categories.joinToString(", ") { it.name }.ifBlank { null })
        appendLine()
        fork.options.forEach { option ->
            val kind = when (option.kind) {
                ForkOutcome.AS_PLANNED -> "по плану"
                ForkOutcome.DETOUR -> "обход"
                ForkOutcome.CANCEL -> "отмена"
            }
            appendLine("- ${option.key} | $kind | ${option.text}")
        }
    }

    /**
     * Инструкция, которая уезжает вместе с текстом. Без неё модель почти наверняка
     * перепишет план целиком «как лучше» и потеряет якоря — то есть превратит правку
     * в замену.
     */
    val INSTRUCTIONS: String = """
        Ниже план поездки в Markdown. Правь его и верни в том же формате.

        Правила:
        - Якоря в фигурных скобках (`{#id}`) не меняй и не выдумывай новые.
        - Запись с якорем — правка существующей. Запись без якоря — добавление новой.
        - Чтобы удалить запись, убери её блок целиком.
        - Новый день обязан иметь строку `Дата: ГГГГ-ММ-ДД`, иначе его некуда поставить.
        - Поля, которых не знаешь, оставляй как есть. Прочерк `—` значит «нет значения».
        - Не добавляй пояснений до или после плана: ответ разбирается автоматически.
    """.trimIndent()

    private fun StringBuilder.field(name: String, value: String?) {
        if (!value.isNullOrBlank()) appendLine("$name: $value")
    }
}
