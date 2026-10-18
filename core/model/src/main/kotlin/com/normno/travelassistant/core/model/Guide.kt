package com.normno.travelassistant.core.model

/**
 * Подсказка при первом открытии экрана.
 *
 * Ключ — не строка по месту вызова, а запись в этом перечислении. Причина
 * практическая: подсказки показываются один раз и запоминаются навсегда, а
 * опечатка в строковом ключе означает подсказку, которая либо не покажется
 * никогда, либо покажется каждый раз. Компилятор такое ловит, строка — нет.
 *
 * Порядок объявления внутри экрана задаёт порядок показа в туре.
 */
enum class GuideKey(val tour: GuideTour, val order: Int) {

    /* --- «Сейчас» --- */
    TODAY_CURRENT_STOP(GuideTour.TODAY, 0),
    TODAY_COUNTDOWN(GuideTour.TODAY, 1),
    TODAY_ADDRESS_ACTIONS(GuideTour.TODAY, 2),

    /* --- план --- */
    ITINERARY_DAY_CHIPS(GuideTour.ITINERARY, 0),
    ITINERARY_FORKS(GuideTour.ITINERARY, 1),

    /* --- каталог маршрутов --- */
    CATALOG_TEMPLATE_CARD(GuideTour.CATALOG, 0),
    CATALOG_APPLY(GuideTour.CATALOG, 1),

    /* --- камера --- */
    CAMERA_RECOGNITION(GuideTour.CAMERA, 0),
    CAMERA_REGION_PACK(GuideTour.CAMERA, 1),

    /* --- общая навигация, показывается один раз за всё время --- */
    APP_FLOATING_NAV(GuideTour.FIRST_RUN, 0),
    APP_FAB_MENU(GuideTour.FIRST_RUN, 1),
}

/** Тур — набор подсказок одного экрана. Пропускается целиком одной кнопкой. */
enum class GuideTour(val id: String) {
    FIRST_RUN("first-run"),
    TODAY("today"),
    ITINERARY("itinerary"),
    CATALOG("catalog"),
    CAMERA("camera"),
    ;

    companion object {
        fun byId(id: String): GuideTour? = entries.firstOrNull { it.id == id }
    }
}
