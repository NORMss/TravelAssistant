package com.normno.travelassistant.feature.phrasebook

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.normno.travelassistant.core.designsystem.component.EmptyState

/**
 * Разговорник
 *
 * Экран-заготовка: разметка и навигация на месте, содержимое — следующая фаза
 * (см. docs/ROADMAP.md). Пустое состояние здесь не затычка, а то, что увидит
 * пользователь до того, как раздел наполнится.
 */
@Composable
fun PhrasebookScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    EmptyState(
        icon = Icons.Rounded.Translate,
        title = "Разговорник",
        description = "Фразы под язык места: группы по ситуациям, поиск, избранное и свои фразы. Показ во весь экран — чтобы отдать телефон собеседнику.",
        modifier = modifier.fillMaxSize().padding(contentPadding),
    )
}
