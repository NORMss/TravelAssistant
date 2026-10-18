package com.normno.travelassistant.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.normno.travelassistant.core.common.time.Formatting
import com.normno.travelassistant.core.designsystem.component.FloatingCard
import com.normno.travelassistant.core.designsystem.component.ShapedSurface
import com.normno.travelassistant.core.designsystem.theme.TravelShapeTokens
import com.normno.travelassistant.core.model.Trip
import com.normno.travelassistant.core.model.TripStatus
import androidx.compose.material3.Icon

/**
 * Карточка поездки в списке. Главное на ней — не название, а когда и куда:
 * список открывают, чтобы попасть в нужную поездку, а не чтобы читать заголовки.
 */
@Composable
fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapedSurface(
                polygon = TravelShapeTokens.cover,
                modifier = Modifier.size(56.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Flight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(26.dp).align(Alignment.Center),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = trip.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = dateRange(trip),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val where = trip.destinations.joinToString(" · ") { it.name }
                if (where.isNotBlank()) {
                    Text(
                        text = where,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            TripStatusChip(trip.status)
        }
    }
}

private fun dateRange(trip: Trip): String {
    val nights = trip.nightCount
    val range = "${Formatting.dayMonth(trip.startDate)} — ${Formatting.dayMonth(trip.endDate)}"
    return if (nights > 0) "$range · ${nights} ноч." else range
}

@Composable
private fun TripStatusChip(status: TripStatus) {
    val label = when (status) {
        TripStatus.DRAFT -> "черновик"
        TripStatus.PLANNED -> "скоро"
        TripStatus.ACTIVE -> "сейчас"
        TripStatus.FINISHED -> "прошла"
        TripStatus.ARCHIVED -> "в архиве"
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = if (status == TripStatus.ACTIVE) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}
