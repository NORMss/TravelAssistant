package com.normno.travelassistant.feature.today

import com.normno.travelassistant.core.model.Fork
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.Trip
import kotlin.time.Duration

/**
 * Состояние экрана «Сейчас» считается целиком во ViewModel и приходит готовым.
 *
 * Соблазн посчитать «текущий этап» прямо в composable велик — и это ошибка: расчёт
 * зависит от тикающего времени и от часового пояса места, а значит начнёт пересчитываться
 * на каждой перекомпозиции, и проверить его тестом будет нельзя.
 */
sealed interface TodayUiState {

    data object Loading : TodayUiState

    /** Активной поездки нет: либо их ещё нет вовсе, либо все уже прошли. */
    data class NoActiveTrip(val hasAnyTrip: Boolean) : TodayUiState

    data class Active(
        val trip: Trip,
        val day: ItineraryDay?,
        /** Этап, который идёт прямо сейчас. */
        val currentStop: Stop?,
        /** Следующий — он нужен даже когда текущий есть: до него считается отсчёт. */
        val nextStop: Stop?,
        val untilNext: Duration?,
        /** Развилки, относящиеся к текущему моменту, а не ко всему дню. */
        val relevantForks: List<Fork>,
        /** Часовой пояс места: подпись под временем, когда он отличается от системного. */
        val zoneId: String,
        val isPreview: Boolean = false,
    ) : TodayUiState
}
