package com.normno.travelassistant.feature.assistant

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.normno.travelassistant.core.designsystem.component.EmptyState

/**
 * Ассистент
 *
 * Экран-заготовка: разметка и навигация на месте, содержимое — следующая фаза
 * (см. docs/ROADMAP.md). Пустое состояние здесь не затычка, а то, что увидит
 * пользователь до того, как раздел наполнится.
 */
@Composable
fun AssistantScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    EmptyState(
        icon = Icons.Rounded.AutoAwesome,
        title = "Ассистент",
        description = "Планирование и правка плана моделью: обмен Markdown с якорями, предпросмотр изменений и кнопка «Применить». Фаза 4.",
        modifier = modifier.fillMaxSize().padding(contentPadding),
    )
}
