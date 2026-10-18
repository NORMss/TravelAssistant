@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.normno.travelassistant.core.designsystem.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Движение берётся из `MotionScheme.expressive()`, установленной в теме: это и есть
 * «новые анимации Material 3 Expressive» — пружины с лёгким перелётом вместо кривых
 * постоянной длительности. Доставать их нужно отсюда, а не писать `tween(300)` по
 * месту: перелёт у одного элемента и ровная кривая у соседнего читаются как баг.
 *
 * `spatial` — для того, что двигается и меняет размер; перелёт здесь к месту.
 * `effects` — для цвета, прозрачности, тени; перелёт по цвету выглядит грязно.
 */
object Motion {

    /** Появление и исчезновение плавающих элементов, нажатия. */
    @Composable @ReadOnlyComposable
    fun <T> fastSpatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.fastSpatialSpec()

    /** Обычные перестановки внутри экрана. */
    @Composable @ReadOnlyComposable
    fun <T> spatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultSpatialSpec()

    /** Переходы между экранами и крупные перекомпоновки. */
    @Composable @ReadOnlyComposable
    fun <T> slowSpatial(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.slowSpatialSpec()

    @Composable @ReadOnlyComposable
    fun <T> fastEffects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.fastEffectsSpec()

    @Composable @ReadOnlyComposable
    fun <T> effects(): FiniteAnimationSpec<T> = MaterialTheme.motionScheme.defaultEffectsSpec()
}
