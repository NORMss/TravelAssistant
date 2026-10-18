package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Поля синхронизации есть у каждой сущности с первого дня, хотя сервера ещё нет.
 * Добавить их потом — это миграция всей базы и переписанные репозитории; добавить
 * сейчас — четыре колонки, которые до появления сервера просто никто не читает.
 *
 * @param updatedAt момент последней правки, epoch millis. Разрешение конфликтов —
 *   last-write-wins на уровне поля там, где это безопасно, и на уровне записи там, где нет.
 * @param deletedAt надгробие вместо физического удаления: иначе запись, удалённая на одном
 *   телефоне, вернётся со второго при следующей синхронизации.
 * @param remoteRevision версия, которую подтвердил сервер. null — запись сервер ещё не видел.
 * @param dirty есть локальные правки, не отправленные наверх.
 */
@Serializable
data class SyncMeta(
    val updatedAt: Long = 0L,
    val deletedAt: Long? = null,
    val remoteRevision: String? = null,
    val dirty: Boolean = true,
) {
    val isDeleted: Boolean get() = deletedAt != null

    companion object {
        fun touched(now: Long) = SyncMeta(updatedAt = now, dirty = true)
    }
}
