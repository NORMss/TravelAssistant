package com.normno.travelassistant.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.normno.travelassistant.core.database.entity.DayEntity
import com.normno.travelassistant.core.database.entity.ForkEntity
import com.normno.travelassistant.core.database.entity.PlaceEntity
import com.normno.travelassistant.core.database.entity.StopEntity
import com.normno.travelassistant.core.database.entity.TripEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Во всех запросах есть `deleted_at IS NULL`. Это не косметика: удаление в приложении
 * ставит надгробие, а не удаляет строку, иначе синхронизация вернула бы запись назад.
 * Физически строки чистит отдельная задача, когда сервер подтвердил удаление.
 */
@Dao
interface TripDao {

    @Query("SELECT * FROM trips WHERE deleted_at IS NULL ORDER BY start_date DESC")
    fun observeAll(): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :id AND deleted_at IS NULL")
    fun observe(id: String): Flow<TripEntity?>

    @Query("SELECT * FROM trips WHERE id = :id AND deleted_at IS NULL")
    suspend fun find(id: String): TripEntity?

    /**
     * Активная поездка — та, в чьи даты попадает сегодня. Если таких несколько
     * (бывает: вернулся и сразу уехал), берётся начавшаяся позже.
     */
    @Query(
        """
        SELECT * FROM trips
        WHERE deleted_at IS NULL AND :today BETWEEN start_date AND end_date
        ORDER BY start_date DESC LIMIT 1
        """,
    )
    fun observeActive(today: LocalDate): Flow<TripEntity?>

    @Upsert
    suspend fun upsert(trip: TripEntity)

    @Query("UPDATE trips SET deleted_at = :now, dirty = 1 WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)
}

@Dao
interface DayDao {

    @Query("SELECT * FROM days WHERE trip_id = :tripId AND deleted_at IS NULL ORDER BY date")
    fun observeByTrip(tripId: String): Flow<List<DayEntity>>

    @Query("SELECT * FROM days WHERE id = :id AND deleted_at IS NULL")
    suspend fun find(id: String): DayEntity?

    @Query("SELECT * FROM days WHERE trip_id = :tripId AND date = :date AND deleted_at IS NULL")
    suspend fun findByDate(tripId: String, date: LocalDate): DayEntity?

    @Upsert
    suspend fun upsert(day: DayEntity)

    @Upsert
    suspend fun upsertAll(days: List<DayEntity>)

    @Query("UPDATE days SET deleted_at = :now, dirty = 1 WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)
}

@Dao
interface StopDao {

    @Query(
        """
        SELECT * FROM stops
        WHERE trip_id = :tripId AND deleted_at IS NULL
        ORDER BY day_id, order_index
        """,
    )
    fun observeByTrip(tripId: String): Flow<List<StopEntity>>

    @Query(
        """
        SELECT * FROM stops
        WHERE day_id = :dayId AND deleted_at IS NULL
        ORDER BY order_index
        """,
    )
    fun observeByDay(dayId: String): Flow<List<StopEntity>>

    @Query("SELECT * FROM stops WHERE id = :id AND deleted_at IS NULL")
    suspend fun find(id: String): StopEntity?

    @Upsert
    suspend fun upsert(stop: StopEntity)

    @Upsert
    suspend fun upsertAll(stops: List<StopEntity>)

    @Query("UPDATE stops SET done = :done, updated_at = :now, dirty = 1 WHERE id = :id")
    suspend fun setDone(id: String, done: Boolean, now: Long)

    @Query("UPDATE stops SET deleted_at = :now, dirty = 1 WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)

    /**
     * Перестановка этапов внутри дня. Одной транзакцией, потому что промежуточное
     * состояние с двумя одинаковыми `order_index` на экране выглядит как потерянный этап.
     */
    @Transaction
    suspend fun reorder(dayId: String, orderedIds: List<String>, now: Long) {
        orderedIds.forEachIndexed { index, id -> setOrder(id, index, now) }
    }

    @Query("UPDATE stops SET order_index = :order, updated_at = :now, dirty = 1 WHERE id = :id")
    suspend fun setOrder(id: String, order: Int, now: Long)
}

@Dao
interface PlaceDao {

    @Query(
        """
        SELECT * FROM places
        WHERE (trip_id = :tripId OR trip_id IS NULL) AND deleted_at IS NULL
        ORDER BY name
        """,
    )
    fun observeForTrip(tripId: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id AND deleted_at IS NULL")
    suspend fun find(id: String): PlaceEntity?

    @Upsert
    suspend fun upsert(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(places: List<PlaceEntity>)

    @Query("UPDATE places SET deleted_at = :now, dirty = 1 WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)
}

@Dao
interface ForkDao {

    @Query("SELECT * FROM forks WHERE day_id = :dayId AND deleted_at IS NULL")
    fun observeByDay(dayId: String): Flow<List<ForkEntity>>

    @Query("SELECT * FROM forks WHERE trip_id = :tripId AND deleted_at IS NULL")
    fun observeByTrip(tripId: String): Flow<List<ForkEntity>>

    @Upsert
    suspend fun upsert(fork: ForkEntity)

    @Upsert
    suspend fun upsertAll(forks: List<ForkEntity>)

    @Query("UPDATE forks SET chosen_key = :key, updated_at = :now, dirty = 1 WHERE id = :id")
    suspend fun choose(id: String, key: String?, now: Long)

    @Query("UPDATE forks SET deleted_at = :now, dirty = 1 WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)
}
