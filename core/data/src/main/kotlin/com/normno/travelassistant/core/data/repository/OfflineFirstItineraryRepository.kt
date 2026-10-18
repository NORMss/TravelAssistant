package com.normno.travelassistant.core.data.repository

import com.normno.travelassistant.core.common.time.TripClock
import com.normno.travelassistant.core.database.dao.DayDao
import com.normno.travelassistant.core.database.dao.ForkDao
import com.normno.travelassistant.core.database.dao.StopDao
import com.normno.travelassistant.core.database.mapper.toEntity
import com.normno.travelassistant.core.database.mapper.toModel
import com.normno.travelassistant.core.model.DayId
import com.normno.travelassistant.core.model.ForkId
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.StopId
import com.normno.travelassistant.core.model.SyncMeta
import com.normno.travelassistant.core.model.TripId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstItineraryRepository @Inject constructor(
    private val dayDao: DayDao,
    private val stopDao: StopDao,
    private val forkDao: ForkDao,
    private val clock: TripClock,
) : ItineraryRepository {

    /**
     * Три таблицы сводятся в один поток здесь, а не в ViewModel. Так день приходит
     * на экран уже целым: без отдельного кадра, где день есть, а этапов ещё нет.
     */
    override fun observeDays(tripId: TripId): Flow<List<ItineraryDay>> = combine(
        dayDao.observeByTrip(tripId.value),
        stopDao.observeByTrip(tripId.value),
        forkDao.observeByTrip(tripId.value),
    ) { days, stops, forks ->
        val stopsByDay = stops.groupBy { it.dayId }
        val forksByDay = forks.groupBy { it.dayId }
        days.map { day ->
            day.toModel(
                stops = stopsByDay[day.id].orEmpty().map { it.toModel() },
                forks = forksByDay[day.id].orEmpty().map { it.toModel() },
            )
        }
    }

    override fun observeDay(dayId: DayId): Flow<ItineraryDay?> = combine(
        stopDao.observeByDay(dayId.value),
        forkDao.observeByDay(dayId.value),
    ) { stops, forks ->
        val day = dayDao.find(dayId.value) ?: return@combine null
        day.toModel(
            stops = stops.map { it.toModel() },
            forks = forks.map { it.toModel() },
        )
    }

    override suspend fun saveDay(day: ItineraryDay) {
        dayDao.upsert(day.copy(sync = touch()).toEntity())
    }

    override suspend fun saveStop(stop: Stop) {
        stopDao.upsert(stop.copy(sync = touch()).toEntity())
    }

    override suspend fun setStopDone(stopId: StopId, done: Boolean) =
        stopDao.setDone(stopId.value, done, now())

    override suspend fun reorderStops(dayId: DayId, orderedIds: List<StopId>) =
        stopDao.reorder(dayId.value, orderedIds.map { it.value }, now())

    override suspend fun deleteStop(stopId: StopId) = stopDao.markDeleted(stopId.value, now())

    override suspend fun chooseFork(forkId: ForkId, optionKey: String?) =
        forkDao.choose(forkId.value, optionKey, now())

    private fun now() = clock.now().toEpochMilliseconds()

    private fun touch() = SyncMeta.touched(now())
}
