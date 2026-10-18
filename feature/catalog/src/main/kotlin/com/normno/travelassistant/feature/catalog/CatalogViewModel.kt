package com.normno.travelassistant.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.normno.travelassistant.core.common.result.Outcome
import com.normno.travelassistant.core.common.result.asOutcome
import com.normno.travelassistant.core.common.time.TripClock
import com.normno.travelassistant.core.data.repository.TemplateRepository
import com.normno.travelassistant.core.model.TemplateSummary
import com.normno.travelassistant.core.model.TripId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val templates: TemplateRepository,
    private val clock: TripClock,
) : ViewModel() {

    val state: StateFlow<Outcome<List<TemplateSummary>>> = templates.observeCatalog()
        .asOutcome()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Outcome.Loading,
        )

    /**
     * Применяет маршрут к новой поездке.
     *
     * Дата по умолчанию — через две недели, а не сегодня: маршрут на шесть дней,
     * начинающийся сегодня, почти наверняка придётся переставлять, а поправить
     * дату в редакторе — одно действие против шести.
     */
    fun apply(summary: TemplateSummary, startDate: LocalDate? = null, onDone: (TripId) -> Unit) {
        viewModelScope.launch {
            val template = templates.load(summary.id) ?: return@launch
            val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val start = startDate ?: today.plus(14, DateTimeUnit.DAY)
            onDone(templates.applyToNewTrip(template, start))
        }
    }
}
