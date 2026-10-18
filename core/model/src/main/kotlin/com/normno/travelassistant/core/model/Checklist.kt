package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Список сборов. Галочки хранятся по id пункта, а не по позиции: правка текста или
 * перестановка соседей не должна сбивать отметку — на этом прошлое приложение
 * спотыкалось до того, как у пунктов появились собственные id.
 */
@Serializable
data class Checklist(
    val id: String,
    val tripId: TripId?,
    val title: String,
    val dayId: DayId? = null,
    val builtin: Boolean = false,
    val items: List<ChecklistItem> = emptyList(),
    val sync: SyncMeta = SyncMeta(),
) {
    val doneCount: Int get() = items.count { it.checked }
}

@Serializable
data class ChecklistItem(
    val id: String,
    val text: String,
    val checked: Boolean = false,
    val note: String = "",
)

/**
 * Карточка бинго — маленькая игра поверх поездки: «заказать еду, тыкая пальцем
 * в картинки», «расплатиться местным QR». Живёт рядом со списками сборов, потому
 * что механика та же: набор пунктов, которые отмечают по ходу.
 *
 * В отличие от заметок и фотографий, бинго не содержит ничего личного и свободно
 * уезжает в шаблон маршрута: это часть авторского описания места, а не дневник.
 */
@Serializable
data class BingoCard(
    val id: String,
    val text: String,
    val emoji: String = "",
    val builtin: Boolean = false,
    val done: Boolean = false,
)
