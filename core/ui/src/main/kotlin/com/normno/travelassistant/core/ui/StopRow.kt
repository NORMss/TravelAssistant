package com.normno.travelassistant.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.normno.travelassistant.core.common.time.Formatting
import com.normno.travelassistant.core.designsystem.component.FloatingCard
import com.normno.travelassistant.core.model.Stop

/**
 * Этап в таймлайне дня. Время слева фиксированной колонкой — так дни разного
 * наполнения выглядят одним списком, и глаз находит «где я» по вертикали, а не по
 * чтению заголовков.
 */
@Composable
fun StopRow(
    stop: Stop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingCard(modifier = modifier.fillMaxWidth(), onClick = onClick, elevation = 1.dp) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stop.startTime?.let(Formatting::time) ?: "—",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(52.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stop.displayTitle,
                    style = MaterialTheme.typography.titleSmall,
                    textDecoration = if (stop.done) TextDecoration.LineThrough else null,
                )
                if (stop.localTitle.isNotBlank() && stop.localTitle != stop.displayTitle) {
                    Text(
                        text = stop.localTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (stop.tag.isNotBlank()) {
                    Text(
                        text = stop.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}
