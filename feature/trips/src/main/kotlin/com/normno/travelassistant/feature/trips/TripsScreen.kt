package com.normno.travelassistant.feature.trips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.normno.travelassistant.core.common.result.Outcome
import com.normno.travelassistant.core.designsystem.component.EmptyState
import com.normno.travelassistant.core.designsystem.component.TravelLoadingIndicator
import com.normno.travelassistant.core.model.TripId
import com.normno.travelassistant.core.ui.TripCard

/**
 * Список поездок — вход в приложение, когда активной поездки нет.
 *
 * @param contentPadding приходит от плавающей навигации: она висит над контентом, и
 *   последний элемент списка иначе окажется под ней
 */
@Composable
fun TripsScreen(
    onOpenTrip: (TripId) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: TripsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        Outcome.Loading -> TravelLoadingIndicator(
            modifier = modifier.fillMaxSize(),
            label = "Открываем поездки",
        )

        is Outcome.Failure -> EmptyState(
            icon = Icons.Rounded.Luggage,
            title = "Не удалось прочитать поездки",
            description = current.message ?: current.cause.message.orEmpty(),
            modifier = modifier,
        )

        is Outcome.Success -> if (current.data.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Luggage,
                title = "Пока ни одной поездки",
                description = "Создайте поездку — дальше план можно собрать руками " +
                    "или надиктовать ассистенту.",
                actionLabel = "Создать поездку",
                onAction = { viewModel.createDraft(onOpenTrip) },
                modifier = modifier,
            )
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = current.data, key = { it.id.value }) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { onOpenTrip(trip.id) },
                        modifier = Modifier,
                    )
                }
            }
        }
    }
}
