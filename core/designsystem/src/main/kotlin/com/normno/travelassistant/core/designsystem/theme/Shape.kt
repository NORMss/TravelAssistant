@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.normno.travelassistant.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon

/**
 * Скругления крупнее материаловских по умолчанию: интерфейс состоит из плавающих
 * карточек и пилюль, а у плавающего элемента край должен читаться как край, а не
 * как случайный срез.
 */
internal val TravelShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/**
 * Формы из библиотеки M3 Expressive, закреплённые за смыслом. Брать их по месту —
 * верный путь к интерфейсу, где одна и та же сущность на двух экранах разной формы.
 */
object TravelShapeTokens {
    /** Обложка поездки и крупные превью. */
    val cover: RoundedPolygon get() = MaterialShapes.Cookie9Sided

    /** Аватар участника поездки. */
    val avatar: RoundedPolygon get() = MaterialShapes.Circle

    /** Текущий этап на экране «Сейчас» — единственный элемент такой формы. */
    val activeStop: RoundedPolygon get() = MaterialShapes.Clover4Leaf

    /** Метка места на карте и в списке. */
    val placePin: RoundedPolygon get() = MaterialShapes.Pill

    /** Пустое состояние: «здесь пока ничего нет». */
    val empty: RoundedPolygon get() = MaterialShapes.Flower

    /** Индикатор загрузки собирается из этой последовательности. */
    val loadingSequence: List<RoundedPolygon> get() = listOf(
        MaterialShapes.SoftBurst,
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Pentagon,
        MaterialShapes.Pill,
        MaterialShapes.Sunny,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Oval,
    )
}
