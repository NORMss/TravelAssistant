package com.normno.travelassistant.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.normno.travelassistant.core.designsystem.theme.Motion

/**
 * Подсказки при первом открытии экрана: затемнение с вырезом вокруг элемента
 * и карточка с объяснением.
 *
 * **Почему своё, а не библиотека.** Готовые решения есть и живые (Reveal,
 * compose-spotlight, GuideKit), но все они приносят свои кривые анимации и свою
 * вёрстку карточки. В приложении, где движение задаёт `MotionScheme.expressive()`
 * из темы, а карточки выглядят одинаково на всех экранах, чужой компонент
 * читался бы как вставка из другого приложения. Сложная часть здесь небольшая —
 * вырез в затемнении и позиционирование карточки, — а выигрыш от единого стиля
 * постоянный: подсказки появляются поверх каждого экрана.
 *
 * Использование:
 * ```
 * val tour = rememberGuideTour(GuideTour.TODAY, steps, onFinished = vm::markSeen)
 * Column {
 *     CurrentStopCard(Modifier.guideTarget(tour, GuideKey.TODAY_CURRENT_STOP))
 * }
 * GuideOverlay(tour) { key -> ... }
 * ```
 */
@Stable
class GuideTourState internal constructor(
    val tourId: String,
    internal val steps: List<Any>,
    private val onFinished: (String) -> Unit,
) {
    private var index by mutableIntStateOf(NOT_STARTED)

    internal val bounds = mutableStateMapOf<Any, Rect>()

    val current: Any? get() = steps.getOrNull(index)

    val isRunning: Boolean get() = index in steps.indices

    val stepNumber: Int get() = index + 1

    val stepCount: Int get() = steps.size

    val isLast: Boolean get() = index == steps.lastIndex

    /**
     * Запуск откладывается до того, как элементы окажутся измерены: вырез вокруг
     * ещё не размещённого элемента — это дырка в случайном месте экрана.
     */
    fun start() {
        if (index == NOT_STARTED) index = 0
    }

    fun next() {
        if (!isRunning) return
        if (isLast) finish() else index++
    }

    /** Пропустить весь тур. Он не повторится: отметка о показе ставится здесь же. */
    fun skip() = finish()

    private fun finish() {
        index = FINISHED
        onFinished(tourId)
    }

    private companion object {
        const val NOT_STARTED = -1
        const val FINISHED = Int.MAX_VALUE
    }
}

/**
 * @param enabled false — тур уже показывали; состояние создаётся, но не запускается
 * @param onFinished вызывается один раз, когда тур пройден или пропущен
 */
@Composable
fun rememberGuideTour(
    tourId: String,
    steps: List<Any>,
    enabled: Boolean = true,
    onFinished: (String) -> Unit = {},
): GuideTourState {
    val state = remember(tourId, steps) { GuideTourState(tourId, steps, onFinished) }
    if (enabled && steps.isNotEmpty()) state.start()
    return state
}

/** Отмечает элемент как цель подсказки. Ничего не рисует, только запоминает границы. */
fun Modifier.guideTarget(state: GuideTourState, key: Any): Modifier =
    onGloballyPositioned { coordinates ->
        state.bounds[key] = coordinates.boundsInRoot()
    }

/** Текст одной подсказки. */
data class GuideStep(val title: String, val body: String)

/**
 * Слой подсказок. Ставится последним в корневом `Box` экрана, поверх содержимого.
 *
 * Тап в любом месте — следующий шаг: искать маленькую кнопку «далее» на
 * затемнённом экране неудобно, а промахнуться мимо всего экрана нельзя.
 */
@Composable
fun GuideOverlay(
    state: GuideTourState,
    modifier: Modifier = Modifier,
    scrimColor: Color = Color.Black.copy(alpha = 0.72f),
    step: (Any) -> GuideStep,
) {
    val key = state.current ?: return
    val target = state.bounds[key] ?: return
    val info = step(key)

    val density = LocalDensity.current
    val padPx = with(density) { 8.dp.toPx() }
    val cornerPx = with(density) { 20.dp.toPx() }

    // Вырез переезжает между шагами пружиной из темы — так видно, что подсказка
    // перешла к соседнему элементу, а не появилась заново в другом месте.
    val hole by animateValueAsState(
        targetValue = target.inflate(padPx),
        typeConverter = Rect.VectorConverter,
        animationSpec = Motion.spatial(),
        label = "guideHole",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(key) { detectTapGestures { state.next() } },
    ) {
        val heightPx = with(density) { maxHeight.toPx() }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Без отдельного слоя BlendMode.Clear вырежет дырку не в затемнении,
                // а во всём, что нарисовано под ним.
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
        ) {
            drawRect(scrimColor)
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(hole.left, hole.top),
                size = Size(hole.width, hole.height),
                cornerRadius = CornerRadius(cornerPx, cornerPx),
                blendMode = BlendMode.Clear,
            )
        }

        // Карточка под вырезом, если элемент в верхней половине экрана, и над ним,
        // если в нижней. Так она не закрывает то, про что рассказывает.
        val below = hole.center.y < heightPx / 2f
        val gap = with(density) { 16.dp.toPx() }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(Motion.effects()),
            exit = fadeOut(Motion.effects()),
            modifier = Modifier.align(if (below) Alignment.TopStart else Alignment.BottomStart),
        ) {
            FloatingCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset {
                        IntOffset(
                            x = 0,
                            y = if (below) {
                                (hole.bottom + gap).toInt()
                            } else {
                                -(heightPx - hole.top + gap).toInt()
                            },
                        )
                    }
                    .semantics { liveRegion = LiveRegionMode.Polite },
                containerColor = MaterialTheme.colorScheme.inverseSurface,
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = info.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                    Text(
                        text = info.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${state.stepNumber} из ${state.stepCount}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                        )
                        Row {
                            if (!state.isLast) {
                                TextButton(onClick = state::skip) { Text("Пропустить") }
                            }
                            TextButton(onClick = state::next) {
                                Text(if (state.isLast) "Понятно" else "Далее")
                            }
                        }
                    }
                }
            }
        }
    }
}
