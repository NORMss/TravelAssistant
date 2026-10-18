@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.normno.travelassistant.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.normno.travelassistant.core.designsystem.theme.Motion

/**
 * Карточка, которая действительно висит над фоном: без границы, с тенью и заметным
 * скруглением. Нажатие чуть вжимает её пружиной из темы — отклик на касание важнее
 * любой декорации, потому что по этим карточкам тыкают на ходу.
 */
@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    elevation: Dp = 3.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = Motion.fastSpatial(),
        label = "cardPress",
    )

    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val shape = MaterialTheme.shapes.large

    if (onClick == null) {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            content = content,
        )
    } else {
        Card(
            onClick = onClick,
            modifier = modifier.scale(scale),
            shape = shape,
            colors = colors,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            interactionSource = interaction,
            content = content,
        )
    }
}

/**
 * Контейнер формы из библиотеки M3 Expressive: обложка поездки, аватар, метка места.
 *
 * Форма — не украшение, а способ отличить сущности на одном экране, не прибегая к
 * цвету: цвет здесь задают обои пользователя, и опираться на него нельзя.
 */
@Composable
fun ShapedSurface(
    polygon: RoundedPolygon,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier,
        shape = polygon.toShape(),
        color = containerColor,
    ) {
        Box(modifier = Modifier.clip(polygon.toShape()), content = content)
    }
}
