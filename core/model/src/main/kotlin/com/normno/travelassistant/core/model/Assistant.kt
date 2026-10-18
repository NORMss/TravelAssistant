package com.normno.travelassistant.core.model

import kotlinx.serialization.Serializable

/**
 * Разговор с моделью о поездке. Хранится локально: это рабочий черновик плана,
 * к которому возвращаешься, а не одноразовый запрос.
 */
@Serializable
data class AssistantThread(
    val id: String,
    val tripId: TripId?,
    val title: String = "",
    val messages: List<AssistantMessage> = emptyList(),
    val sync: SyncMeta = SyncMeta(),
)

@Serializable
data class AssistantMessage(
    val id: String,
    val role: AssistantRole,
    val text: String,
    val createdAt: Long,
    /**
     * Предложенная моделью правка плана — уже разобранная, но ещё не применённая.
     * Пользователь видит список изменений и нажимает «Применить» сам: модель
     * не пишет в план напрямую, никогда.
     */
    val proposal: PlanProposal? = null,
    /** Описание интерфейса в протоколе A2UI, если ответ пришёл в виде UI, а не текста. */
    val a2uiPayload: String? = null,
    val model: String = "",
    val error: String? = null,
)

enum class AssistantRole { USER, ASSISTANT, SYSTEM, TOOL }

/**
 * Разбор ответа модели в набор операций над планом. Формат обмена — Markdown с
 * якорями (см. core:ai), потому что JSON ломается от одной запятой, а Markdown
 * модель правит уверенно и его видно глазами до применения.
 */
@Serializable
data class PlanProposal(
    val tripId: TripId,
    val changes: List<PlanChange> = emptyList(),
    /** Ответ оборван или половина плана «удаляется» — показать предупреждение сверху. */
    val warnings: List<String> = emptyList(),
) {
    val isEmpty: Boolean get() = changes.isEmpty()
    val removals: Int get() = changes.count { it.kind == PlanChangeKind.REMOVE }
}

@Serializable
data class PlanChange(
    val kind: PlanChangeKind,
    val target: PlanChangeTarget,
    val entityId: String,
    val label: String,
    /** Человекочитаемая разница по полям: «время 05:50 → 06:10, заметка». */
    val fields: List<String> = emptyList(),
)

enum class PlanChangeKind { ADD, UPDATE, REMOVE }

enum class PlanChangeTarget { DAY, STOP, FORK, PLACE, CHECKLIST }
