package com.normno.travelassistant.core.ai.provider

import com.normno.travelassistant.core.ai.LlmAvailability
import com.normno.travelassistant.core.ai.LlmCapabilities
import com.normno.travelassistant.core.ai.LlmChunk
import com.normno.travelassistant.core.ai.LlmProvider
import com.normno.travelassistant.core.ai.LlmRequest
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.SystemInstruction
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini Nano на самом устройстве, через ML Kit GenAI Prompt API.
 *
 * Почему это первый провайдер, а не последний: он бесплатный, работает в самолёте и
 * в стране с заблокированными сервисами, и не отправляет план поездки наружу.
 * Ограничения тоже существенные: короткий контекст и качество ниже облачного, поэтому
 * его дело — короткие задачи. Перевести фразу, подобрать формулировку, сжать заметку,
 * предложить, чем заменить закрытый музей.
 *
 * Доступность проверяется каждый раз: модели нет на части телефонов, а там, где есть,
 * её может потребоваться скачать. Это обычный путь, а не ошибка.
 */
@Singleton
class OnDeviceLlmProvider @Inject constructor() : LlmProvider {

    override val id: String = ID
    override val displayName: String = "Gemini Nano (на устройстве)"

    override val capabilities = LlmCapabilities(
        onDevice = true,
        requiresApiKey = false,
        vision = true,
        structuredOutput = true,
        contextTokens = 4_000,
    )

    override suspend fun availability(): LlmAvailability =
        runCatching {
            when (Generation.getClient().checkStatus()) {
                FeatureStatus.AVAILABLE -> LlmAvailability.Ready
                FeatureStatus.DOWNLOADABLE -> LlmAvailability.NeedsDownload()
                FeatureStatus.DOWNLOADING -> LlmAvailability.Downloading
                else -> LlmAvailability.Unavailable("Устройство не поддерживает Gemini Nano")
            }
        }.getOrElse { error ->
            LlmAvailability.Unavailable(error.message ?: "ML Kit недоступен")
        }

    /** Скачивание модели — отдельное действие с видимым пользователю прогрессом. */
    fun download(): Flow<Unit> = flow {
        Generation.getClient().download().collect { emit(Unit) }
    }

    override fun generate(request: LlmRequest): Flow<LlmChunk> = flow {
        val model = Generation.getClient()
        val prompt = buildPrompt(request)

        val content = if (request.systemInstruction.isBlank()) {
            generateContentRequest(TextPart(prompt)) {}
        } else {
            generateContentRequest(
                SystemInstruction(request.systemInstruction),
                TextPart(prompt),
            ) {}
        }

        // Nano отдаёт накопленный текст, а не дельту: вычитаем уже отправленное,
        // иначе экран будет дублировать каждый фрагмент.
        var emitted = 0
        model.generateContentStream(content).collect { response ->
            val text = response.candidates.firstOrNull()?.text.orEmpty()
            if (text.length > emitted) {
                emit(LlmChunk.Text(text.substring(emitted)))
                emitted = text.length
            }
        }
        emit(LlmChunk.Done)
    }

    /**
     * История склеивается в один промпт: системная инструкция и многоходовый диалог
     * поддерживаются не на всех устройствах (`isSystemPromptAvailable`), а терять
     * контекст правки нельзя.
     */
    private fun buildPrompt(request: LlmRequest): String {
        if (request.history.isEmpty()) return request.prompt
        return buildString {
            request.history.forEach { turn ->
                appendLine("${turn.role}: ${turn.text}")
            }
            append("user: ")
            append(request.prompt)
        }
    }

    companion object {
        const val ID = "mlkit-nano"
    }
}
