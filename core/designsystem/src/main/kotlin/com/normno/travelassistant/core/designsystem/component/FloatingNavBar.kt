@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.normno.travelassistant.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Один раздел в плавающей навигации.
 *
 * @param key то, что уйдёт в back stack при нажатии
 */
@Immutable
data class NavItem(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

/**
 * Навигация, не приклеенная к низу экрана.
 *
 * Обычный `NavigationBar` занимает полосу во всю ширину и визуально прибивает контент
 * к ней. Плавающая пилюля оставляет контент идти под собой до самого края — отсюда
 * ощущение, что экран больше, чем он есть. Это главный элемент, задающий характер
 * интерфейса, поэтому он живёт в дизайн-системе, а не в модуле приложения.
 *
 * Подпись показывается только у выбранного раздела: у остальных иконка понятна из
 * контекста, а шесть подписей в пилюле не поместятся ни на одном телефоне.
 *
 * @param expanded false — панель сворачивается при скролле вниз, освобождая экран
 */
@Composable
fun FloatingNavBar(
    items: List<NavItem>,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    HorizontalFloatingToolbar(
        expanded = expanded,
        modifier = modifier,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        trailingContent = { trailing?.invoke() },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                val labelVisible = selected && expanded

                FilledIconToggleButton(
                    checked = selected,
                    onCheckedChange = { if (!selected) onSelect(item.key) },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        // Невыбранная кнопка не должна иметь своей подложки: пять
                        // одинаковых кружков в пилюле спорят с выбранным разделом.
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.icon,
                        // Когда подпись видна, описание у иконки убирается: иначе
                        // скринридер читает раздел дважды подряд.
                        contentDescription = if (labelVisible) null else item.label,
                        modifier = Modifier.size(24.dp),
                    )
                }

                // Подпись выезжает пружиной из темы — отдельного spec здесь не нужно.
                AnimatedVisibility(visible = labelVisible) {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 2.dp, end = 6.dp),
                    )
                }
            }
        }
    }
}
