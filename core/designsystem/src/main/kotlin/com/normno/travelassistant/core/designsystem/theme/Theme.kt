@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.normno.travelassistant.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Тема приложения.
 *
 * Два решения, которые стоит знать заранее:
 *
 * 1. `MaterialExpressiveTheme`, а не `MaterialTheme`. Разница не в наборе компонентов,
 *    а в значениях по умолчанию: схема движения становится expressive, и все
 *    material-компоненты начинают двигаться пружинами с перелётом.
 *
 * 2. Цвета по системе — основной путь, а не опция. Своей фирменной палитры у приложения
 *    нет: на Android 12+ схему задают обои пользователя. Запасная палитра включается
 *    только на старых версиях и когда пользователь выключил динамику сам.
 */
@Composable
fun TravelAssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val scheme: ColorScheme = remember(darkTheme, dynamicColor, supportsDynamic) {
        when {
            dynamicColor && supportsDynamic ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> FallbackDarkScheme
            else -> expressiveLightColorScheme()
        }
    }

    val accents = remember(scheme, darkTheme) { accentsFor(scheme, darkTheme) }

    CompositionLocalProvider(LocalTravelAccents provides accents) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            motionScheme = MotionScheme.expressive(),
            shapes = TravelShapes,
            typography = TravelTypography,
            content = content,
        )
    }
}
