package com.normno.travelassistant.core.datastore

import kotlinx.serialization.Serializable

/**
 * Настройки приложения. Намеренно короткий список: каждый переключатель — это ветка
 * поведения, которую потом надо держать работающей.
 */
@Serializable
data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    /**
     * Цвета по системе. По умолчанию включено: на Android 12+ палитру задают обои,
     * и это основной вид приложения, а не опция.
     */
    val dynamicColor: Boolean = true,
    val remindersEnabled: Boolean = true,
    /** Выбранный провайдер модели. Пусто — ассистент работает в режиме обмена текстом. */
    val llmProviderId: String = "",
    val llmModel: String = "",
    /**
     * «Предпросмотр времени»: приложение показывает себя таким, каким будет в этот
     * момент поездки. Проверить план до вылета иначе невозможно.
     */
    val previewInstantMillis: Long? = null,
    val lastOpenedTripId: String? = null,
)

enum class ThemePreference { SYSTEM, LIGHT, DARK }
