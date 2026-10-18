package com.normno.travelassistant.feature.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.normno.travelassistant.core.ai.LlmAvailability
import com.normno.travelassistant.core.ai.LlmRegistry
import com.normno.travelassistant.core.datastore.AppSettings
import com.normno.travelassistant.core.datastore.SettingsRepository
import com.normno.travelassistant.core.datastore.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModelStatus(
    val id: String,
    val name: String,
    val onDevice: Boolean,
    val availability: LlmAvailability,
)

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val dynamicColorSupported: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
    val models: List<ModelStatus> = emptyList(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val registry: LlmRegistry,
) : ViewModel() {

    private val models = MutableStateFlow<List<ModelStatus>>(emptyList())

    val state: StateFlow<SettingsUiState> = combine(
        repository.settings,
        models,
    ) { settings, modelList ->
        SettingsUiState(settings = settings, models = modelList)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    init {
        refreshModels()
    }

    /**
     * Доступность модели спрашивается заново при каждом открытии настроек: Gemini Nano
     * мог быть скачан, ключ — отозван, сеть — пропасть. Кэшировать это значит показывать
     * пользователю неправду.
     */
    fun refreshModels() {
        viewModelScope.launch {
            models.value = registry.all().map { provider ->
                ModelStatus(
                    id = provider.id,
                    name = provider.displayName,
                    onDevice = provider.capabilities.onDevice,
                    availability = provider.availability(),
                )
            }
        }
    }

    fun setTheme(value: ThemePreference) = launchEdit { repository.setTheme(value) }

    fun setDynamicColor(value: Boolean) = launchEdit { repository.setDynamicColor(value) }

    fun setReminders(value: Boolean) = launchEdit { repository.setRemindersEnabled(value) }

    fun selectModel(providerId: String) = launchEdit { repository.setLlm(providerId, "") }

    private fun launchEdit(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
