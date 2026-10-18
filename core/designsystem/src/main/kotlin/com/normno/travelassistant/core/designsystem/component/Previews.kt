package com.normno.travelassistant.core.designsystem.component

import androidx.compose.ui.tooling.preview.Preview

/**
 * Один аннотационный набор на все превью: любой компонент обязан выглядеть
 * прилично в светлой и тёмной теме и при крупном шрифте. Проверять это глазами
 * в трёх отдельных превью никто не станет — поэтому они собраны здесь.
 */
@Preview(name = "Светлая", group = "themes")
@Preview(name = "Тёмная", group = "themes", uiMode = 0x20)
@Preview(name = "Крупный шрифт", group = "a11y", fontScale = 1.5f)
annotation class TravelPreviews

@Preview(name = "Телефон", device = "spec:width=411dp,height=891dp")
@Preview(name = "Планшет", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Складной", device = "spec:width=673dp,height=841dp")
annotation class DevicePreviews
