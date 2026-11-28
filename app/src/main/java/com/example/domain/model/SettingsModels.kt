package com.example.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

enum class DashboardThemeMode {
  DARK,
  LIGHT,
}

enum class AppLanguage(val code: String, val label: String) {
  ID("id", "Bahasa Indonesia"),
  EN("en", "English"),
}

enum class IslandThemePreset(val displayName: String) {
  MIDNIGHT("Charcoal"),
  MINIMAL("Minimal"),
  AURORA("Contrast"),
  CUSTOM("Custom"),
}

data class IslandThemeColors(
  val backgroundArgb: Long,
  val foregroundArgb: Long,
  val accentArgb: Long,
  val iconArgb: Long,
) {
  fun contrastRatio(): Float {
    val l1 = Color(foregroundArgb).luminance()
    val l2 = Color(backgroundArgb).luminance()
    val brightest = max(l1, l2)
    val darkest = min(l1, l2)
    return (brightest + 0.05f) / (darkest + 0.05f)
  }

  val hasReadableContrast: Boolean
    get() = contrastRatio() >= 4.5f

  companion object {
    // Strictly soft charcoal and off-white monochrome overlay tokens (no #000000 or #FFFFFF)
    val Midnight =
      IslandThemeColors(
        backgroundArgb = 0xFF141416,
        foregroundArgb = 0xFFF1F1F3,
        accentArgb = 0xFFF1F1F3,
        iconArgb = 0xFF9898A2,
      )

    val Minimal =
      IslandThemeColors(
        backgroundArgb = 0xFF19191C,
        foregroundArgb = 0xFFF1F1F3,
        accentArgb = 0xFFD4D4DC,
        iconArgb = 0xFF9898A2,
      )

    val Aurora =
      IslandThemeColors(
        backgroundArgb = 0xFF1F1F23,
        foregroundArgb = 0xFFF4F4F5,
        accentArgb = 0xFFE4E4E7,
        iconArgb = 0xFF9898A2,
      )
  }
}

data class IslandAppearance(
  val widthDp: Float = 152f,
  val heightDp: Float = 36f,
  val cornerRadiusDp: Float = 24f,
  val topOffsetDp: Float = 10f,
  val horizontalOffsetDp: Float = 0f,
  val notificationWidthDp: Float = 328f,
  val notificationMaxHeightDp: Float = 156f,
  val notificationOpacity: Float = 0.96f,
  val menuWidthDp: Float = 332f,
  val menuHeightDp: Float = 264f,
  val animationEnabled: Boolean = true,
  val animationSpeedMultiplier: Float = 1.0f,
  val autoCollapseSeconds: Int = 5,
  val autoDimDelaySeconds: Int = 6,
  val themePreset: IslandThemePreset = IslandThemePreset.MIDNIGHT,
  val customColors: IslandThemeColors = IslandThemeColors.Midnight,
  val idleDisplayMode: IdleDisplayMode = IdleDisplayMode.PILL_LEFT_FACE_INFO,
  val idlePlacement: IdlePlacement = IdlePlacement.FLOATING,
  val idleTapAction: IdleTapAction = IdleTapAction.QUICK_MENU,
  val selectedExpression: ExpressionState = ExpressionState.SMILE,
  val autoDimEnabled: Boolean = true,
  val dimmedOpacity: Float = 0.45f,
) {
  fun resolvedColors(): IslandThemeColors = IslandThemeColors.Midnight
}

data class UserSettings(
  val isOverlayEnabled: Boolean = false,
  val dashboardThemeMode: DashboardThemeMode = DashboardThemeMode.DARK,
  val language: AppLanguage = AppLanguage.ID,
  val appearance: IslandAppearance = IslandAppearance(),
  val calendarEnabled: Boolean = true,
  val weatherEnabled: Boolean = true,
  val shortcutsEnabled: Boolean = true,
  val timerActivityEnabled: Boolean = true,
  val stopwatchEnabled: Boolean = true,
  val notificationActivityEnabled: Boolean = true,
  val mediaActivityEnabled: Boolean = true,
  val callActivityEnabled: Boolean = true,
  val batteryActivityEnabled: Boolean = true,
  val prayerAlarmEnabled: Boolean = false,
  val hideTopDotEnabled: Boolean = true,
  val showNotificationPreview: Boolean = true,
  val autoExpandOnNotification: Boolean = true,
  val startOnBoot: Boolean = false,
  val cityName: String = "Jakarta",
  val cityLatitude: Double = -6.2088,
  val cityLongitude: Double = 106.8456,
  val useGpsLocation: Boolean = false,
  val pinnedShortcutPackages: List<String> = emptyList(),
  val shortcutSlots: List<String> = List(SHORTCUT_SLOT_COUNT) { "" },
  val quickMenuItems: List<QuickMenuItem> = QuickMenuItem.defaultItems,
  val cachedWeatherTempC: Float? = null,
  val cachedWeatherCode: Int? = null,
  val cachedWeatherCondition: String? = null,
  val cachedWeatherWindKmh: Float? = null,
  val cachedWeatherIsDay: Boolean = true,
  val cachedWeatherUpdatedAtMs: Long = 0L,
) {
  fun resolvedShortcutSlots(): List<String?> {
    val normalized =
      if (pinnedShortcutPackages.isNotEmpty()) {
        pinnedShortcutPackages
      } else {
        shortcutSlots.filter { it.isNotBlank() }
      }
    return List(MAX_GRID_SHORTCUTS) { idx ->
      normalized.getOrNull(idx)?.takeIf { it.isNotBlank() }
    }
  }

  fun orderedShortcuts(): List<String> =
    if (pinnedShortcutPackages.isNotEmpty()) {
      pinnedShortcutPackages.distinct().take(MAX_GRID_SHORTCUTS)
    } else {
      shortcutSlots.filter { it.isNotBlank() }.distinct().take(MAX_GRID_SHORTCUTS)
    }

  fun enabledQuickMenuItems(): List<QuickMenuItem> =
    quickMenuItems.filter { item ->
      when (item) {
        QuickMenuItem.CALENDAR -> calendarEnabled
        QuickMenuItem.WEATHER -> weatherEnabled
        QuickMenuItem.SHORTCUTS -> shortcutsEnabled
        QuickMenuItem.PRAYER -> prayerAlarmEnabled
        QuickMenuItem.TIMER_1M -> timerActivityEnabled
        QuickMenuItem.STOPWATCH -> stopwatchEnabled
        QuickMenuItem.HIDE_ISLAND -> hideTopDotEnabled
        QuickMenuItem.MOOD_PICKER -> true
      }
    }

  companion object {
    const val SHORTCUT_SLOT_COUNT = 8
    const val MAX_GRID_SHORTCUTS = 16
  }
}

data class PermissionSnapshot(
  val hasOverlayPermission: Boolean = false,
  val hasNotificationAccess: Boolean = false,
  val hasPostNotificationsPermission: Boolean = true,
  val hasLocationPermission: Boolean = false,
  val hasCalendarPermission: Boolean = false,
)
