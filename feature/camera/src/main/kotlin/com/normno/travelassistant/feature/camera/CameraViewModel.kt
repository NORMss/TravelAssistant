package com.normno.travelassistant.feature.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.normno.travelassistant.core.model.LandmarkSighting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CameraUiState(
    val surfaceRequest: SurfaceRequest? = null,
    /** Пакет региона для этой поездки. Пусто — распознавание выключено. */
    val activePackId: String? = null,
    val packReady: Boolean = false,
    /** Что предложила модель по текущему кадру. Подтверждает пользователь. */
    val sighting: LandmarkSighting? = null,
    val error: String? = null,
)

/**
 * Камера для фотоотчёта.
 *
 * `SurfaceRequest` держится во ViewModel, а не в composable: при повороте экрана
 * пересоздание запроса означает чёрный кадр на пол-секунды и заново
 * инициализированную камеру.
 */
@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CameraUiState())
    val state: StateFlow<CameraUiState> = _state.asStateFlow()

    private val preview = Preview.Builder().build().apply {
        setSurfaceProvider { request ->
            _state.value = _state.value.copy(surfaceRequest = request)
        }
    }

    private val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    /**
     * Привязка живёт столько, сколько открыт экран: `awaitCancellation` держит
     * её до отмены корутины, а `finally` освобождает камеру. Без этого вторая
     * камера в приложении не откроется — устройство занято.
     */
    fun bindToLifecycle(context: Context, owner: LifecycleOwner) {
        viewModelScope.launch {
            val provider = ProcessCameraProvider.awaitInstance(context)
            provider.unbindAll()
            runCatching {
                provider.bindToLifecycle(
                    owner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
                awaitCancellation()
            }.onFailure { error ->
                if (error !is kotlinx.coroutines.CancellationException) {
                    _state.value = _state.value.copy(error = error.message)
                }
            }
            provider.unbindAll()
        }
    }

    fun dismissSighting() {
        _state.value = _state.value.copy(sighting = null)
    }
}
