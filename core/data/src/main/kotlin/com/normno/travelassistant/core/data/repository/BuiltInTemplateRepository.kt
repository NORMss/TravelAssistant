package com.normno.travelassistant.core.data.repository

import android.content.Context
import com.normno.travelassistant.core.common.di.IoDispatcher
import com.normno.travelassistant.core.common.newId
import com.normno.travelassistant.core.common.time.TripClock
import com.normno.travelassistant.core.database.dao.DayDao
import com.normno.travelassistant.core.database.dao.ForkDao
import com.normno.travelassistant.core.database.dao.StopDao
import com.normno.travelassistant.core.database.mapper.toEntity
import com.normno.travelassistant.core.model.DayId
import com.normno.travelassistant.core.model.Fork
import com.normno.travelassistant.core.model.ForkId
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.StopId
import com.normno.travelassistant.core.model.SyncMeta
import com.normno.travelassistant.core.model.TemplateSource
import com.normno.travelassistant.core.model.TemplateStop
import com.normno.travelassistant.core.model.TemplateSummary
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripId
import com.normno.travelassistant.core.model.TripStatus
import com.normno.travelassistant.core.model.TripTemplate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Шаблоны, лежащие в assets. Скачанные добавятся сюда же в фазе 6 — интерфейс
 * для экрана при этом не изменится.
 */
@Singleton
class BuiltInTemplateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tripDao: com.normno.travelassistant.core.database.dao.TripDao,
    private val dayDao: DayDao,
    private val stopDao: StopDao,
    private val forkDao: ForkDao,
    private val clock: TripClock,
    @IoDispatcher private val io: CoroutineDispatcher,
) : TemplateRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun observeCatalog(): Flow<List<TemplateSummary>> = flow {
        emit(withContext(io) { readAll().map { it.toSummary() } })
    }

    override suspend fun load(id: String): TripTemplate? = withContext(io) {
        readAll().firstOrNull { it.id == id }
    }

    override suspend fun applyToNewTrip(
        template: TripTemplate,
        startDate: LocalDate,
        title: String?,
    ): TripId = withContext(io) {
        val now = clock.now().toEpochMilliseconds()
        val tripId = TripId(newId())

        val trip = Trip(
            id = tripId,
            title = title ?: template.title,
            subtitle = template.summary,
            startDate = startDate,
            endDate = startDate.plus(
                (template.dayCount - 1).coerceAtLeast(0),
                DateTimeUnit.DAY,
            ),
            destinations = template.destinations,
            homeZoneId = kotlinx.datetime.TimeZone.currentSystemDefault().id,
            status = TripStatus.PLANNED,
            sync = SyncMeta.touched(now),
        )
        tripDao.upsert(trip.toEntity())

        // Ключи шаблона меняются на свежие id: иначе два применения одного
        // шаблона столкнутся первичными ключами, а синхронизация решит, что это
        // одна и та же запись на двух телефонах.
        val dayIds = mutableMapOf<Int, DayId>()
        val stopIds = mutableMapOf<String, StopId>()

        val days = template.days.map { templateDay ->
            val dayId = DayId(newId())
            dayIds[templateDay.dayIndex] = dayId
            ItineraryDay(
                id = dayId,
                tripId = tripId,
                date = startDate.plus(templateDay.dayIndex - 1, DateTimeUnit.DAY),
                title = templateDay.title,
                label = templateDay.label,
                lede = templateDay.summary,
                sync = SyncMeta.touched(now),
            )
        }
        dayDao.upsertAll(days.map { it.toEntity() })

        val stops = template.days.flatMap { templateDay ->
            val dayId = dayIds.getValue(templateDay.dayIndex)
            templateDay.stops.map { templateStop ->
                val stopId = StopId(newId())
                stopIds[templateStop.key] = stopId
                templateStop.toStop(stopId, tripId, dayId, now)
            }
        }
        stopDao.upsertAll(stops.map { it.toEntity() })

        val forks = template.days.flatMap { templateDay ->
            val dayId = dayIds.getValue(templateDay.dayIndex)
            templateDay.forks.map { templateFork ->
                Fork(
                    id = ForkId(newId()),
                    tripId = tripId,
                    dayId = dayId,
                    question = templateFork.question,
                    localQuestion = templateFork.localQuestion,
                    options = templateFork.options,
                    // Развилка, чей этап не доехал до поездки, просто теряет привязку
                    // и показывается на весь день — это лучше, чем ссылка в пустоту.
                    stopIds = templateFork.stopKeys.mapNotNull { stopIds[it] },
                    categories = templateFork.categories,
                    sync = SyncMeta.touched(now),
                )
            }
        }
        forkDao.upsertAll(forks.map { it.toEntity() })

        tripId
    }

    private fun readAll(): List<TripTemplate> {
        val names = runCatching { context.assets.list(ASSET_DIR) }.getOrNull().orEmpty()
        return names.filter { it.endsWith(".json") }.mapNotNull { name ->
            runCatching {
                context.assets.open("$ASSET_DIR/$name").bufferedReader().use { reader ->
                    json.decodeFromString<TripTemplate>(reader.readText())
                }
            }.getOrNull()
        }
    }

    private companion object {
        const val ASSET_DIR = "templates"
    }
}

private fun TripTemplate.toSummary() = TemplateSummary(
    id = id,
    title = title,
    summary = summary,
    countryCodes = destinations.map { it.countryCode },
    dayCount = dayCount,
    stopCount = stopCount,
    author = author,
    tags = tags,
    source = TemplateSource.BUILT_IN,
    regionPackIds = regionPackIds,
)

private fun TemplateStop.toStop(
    id: StopId,
    tripId: TripId,
    dayId: DayId,
    now: Long,
) = Stop(
    id = id,
    tripId = tripId,
    dayId = dayId,
    orderIndex = orderIndex,
    title = title,
    localTitle = localTitle,
    transliteration = transliteration,
    startTime = startTime.takeIf { it.isNotBlank() }
        ?.let { runCatching { LocalTime.parse(it) }.getOrNull() },
    durationMinutes = durationMinutes,
    category = category,
    address = address,
    // Подсказка автора шаблона становится заметкой поездки: это единственный
    // авторский текст, который переносится, и он помечен как цитата.
    note = tip,
    tag = tag,
    sync = SyncMeta.touched(now),
)
