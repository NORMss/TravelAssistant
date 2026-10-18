package com.normno.travelassistant.core.data

import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.normno.travelassistant.core.model.TripTemplate
import kotlinx.serialization.json.Json
import org.junit.Test
import java.io.File

/**
 * Второй рубеж защиты демонстрационного шаблона.
 *
 * Первый рубеж — тип [TripTemplate]: в нём просто нет полей для броней, рейсов и
 * участников. Второй — этот тест: он читает файл, который реально уедет в
 * публичный репозиторий, и падает, если находит следы личного.
 *
 * Почему именно так. Шаблон собран скриптом `tools/make_demo_template.py` из
 * личного плана поездки. Скрипт можно сломать правкой, забыть перезапустить или
 * расширить «ещё одним полезным полем», которое потащит за собой заметку с
 * номером брони. Тест этого не позволит: он смотрит на результат, а не на намерения.
 *
 * При добавлении нового шаблона достаточно положить файл в `assets/templates/` —
 * тест подхватит его сам.
 */
class DemoTemplatePrivacyTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val templates: List<File> =
        File("src/main/assets/templates").listFiles { f -> f.extension == "json" }
            ?.sortedBy { it.name }
            .orEmpty()

    @Test
    fun `шаблоны вообще есть`() {
        assertThat(templates).isNotEmpty()
    }

    @Test
    fun `шаблон разбирается в модель`() {
        templates.forEach { file ->
            val template = json.decodeFromString<TripTemplate>(file.readText())
            assertThat(template.days).isNotEmpty()
            assertThat(template.days.map { it.dayIndex })
                .containsExactlyElementsIn(1..template.days.size).inOrder()
            assertThat(template.stopCount).isGreaterThan(0)
        }
    }

    @Test
    fun `в шаблоне нет имён, броней и контактов`() {
        templates.forEach { file ->
            val text = file.readText()
            PERSONAL_PATTERNS.forEach { (name, pattern) ->
                val match = pattern.find(text)
                assertWithMessage("%s: %s → «%s»", file.name, name, match?.value)
                    .that(match)
                    .isNull()
            }
        }
    }

    @Test
    fun `в шаблоне нет длинных числовых последовательностей`() {
        // Ловушка на будущее: номера билетов, карт и багажных квитанций выглядят
        // именно так. Порог в семь цифр, а не шесть, выбран сознательно: шесть —
        // это почтовый индекс, который законно стоит в китайском и российском
        // адресе. Шестизначные номера броней ловит отдельное правило выше —
        // по смеси букв и цифр.
        val digits = Regex("""(?<![\d.])\d{7,}(?![\d.])""")
        templates.forEach { file ->
            val found = digits.findAll(file.readText()).map { it.value }.toList()
            assertWithMessage("%s: %s", file.name, found).that(found).isEmpty()
        }
    }

    @Test
    fun `в шаблоне нет дат конкретной поездки`() {
        // Дни в шаблоне относительные. Появившаяся дата означает, что кто-то
        // перенёс в шаблон кусок личного плана.
        val isoDate = Regex("""\d{4}-\d{2}-\d{2}""")
        templates.forEach { file ->
            val found = isoDate.findAll(file.readText()).map { it.value }.toList()
            assertWithMessage("%s: %s", file.name, found).that(found).isEmpty()
        }
    }

    private companion object {
        val PERSONAL_PATTERNS = listOf(
            "фамилия автора" to Regex("Bezborod", RegexOption.IGNORE_CASE),
            "имя" to Regex("""\bСергей|Серёж|Сергея""", RegexOption.IGNORE_CASE),
            "почта" to Regex("""[\w.+-]+@[\w-]+\.[a-z]{2,}""", RegexOption.IGNORE_CASE),
            // Код рейса — две буквы или буква с цифрой, затем номер: «S7 5724».
            // Номер торгового помещения вида «E103» под это не подходит, иначе
            // тест падал бы на адресе кофейни в торговом центре.
            "код рейса" to Regex("""\b([A-Z]{2}|[A-Z]\d)\s?\d{3,4}\b"""),
            // Номер брони — смесь букв и цифр: «O6FQ86», «MVX9JRX». Требование
            // иметь и то и другое отсекает значения перечислений вроде FLIGHT.
            "номер брони" to Regex(
                """\b(?=[A-Z0-9]{6,8}\b)(?=[A-Z0-9]*\d)(?=[A-Z0-9]*[A-Z])[A-Z0-9]+\b""",
            ),
            "номер телефона" to Regex("""\+\d[\d\s()-]{7,}"""),
            "депозит и переписка с отелем" to Regex(
                "депозит|ПОПРАВКА|письменн|предоплат", RegexOption.IGNORE_CASE,
            ),
            "размер группы" to Regex("вчетвер|на четвер|четвером", RegexOption.IGNORE_CASE),
            "город вылета" to Regex("Новосиб|Толмач", RegexOption.IGNORE_CASE),
        )
    }
}
