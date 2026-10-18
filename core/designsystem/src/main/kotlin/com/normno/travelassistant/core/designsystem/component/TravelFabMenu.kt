@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.normno.travelassistant.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp

@Immutable
data class FabAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * Главное действие экрана, раскрывающееся в список.
 *
 * `FloatingActionButtonMenu` из material3 1.5.0 делает то, за чем раньше писали свой
 * bottom sheet: пункты вылетают из кнопки пружиной, сама кнопка морфится в крестик,
 * фон за ней гаснет. Здесь он обёрнут так, чтобы экран задавал только список действий
 * и ничего не знал про анимацию.
 *
 * @param expanded состоянием владеет вызывающий экран: его нужно сбрасывать при
 *   навигации и при системном «назад», а это знает только экран
 */
@Composable
fun TravelFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    actions: List<FabAction>,
    modifier: Modifier = Modifier,
    collapsedIcon: ImageVector = Icons.Rounded.Add,
    collapsedLabel: String = "Добавить",
) {
    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = onExpandedChange,
                containerSize = ToggleFloatingActionButtonDefaults.containerSizeMedium(),
                modifier = Modifier.semantics {
                    isTraversalGroup = true
                    // Скринридеру нужен один понятный узел, а не «кнопка, кнопка, кнопка»:
                    // те же действия отдаются ему списком, без раскрытия меню.
                    traversalIndex = -1f
                    customActions = actions.map { action ->
                        CustomAccessibilityAction(action.label) {
                            action.onClick()
                            true
                        }
                    }
                },
            ) {
                val progress = checkedProgress
                val icon = if (progress > 0.5f) Icons.Rounded.Close else collapsedIcon
                with(ToggleFloatingActionButtonDefaults) {
                    Icon(
                        imageVector = icon,
                        contentDescription = if (expanded) "Закрыть меню" else collapsedLabel,
                        modifier = Modifier
                            .animateIcon({ progress })
                            .size(24.dp),
                    )
                }
            }
        },
    ) {
        actions.forEach { action ->
            FloatingActionButtonMenuItem(
                onClick = {
                    onExpandedChange(false)
                    action.onClick()
                },
                icon = { Icon(action.icon, contentDescription = null) },
                text = { Text(action.label) },
            )
        }
    }
}
