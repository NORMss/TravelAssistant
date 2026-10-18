package com.normno.travelassistant.core.vision

import android.graphics.Bitmap
import com.normno.travelassistant.core.model.LandmarkSighting

/**
 * Офлайн-распознавание достопримечательности по кадру.
 *
 * Идея и формат моделей взяты из предыдущего проекта автора: классификатор на
 * .tflite с метаданными, по одному на регион. Что изменилось: модели больше не
 * лежат в APK (117 МБ на три региона — неприемлемо), а скачиваются по одной,
 * и результат считается **предложением**, а не фактом — см. [LandmarkSighting].
 *
 * Реализация появится в фазе 5 (docs/VISION.md). Интерфейс объявлен сейчас,
 * чтобы экран камеры и фотоотчёт строились против него, а не против MediaPipe.
 */
interface LandmarkRecognizer {

    /** Готов ли распознаватель: установлен ли пакет, загрузилась ли модель. */
    suspend fun isReady(packId: String): Boolean

    /**
     * @param maxResults больше трёх вариантов пользователю показывать бессмысленно:
     *   из длинного списка он всё равно выберет первый
     */
    suspend fun recognise(
        bitmap: Bitmap,
        packId: String,
        maxResults: Int = 3,
    ): List<LandmarkSighting>

    /** Освободить модель. Она занимает десятки мегабайт в памяти. */
    fun release()
}
