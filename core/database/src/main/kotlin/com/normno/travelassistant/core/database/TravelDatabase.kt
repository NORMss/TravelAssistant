package com.normno.travelassistant.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.normno.travelassistant.core.database.dao.DayDao
import com.normno.travelassistant.core.database.dao.ForkDao
import com.normno.travelassistant.core.database.dao.PlaceDao
import com.normno.travelassistant.core.database.dao.StopDao
import com.normno.travelassistant.core.database.dao.TripDao
import com.normno.travelassistant.core.database.entity.DayEntity
import com.normno.travelassistant.core.database.entity.ForkEntity
import com.normno.travelassistant.core.database.entity.PlaceEntity
import com.normno.travelassistant.core.database.entity.StopEntity
import com.normno.travelassistant.core.database.entity.TripEntity

/**
 * Локальная база — источник правды. Приложение работает без сети полностью; сеть,
 * когда появится, будет писать сюда же, а не в обход.
 *
 * Схемы экспортируются в `core/database/schemas` и лежат в репозитории: по ним
 * `MigrationTestHelper` проверяет, что миграция с версии N на N+1 не теряет данные.
 * Поднимая `version`, добавляйте `Migration` и тест — автомиграции молча роняют
 * колонки, если переименование не описано явно.
 */
@Database(
    entities = [
        TripEntity::class,
        DayEntity::class,
        StopEntity::class,
        PlaceEntity::class,
        ForkEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun tripDao(): TripDao
    abstract fun dayDao(): DayDao
    abstract fun stopDao(): StopDao
    abstract fun placeDao(): PlaceDao
    abstract fun forkDao(): ForkDao

    companion object {
        const val NAME = "travel.db"
    }
}
