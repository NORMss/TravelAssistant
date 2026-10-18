package com.normno.travelassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.normno.travelassistant.core.datastore.AppSettings
import com.normno.travelassistant.core.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Держит только то, что нужно до первого экрана: тему. Пока настройки не прочитаны,
 * `null` — это сигнал держать splash, а не причина мигнуть светлой темой и
 * перекраситься в тёмную на следующем кадре.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    settings: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings?> = settings.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )
}
