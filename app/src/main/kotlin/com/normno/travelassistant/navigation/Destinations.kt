package com.normno.travelassistant.navigation

import androidx.navigation3.runtime.NavKey
import com.normno.travelassistant.core.model.TripId
import kotlinx.serialization.Serializable

/**
 * Пункты навигации — сериализуемые объекты, а не строки с параметрами.
 *
 * В Navigation 3 back stack принадлежит приложению: это обычный список, который можно
 * прочитать, изменить и сохранить. Отсюда требование `@Serializable` — иначе стек не
 * переживёт убийство процесса, и пользователь, вернувшись в приложение из мессенджера,
 * окажется на первом экране.
 */
@Serializable data object TodayRoute : NavKey

@Serializable data object TripsRoute : NavKey

@Serializable data class ItineraryRoute(val tripId: String) : NavKey {
    val id: TripId get() = TripId(tripId)
}

@Serializable data object CatalogRoute : NavKey

@Serializable data object CameraRoute : NavKey

@Serializable data object PlacesRoute : NavKey

@Serializable data object PhrasebookRoute : NavKey

@Serializable data object AssistantRoute : NavKey

@Serializable data object SettingsRoute : NavKey
