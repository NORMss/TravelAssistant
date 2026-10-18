package com.normno.travelassistant.core.data.repository

import com.normno.travelassistant.core.common.time.TripClock
import com.normno.travelassistant.core.database.dao.TripDao
import com.normno.travelassistant.core.database.mapper.toEntity
import com.normno.travelassistant.core.database.mapper.toModel
import com.normno.travelassistant.core.model.SyncMeta
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Локальная база — источник правды. Сеть, когда появится, будет писать сюда же, а не
 * в обход: приложение обязано работать в самолёте и в стране с заблокированными
 * сервисами, а не деградировать до списка «нет соединения».
 */
@Singleton
class OfflineFirstTripRepository @Inject constructor(
    private val dao: TripDao,
    private val clock: TripClock,
) : TripRepository {

    override fun observeTrips(): Flow<List<Trip>> =
        dao.observeAll().map { rows -> rows.map { it.toModel() } }

    override fun observeTrip(id: TripId): Flow<Trip?> =
        dao.observe(id.value).map { it?.toModel() }

    override fun observeActiveTrip(): Flow<Trip?> {
        // Дата берётся в системном поясе: пока поездка не началась, других нет.
        val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return dao.observeActive(today).map { it?.toModel() }
    }

    override suspend fun find(id: TripId): Trip? = dao.find(id.value)?.toModel()

    override suspend fun save(trip: Trip) {
        // Отметку времени ставит репозиторий, а не вызывающий: иначе рано или поздно
        // появится запись с updatedAt из прошлого, и синхронизация её потеряет.
        val stamped = trip.copy(sync = SyncMeta.touched(clock.now().toEpochMilliseconds()))
        dao.upsert(stamped.toEntity())
    }

    override suspend fun delete(id: TripId) =
        dao.markDeleted(id.value, clock.now().toEpochMilliseconds())
}
