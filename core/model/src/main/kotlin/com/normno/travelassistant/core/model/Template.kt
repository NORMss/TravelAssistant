package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Готовый маршрут, которым можно поделиться и который можно скачать.
 *
 * Главное в этом типе — **чего в нём нет**. Ни броней, ни рейсов, ни участников,
 * ни SOS-карточки, ни заметок, ни фотографий, ни дат. Не «мы договорились их не
 * класть», а физически нет полей, куда это можно положить.
 *
 * Так сделано намеренно. Шаблон уезжает в публичный репозиторий и к чужим людям,
 * а вычищать личное из свободного текста вручную — ненадёжно: достаточно одного
 * пропущенного поля, чтобы наружу ушла фамилия или номер брони. Разделение на
 * [TripTemplate] (публичный слой) и [Trip] (личный слой) делает такую утечку
 * невозможной по типу, а не по внимательности.
 *
 * Поэтому и дни здесь относительные: «День 1», а не «13 сентября 2026». Шаблон
 * описывает маршрут, а не чью-то поездку — даты появляются при применении.
 */
@Serializable
data class TripTemplate(
    val id: String,
    val title: String,
    val summary: String = "",
    val destinations: List<Destination> = emptyList(),
    val days: List<TemplateDay> = emptyList(),
    val places: List<Place> = emptyList(),
    val phraseGroups: List<PhraseGroup> = emptyList(),
    val checklists: List<Checklist> = emptyList(),
    val bingo: List<BingoCard> = emptyList(),
    val author: TemplateAuthor = TemplateAuthor(),
    /** Какие пакеты распознавания достопримечательностей пригодятся на маршруте. */
    val regionPackIds: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val license: String = "CC-BY-4.0",
    val formatVersion: Int = 1,
    val revision: Int = 1,
) {
    val dayCount: Int get() = days.size
    val stopCount: Int get() = days.sumOf { it.stops.size }
}

/**
 * Автор шаблона: только то, что он сам решил показать.
 *
 * Ни почты, ни телефона, ни идентификатора устройства. Подпись может быть и
 * пустой — анонимный шаблон это нормально.
 */
@Serializable
data class TemplateAuthor(
    val displayName: String = "",
    /** Ссылка на профиль, если автор хочет. Проверяется на схему http(s) при импорте. */
    val link: String = "",
)

/** День маршрута. Номер, а не дата: дата появится, когда шаблон применят к поездке. */
@Serializable
data class TemplateDay(
    val dayIndex: Int,
    val title: String = "",
    val label: String = "",
    val summary: String = "",
    val stops: List<TemplateStop> = emptyList(),
    val forks: List<TemplateFork> = emptyList(),
)

/**
 * Этап маршрута. По сравнению со [Stop] нет `bookingRef`, `done`, `reminder` и
 * `sync` — всё это принадлежит конкретной поездке, а не маршруту.
 *
 * `tip` — совет автора про это место. Это авторский текст, а не выписка из
 * личной переписки: при импорте чужого шаблона он показывается как цитата автора.
 */
@Serializable
data class TemplateStop(
    val key: String,
    val orderIndex: Int,
    val title: String,
    val localTitle: String = "",
    val transliteration: String = "",
    /** Ориентировочное время по местному поясу. Сдвигается при применении. */
    val startTime: String = "",
    val durationMinutes: Int? = null,
    val category: StopCategory = StopCategory.SIGHT,
    val address: Address? = null,
    val tip: String = "",
    val tag: String = "",
    /** Ориентир по стоимости на одного человека, в валюте места. */
    val costPerPersonMin: Int? = null,
    val costPerPersonMax: Int? = null,
)

@Serializable
data class TemplateFork(
    val key: String,
    val question: String,
    val localQuestion: String = "",
    val options: List<ForkOption> = emptyList(),
    val stopKeys: List<String> = emptyList(),
    val categories: List<StopCategory> = emptyList(),
)

/**
 * Откуда шаблон взялся. Влияет на то, что показывать пользователю перед импортом:
 * встроенному доверяем, скачанному — предупреждаем, что текст чужой.
 */
enum class TemplateSource { BUILT_IN, DOWNLOADED, IMPORTED_FILE, AUTHORED_LOCALLY }

/** Карточка шаблона в каталоге — то, что видно до скачивания. */
@Serializable
data class TemplateSummary(
    val id: String,
    val title: String,
    val summary: String = "",
    val countryCodes: List<String> = emptyList(),
    val dayCount: Int = 0,
    val stopCount: Int = 0,
    val author: TemplateAuthor = TemplateAuthor(),
    val tags: List<String> = emptyList(),
    val sizeBytes: Long = 0,
    val source: TemplateSource = TemplateSource.DOWNLOADED,
    val regionPackIds: List<String> = emptyList(),
)
