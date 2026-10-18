package com.normno.travelassistant.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GeoPointTest {

    @Test
    fun `расстояние считается по большому кругу`() {
        val tokyo = GeoPoint(35.6762, 139.6503)
        val seoul = GeoPoint(37.5665, 126.9780)
        // Справочное расстояние Токио — Сеул около 1157 км.
        val km = tokyo.distanceTo(seoul) / 1000
        assertTrue(km in 1140.0..1175.0, "получилось $km км")
    }

    @Test
    fun `расстояние до себя — ноль`() {
        val point = GeoPoint(55.75, 37.62)
        assertEquals(0.0, point.distanceTo(point), absoluteTolerance = 0.001)
    }

    @Test
    fun `координаты вне диапазона отвергаются сразу`() {
        // Битая точка должна ломаться на границе данных, а не всплыть пином
        // посреди океана на экране карты.
        assertFailsWith<IllegalArgumentException> { GeoPoint(91.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { GeoPoint(0.0, 181.0) }
    }
}
