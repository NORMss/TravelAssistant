package com.normno.travelassistant.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Запасная палитра. Работает только там, где системных цветов нет (Android 11 и ниже)
 * или когда пользователь сам отключил «цвета по системе» в настройках. На всех
 * остальных устройствах схему задаёт обои — именно этого просит Material You, и именно
 * поэтому у приложения нет своего фирменного цвета, который спорил бы с системным.
 *
 * Для светлой темы берётся готовая `expressiveLightColorScheme()` из material3;
 * тёмного аналога в библиотеке нет, поэтому он собран здесь из того же тонального ряда.
 */
private val Teal80 = Color(0xFF76D7C4)
private val Teal30 = Color(0xFF00504A)
private val Teal20 = Color(0xFF003733)
private val Sand80 = Color(0xFFE8C9A0)
private val Sand30 = Color(0xFF53411F)
private val Sand20 = Color(0xFF3A2C0E)
private val Clay80 = Color(0xFFF2B8B5)
private val Clay30 = Color(0xFF5C3A37)
private val Ink10 = Color(0xFF101413)
private val Ink20 = Color(0xFF1B201F)
private val Ink90 = Color(0xFFE1E4E2)
private val Ink80 = Color(0xFFC3C8C6)
private val Outline60 = Color(0xFF8A9390)

internal val FallbackDarkScheme: ColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Color(0xFF93F3DF),
    secondary = Sand80,
    onSecondary = Sand20,
    secondaryContainer = Sand30,
    onSecondaryContainer = Color(0xFFFFE5BC),
    tertiary = Clay80,
    onTertiary = Color(0xFF442422),
    tertiaryContainer = Clay30,
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Ink10,
    onBackground = Ink90,
    surface = Ink10,
    onSurface = Ink90,
    surfaceVariant = Ink20,
    onSurfaceVariant = Ink80,
    surfaceContainerLowest = Color(0xFF0B0F0E),
    surfaceContainerLow = Ink20,
    surfaceContainer = Color(0xFF212625),
    surfaceContainerHigh = Color(0xFF2B312F),
    surfaceContainerHighest = Color(0xFF363C3A),
    outline = Outline60,
    outlineVariant = Color(0xFF3F4644),
)

/**
 * Роли, которых в Material-схеме нет, но которые нужны смыслу приложения: развилка
 * «идём по плану / обход / отмена», и состояние «срочно».
 *
 * Выводятся из текущей схемы, а не задаются константами, — иначе при системных цветах
 * эти три цвета торчали бы из палитры чужеродным пятном.
 */
@Immutable
data class TravelAccents(
    val onTrack: Color,
    val onTrackContainer: Color,
    val detour: Color,
    val detourContainer: Color,
    val cancelled: Color,
    val cancelledContainer: Color,
    val urgent: Color,
    val urgentContainer: Color,
)

internal fun accentsFor(scheme: ColorScheme, dark: Boolean): TravelAccents = TravelAccents(
    onTrack = scheme.primary,
    onTrackContainer = scheme.primaryContainer,
    detour = scheme.tertiary,
    detourContainer = scheme.tertiaryContainer,
    cancelled = scheme.error,
    cancelledContainer = scheme.errorContainer,
    urgent = if (dark) scheme.error else scheme.error,
    urgentContainer = scheme.errorContainer,
)

val LocalTravelAccents = staticCompositionLocalOf {
    accentsFor(FallbackDarkScheme, dark = true)
}

/** Короткий доступ: `TravelTheme.accents.detour`. */
object TravelTheme {
    val accents: TravelAccents
        @Composable @ReadOnlyComposable get() = LocalTravelAccents.current
}
