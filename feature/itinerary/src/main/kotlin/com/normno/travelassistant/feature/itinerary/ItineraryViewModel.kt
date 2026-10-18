package com.normno.travelassistant.feature.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.normno.travelassistant.core.data.repository.ItineraryRepository
import com.normno.travelassistant.core.data.repository.TripRepository
import com.normno.travelassistant.core.model.DayId
import com.normno.travelassistant.core.model.ItineraryDay
import com.normno.travelassistant.core.model.StopId
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItineraryUiState(
    val trip: Trip? = null,
    val days: List<ItineraryDay> = emptyList(),
    val selectedDayId: DayId? = null,
) {
    /** Пока день не выбран руками, показывается первый: пустой экран плана бесполезен. */
    val selectedDay: ItineraryDay?
        get() = days.firstOrNull { it.id == selectedDayId } ?: days.firstOrNull()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ItineraryViewModel @Inject constructor(
    private val trips: TripRepository,
    private val itinerary: ItineraryRepository,
) : ViewModel() {

    private val tripId = MutableStateFlow<TripId?>(null)
    private val selectedDay = MutableStateFlow<DayId?>(null)

    /**
     * `flatMapLatest`, а не вложенный `collect`: при переключении поездки подписка на
     * предыдущую должна умереть. Вложенный collect её бы оставил, и экран начал бы
     * получать обновления от поездки, которую уже закрыли.
     */
    val state: StateFlow<ItineraryUiState> = combine(
        tripId.flatMapLatest { id ->
            if (id == null) flowOf(null) else trips.observeTrip(id)
        },
        tripId.flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else itinerary.observeDays(id)
        },
        selectedDay,
    ) { trip, days, dayId ->
        ItineraryUiState(trip = trip, days = days, selectedDayId = dayId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ItineraryUiState(),
    )

    fun open(id: TripId) {
        if (tripId.value != id) {
            tripId.value = id
            selectedDay.value = null
        }
    }

    fun selectDay(id: DayId) {
        selectedDay.value = id
    }

    fun toggleDone(stopId: StopId, done: Boolean) {
        viewModelScope.launch { itinerary.setStopDone(stopId, done) }
    }
}
