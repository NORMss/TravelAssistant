package com.normno.travelassistant.core.database

import androidx.room.TypeConverter
import com.normno.travelassistant.core.model.Destination
import com.normno.travelassistant.core.model.ForkOption
import com.normno.travelassistant.core.model.Traveller
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.json.Json

/**
 * Даты и время хранятся строками ISO, а не числами: такая колонка сортируется
 * лексикографически в правильном порядке, читается глазами в инспекторе базы и не
 * зависит от часового пояса устройства в момент записи.
 */
internal val DatabaseJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

class Converters {

    @TypeConverter fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter fun stringToLocalDate(value: String?): LocalDate? =
        value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    @TypeConverter fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter fun stringToLocalTime(value: String?): LocalTime? =
        value?.let { runCatching { LocalTime.parse(it) }.getOrNull() }

    @TypeConverter fun destinationsToJson(value: List<Destination>): String =
        DatabaseJson.encodeToString(value)

    @TypeConverter fun jsonToDestinations(value: String): List<Destination> =
        runCatching { DatabaseJson.decodeFromString<List<Destination>>(value) }
            .getOrDefault(emptyList())

    @TypeConverter fun travellersToJson(value: List<Traveller>): String =
        DatabaseJson.encodeToString(value)

    @TypeConverter fun jsonToTravellers(value: String): List<Traveller> =
        runCatching { DatabaseJson.decodeFromString<List<Traveller>>(value) }
            .getOrDefault(emptyList())

    @TypeConverter fun forkOptionsToJson(value: List<ForkOption>): String =
        DatabaseJson.encodeToString(value)

    @TypeConverter fun jsonToForkOptions(value: String): List<ForkOption> =
        runCatching { DatabaseJson.decodeFromString<List<ForkOption>>(value) }
            .getOrDefault(emptyList())

    @TypeConverter fun stringListToJson(value: List<String>): String =
        DatabaseJson.encodeToString(value)

    @TypeConverter fun jsonToStringList(value: String): List<String> =
        runCatching { DatabaseJson.decodeFromString<List<String>>(value) }
            .getOrDefault(emptyList())
}
