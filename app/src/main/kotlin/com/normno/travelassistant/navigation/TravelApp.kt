package com.normno.travelassistant.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.normno.travelassistant.core.designsystem.component.FabAction
import com.normno.travelassistant.core.designsystem.component.FloatingNavBar
import com.normno.travelassistant.core.designsystem.component.NavItem
import com.normno.travelassistant.core.designsystem.component.TravelFabMenu
import com.normno.travelassistant.feature.assistant.AssistantScreen
import com.normno.travelassistant.feature.camera.CameraScreen
import com.normno.travelassistant.feature.catalog.CatalogScreen
import com.normno.travelassistant.feature.itinerary.ItineraryScreen
import com.normno.travelassistant.feature.phrasebook.PhrasebookScreen
import com.normno.travelassistant.feature.places.PlacesScreen
import com.normno.travelassistant.feature.settings.SettingsScreen
import com.normno.travelassistant.feature.today.TodayScreen
import com.normno.travelassistant.feature.trips.TripsScreen

/**
 * Корневой каркас: содержимое во всю высоту, навигация и главное действие — плавают
 * над ним.
 *
 * Здесь нет `Scaffold` с `bottomBar`. Это осознанно: `bottomBar` отрезает полосу
 * экрана себе, и список всегда заканчивается над ней. Плавающая пилюля оставляет
 * контент идти до самого низа, а отступ, чтобы последний элемент не прятался под
 * ней, передаётся экранам как `contentPadding`.
 */
@Composable
fun TravelApp(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(TodayRoute)
    var fabExpanded by rememberSaveable { mutableStateOf(false) }

    val current = backStack.lastOrNull()
    val tabs = remember { bottomTabs() }

    // Высота плавающей навигации плюс системные отступы: ровно столько должен
    // добавить к себе любой скроллящийся экран.
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val direction = LocalLayoutDirection.current
    val screenPadding = PaddingValues(
        start = insets.calculateStartPadding(direction) + SCREEN_GUTTER,
        end = insets.calculateEndPadding(direction) + SCREEN_GUTTER,
        top = insets.calculateTopPadding() + 8.dp,
        bottom = insets.calculateBottomPadding() + FLOATING_UI_HEIGHT,
    )

    Box(modifier = modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize(),
            entryProvider = entryProvider {
                entry<TodayRoute> {
                    TodayScreen(
                        onOpenTrips = { backStack.add(TripsRoute) },
                        onOpenStop = { },
                        contentPadding = screenPadding,
                    )
                }
                entry<TripsRoute> {
                    TripsScreen(
                        onOpenTrip = { id -> backStack.add(ItineraryRoute(id.value)) },
                        contentPadding = screenPadding,
                    )
                }
                entry<ItineraryRoute> { route ->
                    ItineraryScreen(
                        tripId = route.id,
                        onOpenStop = { },
                        contentPadding = screenPadding,
                    )
                }
                entry<CatalogRoute> {
                    CatalogScreen(
                        onTripCreated = { id -> backStack.add(ItineraryRoute(id.value)) },
                        contentPadding = screenPadding,
                    )
                }
                entry<CameraRoute> { CameraScreen(contentPadding = screenPadding) }
                entry<PlacesRoute> { PlacesScreen(contentPadding = screenPadding) }
                entry<PhrasebookRoute> { PhrasebookScreen(contentPadding = screenPadding) }
                entry<AssistantRoute> { AssistantScreen(contentPadding = screenPadding) }
                entry<SettingsRoute> { SettingsScreen(contentPadding = screenPadding) }
            },
        )

        FloatingNavBar(
            items = tabs,
            selectedKey = current?.let { key -> tabs.firstOrNull { it.key == key }?.key },
            onSelect = { key -> backStack.switchTo(key as NavKey) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = insets.calculateBottomPadding() + 16.dp),
        )

        TravelFabMenu(
            expanded = fabExpanded,
            onExpandedChange = { fabExpanded = it },
            actions = fabActions(
                onAssistant = { backStack.switchTo(AssistantRoute) },
                onCamera = { backStack.switchTo(CameraRoute) },
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = insets.calculateBottomPadding() + 96.dp,
                ),
        )
    }
}

/**
 * Переход между разделами не наращивает стек: шесть нажатий по навигации не должны
 * означать шесть нажатий «назад», чтобы выйти.
 */
private fun MutableList<NavKey>.switchTo(key: NavKey) {
    if (lastOrNull() == key) return
    val existing = indexOfFirst { it == key }
    if (existing >= 0) {
        // Раздел уже в стеке — возвращаемся к нему, а не открываем второй раз.
        while (size > existing + 1) removeAt(size - 1)
    } else {
        if (size > 1) subList(1, size).clear()
        if (firstOrNull() != key) add(key)
    }
}

private fun bottomTabs(): List<NavItem> = listOf(
    NavItem(TodayRoute, "Сейчас", Icons.Rounded.Schedule),
    NavItem(TripsRoute, "Поездки", Icons.Rounded.Luggage),
    NavItem(CatalogRoute, "Маршруты", Icons.Rounded.Explore),
    NavItem(PlacesRoute, "Карта", Icons.Rounded.Map),
    NavItem(PhrasebookRoute, "Фразы", Icons.Rounded.Translate),
    NavItem(SettingsRoute, "Ещё", Icons.Rounded.Settings),
)

private fun fabActions(
    onAssistant: () -> Unit,
    onCamera: () -> Unit,
): List<FabAction> = listOf(
    FabAction("Спросить ассистента", Icons.Rounded.AutoAwesome, onAssistant),
    FabAction("Снять в фотоотчёт", Icons.Rounded.PhotoCamera, onCamera),
    FabAction("Добавить этап", Icons.Rounded.EditCalendar) { },
    FabAction("Своё место", Icons.Rounded.Place) { },
)

private val FLOATING_UI_HEIGHT = 96.dp

/** Боковые поля контента. Плавающие карточки не должны касаться краёв экрана. */
private val SCREEN_GUTTER = 16.dp
