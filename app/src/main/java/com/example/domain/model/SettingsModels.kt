package com.example.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.math.max
import kotlin.math.min

enum class IslandThemePreset(val displayName: String) {
  MIDNIGHT("Midnight"),
  MINIMAL("Minimal"),
  AURORA("Aurora"),
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
    val Midnight =
      IslandThemeColors(
        backgroundArgb = 0xFF050507,
        foregroundArgb = 0xFFFFFFFF,
        accentArgb = 0xFF38BDF8,
        iconArgb = 0xFF38BDF8,
      )

    val Minimal =
      IslandThemeColors(
        backgroundArgb = 0xFF141416,
        foregroundArgb = 0xFFE5E1E4,
        accentArgb = 0xFFFFFFFF,
        iconArgb = 0xFFBDC8D1,
      )

    val Aurora =
      IslandThemeColors(
        backgroundArgb = 0xFF08121A,
        foregroundArgb = 0xFFF0F9FF,
        accentArgb = 0xFF10B981,
        iconArgb = 0xFF60A5FA,
      )
  }
}

data class IslandAppearance(
  val widthDp: Float = 148f,
  val heightDp: Float = 36f,
  val cornerRadiusDp: Float = 24f,
  val topOffsetDp: Float = 8f,
  val animationEnabled: Boolean = true,
  val animationSpeedMultiplier: Float = 1.0f,
  val autoCollapseSeconds: Int = 5,
  val themePreset: IslandThemePreset = IslandThemePreset.MIDNIGHT,
  val customColors: IslandThemeColors = IslandThemeColors.Midnight,
) {
  fun resolvedColors(): IslandThemeColors =
    when (themePreset) {
      IslandThemePreset.MIDNIGHT -> IslandThemeColors.Midnight
      IslandThemePreset.MINIMAL -> IslandThemeColors.Minimal
      IslandThemePreset.AURORA -> IslandThemeColors.Aurora
      IslandThemePreset.CUSTOM -> customColors
    }
}

data class UserSettings(
  val isOverlayEnabled: Boolean = false,
  val appearance: IslandAppearance = IslandAppearance(),
  val timerActivityEnabled: Boolean = true,
  val notificationActivityEnabled: Boolean = true,
  val showNotificationPreview: Boolean = true,
  val autoExpandOnNotification: Boolean = true,
  val startOnBoot: Boolean = false,
)

data class PermissionSnapshot(
  val hasOverlayPermission: Boolean = false,
  val hasNotificationAccess: Boolean = false,
  val hasPostNotificationsPermission: Boolean = true,
)
