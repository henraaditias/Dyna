package com.example.domain.repository

import com.example.domain.model.ActivityState
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.PermissionSnapshot
import com.example.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
  val settings: Flow<UserSettings>

  suspend fun setOverlayEnabled(enabled: Boolean)

  suspend fun updateAppearance(transform: (IslandAppearance) -> IslandAppearance)

  suspend fun setThemePreset(preset: IslandThemePreset)

  suspend fun updateCustomColors(colors: IslandThemeColors)

  suspend fun setTimerActivityEnabled(enabled: Boolean)

  suspend fun setNotificationActivityEnabled(enabled: Boolean)

  suspend fun setShowNotificationPreview(show: Boolean)

  suspend fun setAutoExpandOnNotification(autoExpand: Boolean)

  suspend fun setStartOnBoot(startOnBoot: Boolean)

  suspend fun resetAppearance()

  suspend fun resetAllSettings()
}

interface ActivityRepository {
  val currentActivity: StateFlow<ActivityState>
  val isOverlayExpanded: StateFlow<Boolean>

  fun toggleOverlayExpanded()

  fun setOverlayExpanded(expanded: Boolean)

  fun startTimer(durationSeconds: Int)

  fun toggleTimerPause()

  fun stopTimer()

  fun onNotificationPosted(
    key: String,
    appName: String,
    title: String,
    text: String?,
    autoExpand: Boolean,
  )

  fun onNotificationRemoved(key: String)

  fun dismissActiveNotification()
}

interface PermissionStateProvider {
  val permissions: StateFlow<PermissionSnapshot>

  fun refresh()

  fun openOverlaySettings(): Boolean

  fun openNotificationAccessSettings(): Boolean
}
