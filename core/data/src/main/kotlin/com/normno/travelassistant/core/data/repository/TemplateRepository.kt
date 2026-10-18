package com.normno.travelassistant.core.data.repository

import com.normno.travelassistant.core.model.TemplateSummary
import com.normno.travelassistant.core.model.TripId
import com.normno.travelassistant.core.model.TripTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Готовые маршруты: что доступно, как применить к своей поездке.
 *
 * Шаблон и поездка — разные вещи, и их разделение не формальность. Шаблон
 * описывает маршрут и уезжает к чужим людям; поездка — личная, с бронями,
 * заметками и фотографиями. Применение превращает первое во второе, и обратной
 * дороги нет: из поездки шаблон не «экспортируется», его собирают заново,
 * сознательно выбирая, что опубликовать.
 */
interface TemplateRepository {

    /** Встроенные плюс скачанные. Первые доступны всегда, в том числе офлайн. */
    fun observeCatalog(): Flow<List<TemplateSummary>>

    suspend fun load(id: String): TripTemplate?

    /**
     * Создаёт поездку по шаблону.
     *
     * @param startDate первый день; относительные дни шаблона раскладываются от него
     * @return id созданной поездки — её сразу открывают на редактирование
     */
    suspend fun applyToNewTrip(
        template: TripTemplate,
        startDate: LocalDate,
        title: String? = null,
    ): TripId
}
