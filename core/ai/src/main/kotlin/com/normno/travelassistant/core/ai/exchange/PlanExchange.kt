package com.normno.travelassistant.core.ai.exchange

import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.PlanProposal
import com.normno.travelassistant.core.model.Trip

/**
 * Разбор отредактированного Markdown обратно в набор операций над планом.
 *
 * Контракт, от которого нельзя отступать: разбор **никогда** не пишет в базу. Он
 * возвращает [PlanProposal] — список «что добавится, что изменится (с точностью до
 * поля), что удалится». Применяет его пользователь, нажав «Применить», и до этого
 * момента план не меняется. Модель, оборвавшая ответ на середине, иначе удалила бы
 * половину поездки — этот случай наблюдался и именно поэтому экран подтверждения
 * существует.
 *
 * Отсюда же второе требование: пропажа блока считается удалением только если в тексте
 * вообще были блоки этого вида. Молчание — не удаление.
 */
interface PlanImporter {

    /**
     * @param trip текущая поездка — нужна, чтобы отличить правку от добавления
     * @param currentDays текущее состояние плана для сравнения
     * @param markdown то, что вернула модель или вставил пользователь
     */
    fun parse(trip: Trip, currentDays: List<ItineraryDay>, markdown: String): PlanProposal
}
