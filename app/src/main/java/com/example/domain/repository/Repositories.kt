package com.example.domain.repository

import com.example.domain.model.ActivityState
import com.example.domain.model.ActivityType
import com.example.domain.model.AppLanguage
import com.example.domain.model.BatterySnapshot
import com.example.domain.model.CallSnapshot
import com.example.domain.model.DashboardThemeMode
import com.example.domain.model.ExpressionState
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.MediaSnapshot
import com.example.domain.model.NotificationHistoryEntry
import com.example.domain.model.PermissionSnapshot
import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerSchedule
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.QuickMenuItem
import com.example.domain.model.ReplyCapability
import com.example.domain.model.UserSettings
import com.example.domain.model.WeatherSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
  val settings: Flow<UserSettings>

  suspend fun setOverlayEnabled(enabled: Boolean)

  suspend fun setDashboardThemeMode(mode: DashboardThemeMode)

  suspend fun setLanguage(language: AppLanguage)

  suspend fun updateAppearance(transform: (IslandAppearance) -> IslandAppearance)

  suspend fun setThemePreset(preset: IslandThemePreset)

  suspend fun updateCustomColors(colors: IslandThemeColors)

  suspend fun setCalendarEnabled(enabled: Boolean)

  suspend fun setWeatherEnabled(enabled: Boolean)

  suspend fun setShortcutsEnabled(enabled: Boolean)

  suspend fun setTimerActivityEnabled(enabled: Boolean)

  suspend fun setStopwatchEnabled(enabled: Boolean)

  suspend fun setNotificationActivityEnabled(enabled: Boolean)

  suspend fun setMediaActivityEnabled(enabled: Boolean)

  suspend fun setCallActivityEnabled(enabled: Boolean)

  suspend fun setBatteryActivityEnabled(enabled: Boolean)

  suspend fun setPrayerAlarmEnabled(enabled: Boolean)

  suspend fun setHideTopDotEnabled(enabled: Boolean)

  suspend fun setShowNotificationPreview(show: Boolean)

  suspend fun setAutoExpandOnNotification(autoExpand: Boolean)

  suspend fun setStartOnBoot(startOnBoot: Boolean)

  suspend fun updateLocation(
    cityName: String,
    latitude: Double,
    longitude: Double,
    useGpsLocation: Boolean,
  )

  suspend fun setPinnedShortcuts(packages: List<String>)

  suspend fun addShortcut(packageName: String)

  suspend fun removeShortcut(packageName: String)

  suspend fun reorderShortcut(fromIndex: Int, toIndex: Int)

  suspend fun setShortcutSlot(slotIndex: Int, packageName: String?)

  suspend fun setQuickMenuItems(items: List<QuickMenuItem>)

  suspend fun cacheWeather(weather: WeatherSnapshot)

  suspend fun resetAppearance()

  suspend fun resetAllSettings()
}

interface NotificationHistoryRepository {
  val entries: StateFlow<List<NotificationHistoryEntry>>

  suspend fun recordNotification(
    packageName: String,
    appName: String,
    title: String,
    body: String,
    isChat: Boolean,
  )

  suspend fun deleteEntry(id: String)

  suspend fun clearAll()

  suspend fun purgeIfDayChangedOrRebooted()

  suspend fun onDeviceShutdownOrReboot()
}

interface ActivityRepository {
  val currentActivity: StateFlow<ActivityState>
  val isOverlayExpanded: StateFlow<Boolean>
  val isReplying: StateFlow<Boolean>
  val quickMenuCategory: StateFlow<QuickMenuCategory>
  val isDimmed: StateFlow<Boolean>
  val isIslandHidden: StateFlow<Boolean>
  val isDeviceLocked: StateFlow<Boolean>
  val isLandscape: StateFlow<Boolean>
  val transientExpression: StateFlow<ExpressionState?>
  val batteryState: StateFlow<BatterySnapshot>

  fun toggleOverlayExpanded()

  fun setOverlayExpanded(expanded: Boolean)

  fun setReplying(replying: Boolean)

  fun setQuickMenuCategory(category: QuickMenuCategory)

  fun openQuickMenu(category: QuickMenuCategory = QuickMenuCategory.MENU)

  fun setDimmed(dimmed: Boolean)

  fun hideIsland()

  fun restoreIsland()

  fun setDeviceLocked(locked: Boolean)

  fun setLandscape(landscape: Boolean)

  fun triggerTransientExpression(expression: ExpressionState, durationMs: Long = 2200L)

  fun swapToSecondaryActivity(type: ActivityType)

  fun startTimer(durationSeconds: Int)

  fun toggleTimerPause()

  fun stopTimer()

  fun startStopwatch()

  fun toggleStopwatchPause()

  fun recordStopwatchLap()

  fun stopStopwatch()

  fun onNotificationPosted(
    key: String,
    appName: String,
    title: String,
    text: String?,
    autoExpand: Boolean,
    replyCapability: ReplyCapability? = null,
    hasMarkReadAction: Boolean = false,
    hasContentIntent: Boolean = false,
    sourcePackageName: String? = null,
  )

  fun onNotificationRemoved(key: String)

  fun dismissActiveNotification()

  fun onMediaUpdated(media: MediaSnapshot?)

  fun onCallPosted(call: CallSnapshot, autoExpand: Boolean = true)

  fun onCallRemoved(key: String)

  fun toggleCallMute(muted: Boolean? = null): Boolean

  fun toggleCallVideo(): Boolean

  fun dismissCall()

  fun onBatteryUpdated(snapshot: BatterySnapshot, showChargingBanner: Boolean)

  fun dismissChargingBanner()

  fun dismissLowBatteryAlert()

  fun triggerPrayerAlarm(prayerName: PrayerName, formattedTime: String, cityName: String)

  fun dismissPrayerAlarm()

  fun triggerTestNotification(autoCollapseSeconds: Int = 5)

  fun triggerTestChatNotification(autoCollapseSeconds: Int = 6)

  fun triggerTestCallAndMedia()

  fun toggleSimulatedMediaPlayPause()

  fun toggleSimulatedMediaFavorite()
}

interface PermissionStateProvider {
  val permissions: StateFlow<PermissionSnapshot>

  fun refresh()

  fun openOverlaySettings(): Boolean

  fun openNotificationAccessSettings(): Boolean
}

interface WeatherAndPrayerProvider {
  val weather: StateFlow<WeatherSnapshot>
  val prayerSchedule: StateFlow<PrayerSchedule>

  fun refreshSchedule()

  suspend fun refreshWeather(force: Boolean = false)

  suspend fun searchCity(query: String): List<com.example.domain.model.CitySearchResult>

  suspend fun selectCity(cityName: String, latitude: Double, longitude: Double, isGps: Boolean)
}
