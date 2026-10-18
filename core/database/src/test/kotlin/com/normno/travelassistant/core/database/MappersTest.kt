package com.normno.travelassistant.core.database

import com.normno.travelassistant.core.database.mapper.toEntity
import com.normno.travelassistant.core.database.mapper.toModel
import com.normno.travelassistant.core.model.Address
import com.normno.travelassistant.core.model.GeoPoint
import com.normno.travelassistant.core.model.StopCategory
import com.normno.travelassistant.core.testing.Fixtures
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Круговой обход «домен → таблица → домен». Маппинг пишется руками, значит рано или
 * поздно при добавлении поля кто-нибудь забудет одну из двух сторон — и поле начнёт
 * молча теряться при сохранении. Этот тест ловит ровно такой случай.
 */
class MappersTest {

    @Test
    fun `поездка переживает запись и чтение`() {
        val restored = Fixtures.trip.toEntity().toModel()

        assertThat(restored.title).isEqualTo(Fixtures.trip.title)
        assertThat(restored.startDate).isEqualTo(Fixtures.trip.startDate)
        assertThat(restored.status).isEqualTo(Fixtures.trip.status)
        assertThat(restored.destinations).isEqualTo(Fixtures.trip.destinations)
        assertThat(restored.homeZoneId).isEqualTo("Europe/Moscow")
    }

    @Test
    fun `этап переживает запись и чтение вместе с адресом`() {
        val restored = Fixtures.sensoji.toEntity().toModel()

        assertThat(restored.localTitle).isEqualTo("浅草寺")
        assertThat(restored.transliteration).isEqualTo("Sensō-ji")
        assertThat(restored.startTime).isEqualTo(Fixtures.sensoji.startTime)
        assertThat(restored.category).isEqualTo(StopCategory.SIGHT)
        assertThat(restored.address?.local).isEqualTo("東京都台東区浅草2-3-1")
        assertThat(restored.address?.point).isEqualTo(GeoPoint(35.7148, 139.7967))
    }

    @Test
    fun `этап без адреса не получает пустой адрес`() {
        // Пустой Address вместо null сделал бы «Скопировать» активной кнопкой,
        // которая кладёт в буфер пустую строку.
        val restored = Fixtures.lunch.toEntity().toModel()

        assertThat(restored.address).isNull()
    }

    @Test
    fun `неизвестная категория не роняет чтение`() {
        // План мог прийти из версии приложения новее установленной.
        val row = Fixtures.sensoji.toEntity().copy(category = "TELEPORT")

        assertThat(row.toModel().category).isEqualTo(StopCategory.OTHER)
    }

    @Test
    fun `битые координаты не превращаются в точку посреди океана`() {
        val row = Fixtures.sensoji.toEntity().copy(lat = 999.0, lng = 999.0)
        val restored = row.toModel()

        assertThat(restored.address).isNotNull()
        assertThat(restored.address?.point).isNull()
    }

    @Test
    fun `адрес без координат сохраняет местное написание`() {
        val stop = Fixtures.sensoji.copy(address = Address(local = "浅草", display = "Асакуса"))
        val restored = stop.toEntity().toModel()

        assertThat(restored.address?.local).isEqualTo("浅草")
        assertThat(restored.address?.point).isNull()
    }
}
