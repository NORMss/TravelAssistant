package com.normno.travelassistant.feature.places

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.normno.travelassistant.core.designsystem.component.EmptyState

/**
 * Карта и места
 *
 * Экран-заготовка: разметка и навигация на месте, содержимое — следующая фаза
 * (см. docs/ROADMAP.md). Пустое состояние здесь не затычка, а то, что увидит
 * пользователь до того, как раздел наполнится.
 */
@Composable
fun PlacesScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    EmptyState(
        icon = Icons.Rounded.Map,
        title = "Карта и места",
        description = "Карта с пинами плана, «что рядом», свои места долгим нажатием и офлайн-тайлы. Основа есть в прошлом приложении — переносится в фазе 3.",
        modifier = modifier.fillMaxSize().padding(contentPadding),
    )
}
