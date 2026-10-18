package com.normno.travelassistant.core.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Список доступных провайдеров. Собирается через Hilt-мультибиндинг: добавить
 * провайдера значит добавить `@Provides @IntoSet` в его модуль и ничего больше —
 * ни экран настроек, ни ассистент о нём знать не должны.
 */
@Singleton
class LlmRegistry @Inject constructor(
    private val providers: Set<@JvmSuppressWildcards LlmProvider>,
) {
    fun all(): List<LlmProvider> = providers.sortedBy { it.displayName }

    fun byId(id: String): LlmProvider? = providers.firstOrNull { it.id == id }

    /**
     * Чем отвечать, если пользователь ничего не выбрал. Сначала то, что работает без
     * сети и без ключа: приложение должно быть полезно сразу после установки.
     */
    suspend fun bestAvailable(): LlmProvider? {
        val candidates = all().sortedWith(
            compareByDescending<LlmProvider> { it.capabilities.onDevice }
                .thenBy { it.capabilities.requiresApiKey },
        )
        return candidates.firstOrNull { it.availability() is LlmAvailability.Ready }
    }
}
