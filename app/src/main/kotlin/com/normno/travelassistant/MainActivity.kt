package com.normno.travelassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.normno.travelassistant.core.datastore.ThemePreference
import com.normno.travelassistant.core.designsystem.theme.TravelAssistantTheme
import com.normno.travelassistant.navigation.TravelApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Splash держится, пока не прочитана тема: иначе первый кадр уедет в светлую,
        // а второй — в тёмную, и это видно.
        splash.setKeepOnScreenCondition { viewModel.settings.value == null }

        // Контент рисуется под системными панелями. Отступы раздаются экранам через
        // safeDrawing, а не прибиваются к краям: плавающая навигация обязана висеть
        // над содержимым, а не отрезать его.
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            val preference = settings?.theme ?: ThemePreference.SYSTEM

            TravelAssistantTheme(
                darkTheme = when (preference) {
                    ThemePreference.SYSTEM -> isSystemInDarkTheme()
                    ThemePreference.LIGHT -> false
                    ThemePreference.DARK -> true
                },
                dynamicColor = settings?.dynamicColor ?: true,
            ) {
                TravelApp()
            }
        }
    }
}
