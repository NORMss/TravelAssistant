package com.normno.travelassistant.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.normno.travelassistant.core.model.Destination
import com.normno.travelassistant.core.model.ForkOption
import com.normno.travelassistant.core.model.Traveller
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Где граница между отдельной таблицей и JSON-колонкой.
 *
 * Отдельная таблица — если список фильтруют, сортируют или правят поштучно: дни,
 * этапы, места. По ним идут запросы, и каждая правка должна быть атомарной.
 *
 * JSON-колонка — если список всегда грузится вместе с родителем, ограничен по длине
 * и не является предметом запроса: пункты назначения и участники поездки, варианты
 * развилки. Таблица на три строки, которую всегда читают целиком, даёт только join.
 */
@Entity(
    tableName = "trips",
    indices = [Index("status"), Index("start_date")],
)
data class TripEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String = "",
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "end_date") val endDate: LocalDate,
    val destinations: List<Destination> = emptyList(),
    val travellers: List<Traveller> = emptyList(),
    @ColumnInfo(name = "home_zone_id") val homeZoneId: String = "UTC",
    @ColumnInfo(name = "home_currency") val homeCurrency: String = "RUB",
    @ColumnInfo(name = "cover_uri") val coverUri: String? = null,
    val status: String,
    val notes: String = "",
    @Embedded val sync: SyncColumns = SyncColumns(),
)

@Entity(
    tableName = "days",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("trip_id"), Index(value = ["trip_id", "date"], unique = true)],
)
data class DayEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    val date: LocalDate,
    val title: String = "",
    val label: String = "",
    val lede: String = "",
    @Embedded val sync: SyncColumns = SyncColumns(),
)

@Entity(
    tableName = "stops",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["id"],
            childColumns = ["day_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("day_id"), Index("trip_id"), Index("place_id")],
)
data class StopEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    @ColumnInfo(name = "day_id") val dayId: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val title: String,
    @ColumnInfo(name = "local_title") val localTitle: String = "",
    val transliteration: String = "",
    @ColumnInfo(name = "start_time") val startTime: LocalTime? = null,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int? = null,
    val category: String,
    @ColumnInfo(name = "place_id") val placeId: String? = null,
    @ColumnInfo(name = "address_local") val addressLocal: String = "",
    @ColumnInfo(name = "address_display") val addressDisplay: String = "",
    @ColumnInfo(name = "address_transliteration") val addressTransliteration: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val note: String = "",
    val tag: String = "",
    @ColumnInfo(name = "cost_minor") val costMinor: Long? = null,
    @ColumnInfo(name = "cost_currency") val costCurrency: String? = null,
    @ColumnInfo(name = "booking_ref") val bookingRef: String = "",
    val url: String = "",
    @ColumnInfo(name = "reminder_minutes_before") val reminderMinutesBefore: Int? = null,
    @ColumnInfo(name = "reminder_critical", defaultValue = "0") val reminderCritical: Boolean = false,
    @ColumnInfo(name = "reminder_text") val reminderText: String = "",
    @ColumnInfo(defaultValue = "0") val done: Boolean = false,
    @Embedded val sync: SyncColumns = SyncColumns(),
)

@Entity(
    tableName = "places",
    indices = [Index("trip_id"), Index("kind")],
)
data class PlaceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String? = null,
    val name: String,
    @ColumnInfo(name = "local_name") val localName: String = "",
    val transliteration: String = "",
    @ColumnInfo(name = "address_local") val addressLocal: String = "",
    @ColumnInfo(name = "address_display") val addressDisplay: String = "",
    @ColumnInfo(name = "address_transliteration") val addressTransliteration: String = "",
    val lat: Double? = null,
    val lng: Double? = null,
    val kind: String,
    val note: String = "",
    val source: String,
    val rating: Int? = null,
    @ColumnInfo(defaultValue = "0") val visited: Boolean = false,
    @Embedded val sync: SyncColumns = SyncColumns(),
)

@Entity(
    tableName = "forks",
    foreignKeys = [
        ForeignKey(
            entity = DayEntity::class,
            parentColumns = ["id"],
            childColumns = ["day_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("day_id"), Index("trip_id")],
)
data class ForkEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    @ColumnInfo(name = "day_id") val dayId: String,
    val question: String,
    @ColumnInfo(name = "local_question") val localQuestion: String = "",
    val options: List<ForkOption> = emptyList(),
    @ColumnInfo(name = "stop_ids") val stopIds: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    /** Выбор пользователя. Это пометка для своих — план развилка не меняет. */
    @ColumnInfo(name = "chosen_key") val chosenKey: String? = null,
    @Embedded val sync: SyncColumns = SyncColumns(),
)
