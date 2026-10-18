package com.normno.travelassistant.core.common

import java.util.UUID

/**
 * Новый id выдаётся на телефоне и сразу годится для синхронизации: два устройства
 * одного пользователя не столкнутся, даже если оба были офлайн.
 */
fun newId(): String = UUID.randomUUID().toString()

/** Якорь для Markdown-обмена планом: короткий, читаемый, устойчивый к правке руками. */
fun shortAnchor(prefix: String, index: Int): String = "$prefix$index"
