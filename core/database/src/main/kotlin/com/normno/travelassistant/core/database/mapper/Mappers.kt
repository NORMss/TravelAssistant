package com.normno.travelassistant.core.database.mapper

import com.normno.travelassistant.core.database.entity.DayEntity
import com.normno.travelassistant.core.database.entity.ForkEntity
import com.normno.travelassistant.core.database.entity.PlaceEntity
import com.normno.travelassistant.core.database.entity.StopEntity
import com.normno.travelassistant.core.database.entity.TripEntity
import com.normno.travelassistant.core.database.entity.toColumns
import com.normno.travelassistant.core.database.entity.toModel
import com.normno.travelassistant.core.model.Address
import com.normno.travelassistant.core.model.DayId
import com.normno.travelassistant.core.model.Fork
import com.normno.travelassistant.core.model.ForkId
import com.normno.travelassistant.core.model.ForkOutcome
import com.normno.travelassistant.core.model.GeoPoint
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Money
import com.normno.travelassistant.core.model.Place
import com.normno.travelassistant.core.model.PlaceId
import com.normno.travelassistant.core.model.PlaceKind
import com.normno.travelassistant.core.model.PlaceSource
import com.normno.travelassistant.core.model.Reminder
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.StopCategory
import com.normno.travelassistant.core.model.StopId
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripId
import com.normno.travelassistant.core.model.TripStatus

/**
 * Перевод между таблицей и доменом руками, без рефлексии и без общего базового класса.
 *
 * Хочется сэкономить и отдавать entity прямо в UI — не стоит: тогда переименование
 * колонки правит экраны, а строковые `category`/`status`, нужные SQLite, расползаются
 * по всему приложению вместо enum. Цена — вот этот файл; он меняется редко.
 *
 * Неизвестное значение enum не роняет экран: план мог прийти из импорта или из версии
 * приложения новее установленной.
 */

/* ---------------- Trip ---------------- */

fun TripEntity.toModel(): Trip = Trip(
    id = TripId(id),
    title = title,
    subtitle = subtitle,
    startDate = startDate,
    endDate = endDate,
    destinations = destinations,
    homeZoneId = homeZoneId,
    homeCurrency = homeCurrency,
    travellers = travellers,
    coverUri = coverUri,
    status = enumOrDefault(status, TripStatus.DRAFT),
    notes = notes,
    sync = sync.toModel(),
)

fun Trip.toEntity(): TripEntity = TripEntity(
    id = id.value,
    title = title,
    subtitle = subtitle,
    startDate = startDate,
    endDate = endDate,
    destinations = destinations,
    travellers = travellers,
    homeZoneId = homeZoneId,
    homeCurrency = homeCurrency,
    coverUri = coverUri,
    status = status.name,
    notes = notes,
    sync = sync.toColumns(),
)

/* ---------------- Day ---------------- */

/**
 * День собирается уже с этапами и развилками: показывать день без них незачем,
 * а два отдельных запроса из UI дали бы мигание при вставке этапа.
 */
fun DayEntity.toModel(
    stops: List<Stop> = emptyList(),
    forks: List<Fork> = emptyList(),
): ItineraryDay = ItineraryDay(
    id = DayId(id),
    tripId = TripId(tripId),
    date = date,
    title = title,
    label = label,
    lede = lede,
    stops = stops,
    forks = forks,
    sync = sync.toModel(),
)

fun ItineraryDay.toEntity(): DayEntity = DayEntity(
    id = id.value,
    tripId = tripId.value,
    date = date,
    title = title,
    label = label,
    lede = lede,
    sync = sync.toColumns(),
)

/* ---------------- Stop ---------------- */

fun StopEntity.toModel(): Stop = Stop(
    id = StopId(id),
    tripId = TripId(tripId),
    dayId = DayId(dayId),
    orderIndex = orderIndex,
    title = title,
    localTitle = localTitle,
    transliteration = transliteration,
    startTime = startTime,
    durationMinutes = durationMinutes,
    category = enumOrDefault(category, StopCategory.OTHER),
    placeId = placeId?.let(::PlaceId),
    address = addressOrNull(
        local = addressLocal,
        display = addressDisplay,
        transliteration = addressTransliteration,
        lat = lat,
        lng = lng,
    ),
    note = note,
    tag = tag,
    estimatedCost = costMinor?.let { Money(it, costCurrency.orEmpty()) },
    bookingRef = bookingRef,
    url = url,
    reminder = reminderMinutesBefore?.let {
        Reminder(minutesBefore = it, critical = reminderCritical, text = reminderText)
    },
    done = done,
    sync = sync.toModel(),
)

