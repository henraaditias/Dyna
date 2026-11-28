package com.example

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.DashboardThemeMode
import com.example.domain.model.UserSettings
import com.example.ui.navigation.DynaApp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    val startRoute = intent?.getStringExtra(EXTRA_START_ROUTE)
    val appContainer = (application as DynaApplication).container
    setContent {
      val settings by
        appContainer.settingsRepository.settings.collectAsStateWithLifecycle(
          initialValue = UserSettings()
        )
      val isDark = settings.dashboardThemeMode == DashboardThemeMode.DARK

      LaunchedEffect(isDark) {
        val barStyle =
          if (isDark) SystemBarStyle.dark(Color.TRANSPARENT)
          else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
      }

      DynaApp(appContainer = appContainer, initialRoute = startRoute)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
  }

  companion object {
    const val EXTRA_START_ROUTE = "extra_start_route"
  }
}
