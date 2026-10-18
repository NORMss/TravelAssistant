package com.normno.travelassistant.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Типографика Material-дефолтная по размерам и ритму; меняются только насыщенность
 * заголовков и трекинг — экран «Сейчас» читают на ходу, одной рукой, под солнцем.
 */
internal val TravelTypography: Typography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

/**
 * Текст на языке места показывают не себе, а собеседнику: с другой стороны стола,
 * иногда при плохом зрении. Поэтому у него свой кегль, а не `bodyLarge` пожирнее.
 *
 * Иероглифы и деванагари нельзя сжимать по межстрочному, поэтому lineHeight задан
 * с запасом, а не множителем.
 */
val LocalScriptMedium = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp,
    lineHeight = 36.sp,
)

val LocalScriptLarge = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Bold,
    fontSize = 44.sp,
    lineHeight = 58.sp,
)
