package com.normno.travelassistant.core.database.entity

import androidx.room.ColumnInfo
import com.normno.travelassistant.core.model.SyncMeta

/**
 * Четыре колонки синхронизации, встроенные в каждую таблицу через `@Embedded`.
 *
 * Их здесь ещё никто не читает — сервера нет. Но добавить их позже значит переписать
 * миграции всех таблиц и все запросы разом, а добавить сейчас — четыре поля, которые
 * до появления сервера просто лежат.
 *
 * Ключевое следствие уже работает: удаление — это `deletedAt`, а не `DELETE`. Иначе
 * запись, удалённая на одном телефоне, вернулась бы со второго.
 */
data class SyncColumns(
    @ColumnInfo(name = "updated_at", defaultValue = "0") val updatedAt: Long = 0L,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @ColumnInfo(name = "remote_revision") val remoteRevision: String? = null,
    @ColumnInfo(name = "dirty", defaultValue = "1") val dirty: Boolean = true,
)

internal fun SyncColumns.toModel() = SyncMeta(updatedAt, deletedAt, remoteRevision, dirty)

internal fun SyncMeta.toColumns() = SyncColumns(updatedAt, deletedAt, remoteRevision, dirty)
