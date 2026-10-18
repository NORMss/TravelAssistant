package com.normno.travelassistant.feature.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Download
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
import com.normno.travelassistant.core.common.result.Outcome
import com.normno.travelassistant.core.designsystem.component.ActionChip
import com.normno.travelassistant.core.designsystem.component.EmptyState
import com.normno.travelassistant.core.designsystem.component.FloatingCard
import com.normno.travelassistant.core.designsystem.component.ShapedSurface
import com.normno.travelassistant.core.designsystem.component.TravelLoadingIndicator
import com.normno.travelassistant.core.designsystem.theme.TravelShapeTokens
import com.normno.travelassistant.core.model.TemplateSummary
import com.normno.travelassistant.core.model.TripId

/**
 * Каталог готовых маршрутов.
 *
 * В шаблоне нет ни броней, ни заметок, ни фотографий автора — по устройству типа
 * [com.normno.travelassistant.core.model.TripTemplate], а не по договорённости.
 * Поэтому карточку чужого маршрута можно показывать без оговорок.
 */
@Composable
fun CatalogScreen(
    onTripCreated: (TripId) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val current = state) {
        Outcome.Loading -> TravelLoadingIndicator(modifier = modifier.fillMaxSize())

        is Outcome.Failure -> EmptyState(
            icon = Icons.Rounded.Explore,
            title = "Каталог не открылся",
            description = current.cause.message.orEmpty(),
            modifier = modifier,
        )

        is Outcome.Success -> if (current.data.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Explore,
                title = "Готовых маршрутов пока нет",
                description = "Здесь появятся маршруты, собранные другими путешественниками. " +
                    "Скачанный маршрут можно переделать под себя — он всего лишь заготовка.",
                modifier = modifier,
            )
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items = current.data, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onApply = { viewModel.apply(template, onDone = onTripCreated) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateSummary,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShapedSurface(
                    polygon = TravelShapeTokens.cover,
                    modifier = Modifier.size(52.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp).align(Alignment.Center),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(template.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${template.dayCount} дн. · ${template.stopCount} этапов" +
                            template.author.displayName
                                .takeIf { it.isNotBlank() }
                                ?.let { " · $it" }
                                .orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (template.summary.isNotBlank()) {
                Text(
                    text = template.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            ActionChip(
                label = "Взять за основу",
                icon = Icons.Rounded.Download,
                onClick = onApply,
            )
        }
    }
}