fun Stop.toEntity(): StopEntity = StopEntity(
    id = id.value,
    tripId = tripId.value,
    dayId = dayId.value,
    orderIndex = orderIndex,
    title = title,
    localTitle = localTitle,
    transliteration = transliteration,
    startTime = startTime,
    durationMinutes = durationMinutes,
    category = category.name,
    placeId = placeId?.value,
    addressLocal = address?.local.orEmpty(),
    addressDisplay = address?.display.orEmpty(),
    addressTransliteration = address?.transliteration.orEmpty(),
    lat = address?.point?.lat,
    lng = address?.point?.lng,
    note = note,
    tag = tag,
    costMinor = estimatedCost?.minorUnits,
    costCurrency = estimatedCost?.currency,
    bookingRef = bookingRef,
    url = url,
    reminderMinutesBefore = reminder?.minutesBefore,
    reminderCritical = reminder?.critical ?: false,
    reminderText = reminder?.text.orEmpty(),
    done = done,
    sync = sync.toColumns(),
)

/* ---------------- Place ---------------- */

fun PlaceEntity.toModel(): Place = Place(
    id = PlaceId(id),
    tripId = tripId?.let(::TripId),
    name = name,
    localName = localName,
    transliteration = transliteration,
    address = addressOrNull(
        local = addressLocal,
        display = addressDisplay,
        transliteration = addressTransliteration,
        lat = lat,
        lng = lng,
    ) ?: Address(),
    kind = enumOrDefault(kind, PlaceKind.POINT),
    note = note,
    source = enumOrDefault(source, PlaceSource.USER),
    rating = rating,
    visited = visited,
    sync = sync.toModel(),
)

fun Place.toEntity(): PlaceEntity = PlaceEntity(
    id = id.value,
    tripId = tripId?.value,
    name = name,
    localName = localName,
    transliteration = transliteration,
    addressLocal = address.local,
    addressDisplay = address.display,
    addressTransliteration = address.transliteration,
    lat = address.point?.lat,
    lng = address.point?.lng,
    kind = kind.name,
    note = note,
    source = source.name,
    rating = rating,
    visited = visited,
    sync = sync.toColumns(),
)

/* ---------------- Fork ---------------- */

fun ForkEntity.toModel(): Fork = Fork(
    id = ForkId(id),
    tripId = TripId(tripId),
    dayId = DayId(dayId),
    question = question,
    localQuestion = localQuestion,
    options = options,
    stopIds = stopIds.map(::StopId),
    categories = categories.map { enumOrDefault(it, StopCategory.OTHER) },
    chosenKey = chosenKey,
    sync = sync.toModel(),
)

fun Fork.toEntity(): ForkEntity = ForkEntity(
    id = id.value,
    tripId = tripId.value,
    dayId = dayId.value,
    question = question,
    localQuestion = localQuestion,
    options = options,
    stopIds = stopIds.map { it.value },
    categories = categories.map { it.name },
    chosenKey = chosenKey,
    sync = sync.toColumns(),
)

/** Цвет ветки развилки определяется её исходом, а не словом в тексте. */
fun ForkOutcome.isBlocking(): Boolean = this == ForkOutcome.CANCEL

/* ---------------- helpers ---------------- */

private inline fun <reified T : Enum<T>> enumOrDefault(raw: String, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == raw } ?: fallback

private fun addressOrNull(
    local: String,
    display: String,
    transliteration: String,
    lat: Double?,
    lng: Double?,
): Address? {
    val point = if (lat != null && lng != null) {
        runCatching { GeoPoint(lat, lng) }.getOrNull()
    } else {
        null
    }
    if (local.isBlank() && display.isBlank() && point == null) return null
    return Address(
        local = local,
        display = display,
        transliteration = transliteration,
        point = point,
    )
}
