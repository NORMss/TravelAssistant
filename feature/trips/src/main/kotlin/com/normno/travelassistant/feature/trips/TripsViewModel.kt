package com.normno.travelassistant.feature.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.normno.travelassistant.core.common.newId
import com.normno.travelassistant.core.common.result.Outcome
import com.normno.travelassistant.core.common.result.asOutcome
import com.normno.travelassistant.core.common.time.TripClock
import com.normno.travelassistant.core.data.repository.TripRepository
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripId
import com.normno.travelassistant.core.model.TripStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repository: TripRepository,
    private val clock: TripClock,
) : ViewModel() {

    /**
     * `WhileSubscribed(5_000)` вместо `Eagerly`: при повороте экрана подписка
     * пропадает на миллисекунды, и пересоздавать запрос к базе из-за этого не нужно.
     */
    val state: StateFlow<Outcome<List<Trip>>> = repository.observeTrips()
        .asOutcome()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Outcome.Loading,
        )

    /**
     * Создаёт черновик и сразу возвращает его id: пользователь попадает в редактор, а
     * не в форму «заполните семь полей, чтобы начать». Даты можно поправить потом —
     * у черновика нет требований к полноте.
     */
    fun createDraft(onCreated: (TripId) -> Unit) {
        viewModelScope.launch {
            val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val trip = Trip(
                id = TripId(newId()),
                title = "Новая поездка",
                startDate = today,
                endDate = today.plus(6, DateTimeUnit.DAY),
                homeZoneId = TimeZone.currentSystemDefault().id,
                status = TripStatus.DRAFT,
            )
            repository.save(trip)
            onCreated(trip.id)
        }
    }
}
