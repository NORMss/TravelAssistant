package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Снимок в фотоотчёте поездки.
 *
 * Фотография живёт в личном слое и **никогда** не попадает в [TripTemplate]:
 * на ней бывают лица, документы и геометки с точностью до подъезда.
 *
 * Привязка к этапу или месту делается при съёмке, по текущему моменту плана —
 * искать потом, к чему относился кадр, пользователь не станет.
 */
@Serializable
data class PhotoEntry(
    val id: String,
    val tripId: TripId,
    val dayId: DayId? = null,
    val stopId: StopId? = null,
    val placeId: PlaceId? = null,
    /** content:// или file:// внутри приложения. Наружу уходит только через шер. */
    val uri: String,
    val capturedAt: Long,
    /** Геометка кадра. Пустая, если пользователь не дал доступ к геопозиции. */
    val point: GeoPoint? = null,
    val caption: String = "",
    val favourite: Boolean = false,
    /** Что предложила модель распознавания. Не истина, пока не подтверждено. */
    val sighting: LandmarkSighting? = null,
    val sync: SyncMeta = SyncMeta(),
)

/**
 * Результат офлайн-распознавания достопримечательности.
 *
 * Тот же принцип, что у языковой модели: **распознавание — предложение, а не
 * факт.** Пока `confirmedByUser` ложно, подпись показывается со знаком вопроса и
 * не уходит ни в поиск, ни в экспорт. Классификатор на 30 тысяч классов ошибается
 * регулярно, и подписать чужой храм именем соседнего — худший исход, чем не
 * подписать вовсе.
 *
 * @param packId какой пакет региона дал ответ — по нему понятно, почему модель
 *   промахнулась: Эйфелева башня европейским классификатором в Азии не ищется
 * @param modelVersion версия модели: подписи, сделанные старой, можно пересчитать
 */
@Serializable
data class LandmarkSighting(
    val label: String,
    val confidence: Float,
    val packId: String,
    val modelVersion: Int = 1,
    val confirmedByUser: Boolean = false,
    /** Место из плана, с которым соотнесли подпись. */
    val matchedPlaceId: PlaceId? = null,
) {
    /** Ниже этого порога подпись не показывают вовсе — шум только мешает. */
    val isUsable: Boolean get() = confidence >= MIN_CONFIDENCE

    /** Выше этого можно предлагать уверенно, но всё ещё с подтверждением. */
    val isStrong: Boolean get() = confidence >= STRONG_CONFIDENCE

    companion object {
        const val MIN_CONFIDENCE = 0.35f
        const val STRONG_CONFIDENCE = 0.70f
    }
}
