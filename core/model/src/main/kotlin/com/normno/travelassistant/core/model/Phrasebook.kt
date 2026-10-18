package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Разговорник привязан к языковой паре, а не к стране: один и тот же испанский нужен
 * и в Мадриде, и в Мехико, а в Швейцарии за одну поездку нужны три языка.
 *
 * @param sourceLocale язык пользователя, BCP-47
 * @param targetLocale язык места
 */
@Serializable
data class PhraseGroup(
    val key: String,
    val title: String,
    val localTitle: String = "",
    val sourceLocale: String,
    val targetLocale: String,
    /** Показывать группу только на этих категориях этапов — как в экране «Сейчас». */
    val categories: List<StopCategory> = emptyList(),
    val phrases: List<Phrase> = emptyList(),
    val builtin: Boolean = false,
)

@Serializable
data class Phrase(
    val id: PhraseId,
    val source: String,
    val target: String,
    val transliteration: String = "",
    val favourite: Boolean = false,
    /** Сгенерирована моделью под конкретную ситуацию, а не взята из набора. */
    val generated: Boolean = false,
    val sync: SyncMeta = SyncMeta(),
)
