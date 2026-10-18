package com.normno.travelassistant.core.vision

import com.normno.travelassistant.core.model.RegionPack
import com.normno.travelassistant.core.model.RegionPackStatus
import kotlinx.coroutines.flow.Flow

/**
 * Пакеты распознавания: что доступно, что установлено, что качается.
 *
 * Скачивание — отдельная долгая операция с видимым прогрессом, а не побочный
 * эффект открытия камеры. Пользователь должен уметь начать его дома, свернуть
 * приложение и вернуться к готовому результату, поэтому загрузка живёт в
 * WorkManager, а не в области видимости экрана.
 */
interface RegionPackRepository {

    /** Полный каталог: установленные и доступные, с состоянием каждого. */
    fun observeCatalog(): Flow<List<RegionPackStatus>>

    fun observeStatus(packId: String): Flow<RegionPackStatus?>

    /**
     * Поставить в очередь. Возврат — немедленный: прогресс приходит через
     * [observeStatus].
     *
     * @param onlyOnUnmetered качать 47 МБ в роуминге по мобильной сети — это
     *   деньги пользователя; по умолчанию ждём Wi-Fi
     */
    suspend fun download(packId: String, onlyOnUnmetered: Boolean = true)

    suspend fun cancel(packId: String)

    /** Удалить модель с устройства, освободив место. Каталог при этом остаётся. */
    suspend fun remove(packId: String)

    /** Обновить каталог с сервера. Падение сети — не ошибка: работает кэш. */
    suspend fun refreshCatalog()

    /** Путь к установленной модели или null. Нужен распознавателю. */
    suspend fun installedModelPath(packId: String): String?

    companion object {
        /** Запасной каталог, зашитый в приложение: без сети он всё равно нужен. */
        val BUILT_IN_CATALOG: List<RegionPack> = listOf(
            RegionPack(
                id = "asia-v1",
                title = "Азия",
                countryCodes = listOf(
                    "CN", "JP", "KR", "TH", "VN", "IN", "ID", "MY", "SG",
                    "PH", "KH", "LA", "MM", "NP", "LK", "MN", "TW", "HK",
                ),
                sizeBytes = 47L * 1024 * 1024,
                landmarkCount = 30_000,
            ),
            RegionPack(
                id = "europe-v1",
                title = "Европа",
                countryCodes = listOf(
                    "FR", "DE", "IT", "ES", "GB", "PT", "NL", "BE", "AT",
                    "CH", "CZ", "PL", "GR", "SE", "NO", "DK", "FI", "HU",
                    "RO", "BG", "HR", "RS", "IE", "IS", "EE", "LV", "LT",
                ),
                sizeBytes = 48L * 1024 * 1024,
                landmarkCount = 30_000,
            ),
            RegionPack(
                id = "africa-v1",
                title = "Африка",
                countryCodes = listOf(
                    "EG", "MA", "TN", "ZA", "KE", "TZ", "ET", "NG", "GH",
                    "SN", "NA", "BW", "ZW", "UG", "MU",
                ),
                sizeBytes = 22L * 1024 * 1024,
                landmarkCount = 15_000,
            ),
        )
    }
}
