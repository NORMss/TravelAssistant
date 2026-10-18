package com.normno.travelassistant.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.normno.travelassistant.core.common.time.TripClock
import com.normno.travelassistant.core.data.repository.ItineraryRepository
import com.normno.travelassistant.core.data.repository.TripRepository
import com.normno.travelassistant.core.model.Fork
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.StopId
import com.normno.travelassistant.core.model.Trip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import javax.inject.Inject
import kotlin.time.Duration

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val trips: TripRepository,
    private val itinerary: ItineraryRepository,
    private val clock: TripClock,
) : ViewModel() {

    /**
     * Секундный тик входит в состояние наравне с данными: обратный отсчёт до
     * следующего этапа — это и есть главное, за чем открывают этот экран.
     */
    val state: StateFlow<TodayUiState> = trips.observeActiveTrip()
        .flatMapLatest { trip ->
            if (trip == null) {
                flowOf<TodayUiState>(TodayUiState.NoActiveTrip(hasAnyTrip = false))
            } else {
                combine(
                    itinerary.observeDays(trip.id),
                    clock.ticker(),
                ) { days, _ -> buildState(trip, days) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState.Loading,
        )

    fun toggleDone(stopId: StopId, done: Boolean) {
        viewModelScope.launch { itinerary.setStopDone(stopId, done) }
    }

    fun chooseFork(fork: Fork, optionKey: String?) {
        viewModelScope.launch { itinerary.chooseFork(fork.id, optionKey) }
    }

    private fun buildState(trip: Trip, days: List<ItineraryDay>): TodayUiState {
        val zoneId = trip.zoneIdOn(
            // Дата берётся в поясе первого пункта: до вылета пояс места ещё не наступил,
            // но план уже записан по нему.
            clock.nowIn(trip.destinations.firstOrNull()?.zoneId ?: trip.homeZoneId).date,
        )
        val localNow = clock.nowIn(zoneId)
        val day = days.firstOrNull { it.date == localNow.date }
        val stops = day?.stops?.sortedBy { it.orderIndex }.orEmpty()

        val current = stops.lastOrNull { it.startsAtOrBefore(localNow) }
        val next = stops.firstOrNull { it.startsAfter(localNow) }

        val untilNext = next?.startTime?.let { time ->
            val zone = TripClock.zoneOf(zoneId)
            LocalDateTime(localNow.date, time).toInstant(zone) - clock.now()
        }?.takeIf { it > Duration.ZERO }

        return TodayUiState.Active(
            trip = trip,
            day = day,
            currentStop = current,
            nextStop = next,
            untilNext = untilNext,
            relevantForks = day?.forks.orEmpty().filter { it.isRelevantTo(current) },
            zoneId = zoneId,
        )
    }
}

private fun Stop.startsAtOrBefore(moment: LocalDateTime): Boolean {
    val time = startTime ?: return false
    return time <= moment.time
}

private fun Stop.startsAfter(moment: LocalDateTime): Boolean {
    val time = startTime ?: return false
    return time > moment.time
}

/**
 * Развилка показывается у этапа, который она решает, а не списком на весь день:
 * «в горах дождь?» имеет смысл перед подъёмом и не имеет — за ужином.
 */
private fun Fork.isRelevantTo(stop: Stop?): Boolean {
    if (stop == null) return false
    if (stopIds.contains(stop.id)) return true
    return categories.contains(stop.category)
}
