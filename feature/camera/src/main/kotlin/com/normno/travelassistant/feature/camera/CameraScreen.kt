package com.normno.travelassistant.feature.camera

import androidx.camera.compose.CameraXViewfinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.normno.travelassistant.core.designsystem.component.EmptyState
import com.normno.travelassistant.core.designsystem.component.FloatingCard

/**
 * Камера фотоотчёта. Снимок привязывается к текущему этапу плана автоматически —
 * искать потом, к чему относился кадр, пользователь не станет.
 *
 * Распознавание достопримечательности подключается здесь же, когда для региона
 * скачан пакет; без пакета экран работает как обычная камера.
 */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) { viewModel.bindToLifecycle(context, owner) }

    val request = state.surfaceRequest
    if (request == null) {
        EmptyState(
            icon = Icons.Rounded.PhotoCamera,
            title = "Камера готовится",
            description = state.error ?: "Разрешите доступ к камере, чтобы снимать в фотоотчёт.",
            modifier = modifier.fillMaxSize().padding(contentPadding),
        )
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = Modifier.fillMaxSize(),
        )

        val sighting = state.sighting
        if (sighting != null && sighting.isUsable) {
            FloatingCard(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(contentPadding)
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Знак вопроса не украшение: пока пользователь не подтвердил,
                    // это догадка модели, а не подпись к снимку.
                    Text(
                        text = if (sighting.isStrong) sighting.label else "${sighting.label}?",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Уверенность ${(sighting.confidence * 100).toInt()}% · " +
                            "подтвердите, чтобы подписать снимок",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
