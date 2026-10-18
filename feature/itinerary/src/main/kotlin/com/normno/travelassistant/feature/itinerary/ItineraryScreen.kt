package com.normno.travelassistant.feature.itinerary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.normno.travelassistant.core.common.time.Formatting
import com.normno.travelassistant.core.designsystem.component.ChipOption
import com.normno.travelassistant.core.designsystem.component.ChipRow
import com.normno.travelassistant.core.designsystem.component.EmptyState
import com.normno.travelassistant.core.model.DayId
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.model.TripId
import com.normno.travelassistant.core.ui.StopRow

/**
 * План поездки. Дни — лентой чипов сверху: при любой длине поездки они остаются
 * видимы одним движением, а выбранный день не прячется в выпадающем списке.
 */
@Composable
fun ItineraryScreen(
    tripId: TripId,
    onOpenStop: (Stop) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: ItineraryViewModel = hiltViewModel(),
) {
    LaunchedEffect(tripId) { viewModel.open(tripId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.days.isNotEmpty()) {
            ChipRow(
                options = state.days.map { day ->
                    ChipOption(
                        key = day.id,
                        label = day.label.ifBlank { Formatting.dayMonth(day.date) },
                        count = day.stops.size.takeIf { it > 0 },
                    )
                },
                selectedKey = state.selectedDay?.id,
                onSelect = { key -> viewModel.selectDay(key as DayId) },
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        val stops = state.selectedDay?.stops?.sortedBy { it.orderIndex }.orEmpty()
        if (stops.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.EditCalendar,
                title = if (state.days.isEmpty()) "План ещё пуст" else "В этом дне нет этапов",
                description = "Добавьте этап кнопкой внизу — или отдайте день ассистенту " +
                    "и примените то, что он предложит.",
            )
        } else {
            LazyColumn(
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = stops, key = { it.id.value }) { stop ->
                    StopRow(stop = stop, onClick = { onOpenStop(stop) })
                }
            }
        }
    }
}
