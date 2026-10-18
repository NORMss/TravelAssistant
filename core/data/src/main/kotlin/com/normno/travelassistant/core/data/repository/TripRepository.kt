package com.normno.travelassistant.core.data.repository

import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripId
import kotlinx.coroutines.flow.Flow

/**
 * Единственный вход к данным поездок для feature-модулей.
 *
 * Интерфейс, а не класс, по двум причинам: тест экрана подменяет его тремя строками
 * вместо поднятия Room, а будущая синхронизация добавится как декоратор, не трогая
 * ни один экран.
 */
interface TripRepository {

    fun observeTrips(): Flow<List<Trip>>

    fun observeTrip(id: TripId): Flow<Trip?>

    /** Поездка, в чьи даты попадает сегодня. На неё открывается приложение. */
    fun observeActiveTrip(): Flow<Trip?>

    suspend fun find(id: TripId): Trip?

    suspend fun save(trip: Trip)

    /** Удаление ставит надгробие: физически строку чистит синхронизация. */
    suspend fun delete(id: TripId)
}

/**
 * План поездки целиком: дни со своими этапами и развилками.
 *
 * Отдаётся одним потоком, а не тремя. Экран «План» показывает день с этапами и
 * развилками сразу; собирать их из трёх независимых потоков значит получить мигание
 * при каждой вставке этапа — день уже обновился, этапы ещё нет.
 */
interface ItineraryRepository {

    fun observeDays(tripId: TripId): Flow<List<ItineraryDay>>

    fun observeDay(dayId: com.normno.travelassistant.core.model.DayId): Flow<ItineraryDay?>

    suspend fun saveDay(day: ItineraryDay)

    suspend fun saveStop(stop: com.normno.travelassistant.core.model.Stop)

    suspend fun setStopDone(stopId: com.normno.travelassistant.core.model.StopId, done: Boolean)

    suspend fun reorderStops(
        dayId: com.normno.travelassistant.core.model.DayId,
        orderedIds: List<com.normno.travelassistant.core.model.StopId>,
    )

    suspend fun deleteStop(stopId: com.normno.travelassistant.core.model.StopId)

    suspend fun chooseFork(
        forkId: com.normno.travelassistant.core.model.ForkId,
        optionKey: String?,
    )
}
