package com.normno.travelassistant.feature.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.normno.travelassistant.core.common.time.Formatting
import com.normno.travelassistant.core.designsystem.component.ActionChip
import com.normno.travelassistant.core.designsystem.component.EmptyState
import com.normno.travelassistant.core.designsystem.component.FloatingCard
import com.normno.travelassistant.core.designsystem.component.ShapedSurface
import com.normno.travelassistant.core.designsystem.component.TravelLoadingIndicator
import com.normno.travelassistant.core.designsystem.theme.LocalScriptMedium
import com.normno.travelassistant.core.designsystem.theme.TravelShapeTokens
import com.normno.travelassistant.core.model.Stop
import com.normno.travelassistant.core.ui.AddressActions
import com.normno.travelassistant.core.ui.StopRow

/**
 * «Сейчас» — экран, который открывают по нескольку раз в день: что идёт прямо сейчас,
 * сколько осталось до следующего, и адрес в одно касание.
 */
@Composable
fun TodayScreen(
    onOpenTrips: () -> Unit,
    onOpenStop: (Stop) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: TodayViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        TodayUiState.Loading -> TravelLoadingIndicator(modifier = modifier.fillMaxSize())

        is TodayUiState.NoActiveTrip -> EmptyState(
            icon = Icons.Rounded.Explore,
            title = "Сейчас никуда не едем",
            description = "Экран «Сейчас» оживёт в первый день поездки: покажет " +
                "текущий этап, обратный отсчёт и адрес для таксиста.",
            actionLabel = "К поездкам",
            onAction = onOpenTrips,
            modifier = modifier,
        )

        is TodayUiState.Active -> ActiveTripContent(
            state = current,
            onOpenStop = onOpenStop,
            modifier = modifier,
            contentPadding = contentPadding,
        )
    }
}

@Composable
private fun ActiveTripContent(
    state: TodayUiState.Active,
    onOpenStop: (Stop) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = state.trip.title,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = state.day?.label?.ifBlank { null }
                    ?: state.day?.title?.ifBlank { null }
                    ?: "День без плана",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        val stop = state.currentStop
        if (stop != null) {
            CurrentStopCard(
                stop = stop,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (state.untilNext != null && state.nextStop != null) {
            FloatingCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Schedule, contentDescription = null)
                    Column {
                        Text(
                            text = "Через ${Formatting.duration(state.untilNext)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = state.nextStop.displayTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        val rest = state.day?.stops.orEmpty()
            .sortedBy { it.orderIndex }
            .filter { it.id != state.currentStop?.id }

        if (rest.isEmpty() && state.currentStop == null) {
            EmptyState(
                icon = Icons.Rounded.Explore,
                title = "На сегодня план пуст",
                description = "Свободный день — это тоже решение. Если нет, " +
                    "добавьте этап или попросите ассистента предложить вариант.",
            )
        } else {
            rest.forEach { item ->
                StopRow(
                    stop = item,
                    onClick = { onOpenStop(item) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

/**
 * Текущий этап — единственный элемент такой формы и такого размера на экране.
 * Найти его взглядом нужно за полсекунды, не читая.
 */
@Composable
private fun CurrentStopCard(stop: Stop, modifier: Modifier = Modifier) {
    FloatingCard(modifier = modifier.fillMaxWidth(), elevation = 6.dp) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShapedSurface(
                    polygon = TravelShapeTokens.activeStop,
                    modifier = Modifier.size(48.dp),
                ) {
                    Text(
                        text = stop.startTime?.let(Formatting::time) ?: "сейчас",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                Column {
                    Text(
                        text = stop.displayTitle,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (stop.transliteration.isNotBlank()) {
                        Text(
                            text = stop.transliteration,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Местное написание показывают собеседнику — отсюда свой кегль.
            if (stop.localTitle.isNotBlank()) {
                Text(text = stop.localTitle, style = LocalScriptMedium)
            }

            if (stop.note.isNotBlank()) {
                Text(
                    text = stop.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            val address = stop.address
            if (address != null && !address.isEmpty) {
                AddressActions(address = address)
            } else if (stop.tag.isNotBlank()) {
                ActionChip(
                    label = stop.tag,
                    icon = Icons.Rounded.Schedule,
                    onClick = {},
                    enabled = false,
                )
            }
        }
    }
}
