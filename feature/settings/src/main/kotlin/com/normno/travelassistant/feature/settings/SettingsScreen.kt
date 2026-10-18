package com.normno.travelassistant.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.normno.travelassistant.core.ai.LlmAvailability
import com.normno.travelassistant.core.designsystem.component.ChipOption
import com.normno.travelassistant.core.designsystem.component.ChipRow
import com.normno.travelassistant.core.designsystem.component.FloatingCard
import com.normno.travelassistant.core.datastore.ThemePreference

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Section("Вид") {
            ChipRow(
                options = listOf(
                    ChipOption(ThemePreference.SYSTEM, "Как в системе"),
                    ChipOption(ThemePreference.LIGHT, "Светлая"),
                    ChipOption(ThemePreference.DARK, "Тёмная"),
                ),
                selectedKey = state.settings.theme,
                onSelect = { viewModel.setTheme(it as ThemePreference) },
                contentPadding = PaddingValues(0.dp),
            )
            SwitchRow(
                title = "Цвета по системе",
                subtitle = if (state.dynamicColorSupported) {
                    "Палитру задают обои — как в остальной системе"
                } else {
                    "Нужен Android 12 или новее"
                },
                checked = state.settings.dynamicColor && state.dynamicColorSupported,
                enabled = state.dynamicColorSupported,
                onCheckedChange = viewModel::setDynamicColor,
            )
        }

        Section("Напоминания") {
            SwitchRow(
                title = "Будильники по плану",
                subtitle = "Регистрация, выезд, закрытие входа — то, что нельзя пропустить",
                checked = state.settings.remindersEnabled,
                onCheckedChange = viewModel::setReminders,
            )
        }

        Section("Модель") {
            if (state.models.isEmpty()) {
                Text(
                    text = "Провайдеры не подключены",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.models.forEach { model ->
                SwitchRow(
                    title = model.name,
                    subtitle = model.availability.describe(),
                    checked = state.settings.llmProviderId == model.id,
                    onCheckedChange = { if (it) viewModel.selectModel(model.id) },
                )
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp),
        )
        FloatingCard(elevation = 1.dp) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = { content() },
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

private fun LlmAvailability.describe(): String = when (this) {
    LlmAvailability.Ready -> "готова к работе"
    is LlmAvailability.NeedsDownload -> "нужно скачать модель"
    LlmAvailability.Downloading -> "скачивается"
    is LlmAvailability.NeedsApiKey -> "нужен ключ"
    is LlmAvailability.Unavailable -> reason
}
