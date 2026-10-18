package com.normno.travelassistant.core.ai

import kotlinx.coroutines.flow.Flow

/**
 * Единая точка, через которую приложение разговаривает с языковой моделью.
 *
 * Провайдеров несколько и они принципиально разные: Gemini Nano на устройстве
 * работает офлайн и бесплатно, но коротким контекстом; облачная модель умеет
 * спланировать неделю в Японии, но требует ключа и сети. Экран не должен знать,
 * с кем он говорит, — иначе каждая новая модель переписывает UI.
 *
 * Поток, а не suspend-функция: ответ модели идёт частями, и ждать его целиком,
 * показывая спиннер, — это десятки секунд пустого экрана.
 */
interface LlmProvider {
    val id: String
    val displayName: String
    val capabilities: LlmCapabilities

    /** Готов ли провайдер отвечать прямо сейчас: есть ключ, есть сеть, скачана модель. */
    suspend fun availability(): LlmAvailability

    fun generate(request: LlmRequest): Flow<LlmChunk>
}

data class LlmCapabilities(
    /** Работает без сети. */
    val onDevice: Boolean,
    /** Нужен ключ, введённый пользователем. */
    val requiresApiKey: Boolean,
    /** Принимает изображения — для «что написано в этом меню». */
    val vision: Boolean = false,
    /** Умеет возвращать структурированный ответ по схеме, а не только текст. */
    val structuredOutput: Boolean = false,
    /** Поддерживает инструменты (function calling). */
    val tools: Boolean = false,
    /** Примерный предел контекста в токенах. 0 — неизвестно. */
    val contextTokens: Int = 0,
)

sealed interface LlmAvailability {
    data object Ready : LlmAvailability

    /** Модель есть, но её нужно скачать — на устройстве это нормальный путь. */
    data class NeedsDownload(val approximateBytes: Long? = null) : LlmAvailability

    data object Downloading : LlmAvailability

    data class NeedsApiKey(val providerId: String) : LlmAvailability

    data class Unavailable(val reason: String) : LlmAvailability
}

data class LlmRequest(
    val prompt: String,
    val systemInstruction: String = "",
    /** Предыдущие сообщения треда: без них модель не помнит, что правит. */
    val history: List<LlmTurn> = emptyList(),
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null,
    val images: List<ByteArray> = emptyList(),
)

data class LlmTurn(val role: String, val text: String)

/** Кусок ответа. Текст приходит частями, терминальные события — по одному разу. */
sealed interface LlmChunk {
    data class Text(val delta: String) : LlmChunk
    data class Progress(val message: String) : LlmChunk
    data class Failed(val cause: Throwable, val message: String) : LlmChunk
    data object Done : LlmChunk
}
