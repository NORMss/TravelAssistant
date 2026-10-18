package com.normno.travelassistant.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Настройки читаются потоком, а не однократно: тема и «цвета по системе» меняются на
 * ходу, и экран должен перекраситься без перезапуска.
 */
@Singleton
class SettingsRepository @Inject constructor(
    private val store: DataStore<Preferences>,
) {
    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            theme = prefs[Keys.THEME]
                ?.let { raw -> ThemePreference.entries.firstOrNull { it.name == raw } }
                ?: ThemePreference.SYSTEM,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            remindersEnabled = prefs[Keys.REMINDERS] ?: true,
            llmProviderId = prefs[Keys.LLM_PROVIDER].orEmpty(),
            llmModel = prefs[Keys.LLM_MODEL].orEmpty(),
            previewInstantMillis = prefs[Keys.PREVIEW_INSTANT],
            lastOpenedTripId = prefs[Keys.LAST_TRIP],
        )
    }

    suspend fun setTheme(value: ThemePreference) = put(Keys.THEME, value.name)

    suspend fun setDynamicColor(value: Boolean) = put(Keys.DYNAMIC_COLOR, value)

    suspend fun setRemindersEnabled(value: Boolean) = put(Keys.REMINDERS, value)

    suspend fun setLlm(providerId: String, model: String) {
        store.edit { prefs ->
            prefs[Keys.LLM_PROVIDER] = providerId
            prefs[Keys.LLM_MODEL] = model
        }
    }

    suspend fun setPreviewInstant(millis: Long?) {
        store.edit { prefs ->
            if (millis == null) prefs.remove(Keys.PREVIEW_INSTANT)
            else prefs[Keys.PREVIEW_INSTANT] = millis
        }
    }

    suspend fun setLastOpenedTrip(tripId: String?) {
        store.edit { prefs ->
            if (tripId == null) prefs.remove(Keys.LAST_TRIP)
            else prefs[Keys.LAST_TRIP] = tripId
        }
    }

    private suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        store.edit { it[key] = value }
    }

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val REMINDERS = booleanPreferencesKey("reminders_enabled")
        val LLM_PROVIDER = stringPreferencesKey("llm_provider")
        val LLM_MODEL = stringPreferencesKey("llm_model")
        val PREVIEW_INSTANT = longPreferencesKey("preview_instant")
        val LAST_TRIP = stringPreferencesKey("last_trip_id")
    }
}
