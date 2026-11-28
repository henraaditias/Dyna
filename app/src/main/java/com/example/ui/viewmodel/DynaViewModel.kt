package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.domain.model.ActivityState
import com.example.domain.model.AppLanguage
import com.example.domain.model.BatterySnapshot
import com.example.domain.model.CitySearchResult
import com.example.domain.model.DashboardThemeMode
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.NotificationHistoryEntry
import com.example.domain.model.PermissionSnapshot
import com.example.domain.model.PrayerSchedule
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.QuickMenuItem
import com.example.domain.model.ShortcutAppItem
import com.example.domain.model.UserSettings
import com.example.domain.model.WeatherSnapshot
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.NotificationHistoryRepository
import com.example.domain.repository.PermissionStateProvider
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.WeatherAndPrayerProvider
import com.example.platform.location.CoarseLocationHelper
import com.example.platform.overlay.OverlayWindowController
import com.example.platform.shortcuts.AppShortcutsProvider
import com.example.service.DynaOverlayService
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DynaUiState(
  val settings: UserSettings = UserSettings(),
  val activity: ActivityState = ActivityState.Idle,
  val isOverlayExpanded: Boolean = false,
  val isReplying: Boolean = false,
  val isDimmed: Boolean = false,
  val quickMenuCategory: QuickMenuCategory = QuickMenuCategory.NONE,
  val permissions: PermissionSnapshot = PermissionSnapshot(),
  val isOverlayAttached: Boolean = false,
  val isIslandHidden: Boolean = false,
  val statusMessage: String? = null,
  val weather: WeatherSnapshot = WeatherSnapshot(),
  val prayerSchedule: PrayerSchedule = PrayerSchedule(),
  val batteryState: BatterySnapshot = BatterySnapshot(),
  val citySearchResults: List<CitySearchResult> = emptyList(),
  val isSearchingCity: Boolean = false,
  val installedApps: List<ShortcutAppItem> = emptyList(),
  val notificationHistory: List<NotificationHistoryEntry> = emptyList(),
)

private data class LocalFeatureState(
  val weather: WeatherSnapshot,
  val prayerSchedule: PrayerSchedule,
  val batteryState: BatterySnapshot,
  val citySearchResults: List<CitySearchResult>,
  val isSearchingCity: Boolean,
  val isIslandHidden: Boolean,
)

private data class OverlayPreviewState(
  val expanded: Boolean,
  val replying: Boolean,
  val dimmed: Boolean,
  val quickMenuCategory: QuickMenuCategory,
)

class DynaViewModel(
  private val application: Application,
  private val settingsRepository: SettingsRepository,
  private val notificationHistoryRepository: NotificationHistoryRepository,
  private val activityRepository: ActivityRepository,
  private val permissionStateProvider: PermissionStateProvider,
  private val overlayWindowController: OverlayWindowController,
  private val weatherAndPrayerProvider: WeatherAndPrayerProvider,
  private val appShortcutsProvider: AppShortcutsProvider,
  private val coarseLocationHelper: CoarseLocationHelper,
) : ViewModel() {

  private val statusMessage = MutableStateFlow<String?>(null)
  private val citySearchResults = MutableStateFlow<List<CitySearchResult>>(emptyList())
  private val isSearchingCity = MutableStateFlow(false)
  private val installedApps = MutableStateFlow<List<ShortcutAppItem>>(emptyList())

  private val localFeatureFlow =
    combine(
      weatherAndPrayerProvider.weather,
      weatherAndPrayerProvider.prayerSchedule,
      activityRepository.batteryState,
      combine(citySearchResults, isSearchingCity) { res, searching -> res to searching },
      activityRepository.isIslandHidden,
    ) { weather, schedule, battery, searchPair, hidden ->
      LocalFeatureState(
        weather = weather,
        prayerSchedule = schedule,
        batteryState = battery,
        citySearchResults = searchPair.first,
        isSearchingCity = searchPair.second,
        isIslandHidden = hidden,
      )
    }

  private val overlayPreviewFlow =
    combine(
      activityRepository.isOverlayExpanded,
      activityRepository.isReplying,
      activityRepository.isDimmed,
      activityRepository.quickMenuCategory,
    ) { expanded, replying, dimmed, category ->
      OverlayPreviewState(
        expanded = expanded,
        replying = replying,
        dimmed = dimmed,
        quickMenuCategory = category,
      )
    }

  val uiState: StateFlow<DynaUiState> =
    combine(
        combine(
          settingsRepository.settings,
          activityRepository.currentActivity,
          permissionStateProvider.permissions,
          overlayWindowController.isAttached,
          statusMessage,
        ) { settings, activity, permissions, attached, message ->
          DynaUiState(
            settings = settings,
            activity = activity,
            permissions = permissions,
            isOverlayAttached = attached,
            statusMessage = message,
          )
        },
        localFeatureFlow,
        overlayPreviewFlow,
        installedApps,
        notificationHistoryRepository.entries,
      ) { base, features, preview, apps, history ->
        base.copy(
          weather = features.weather,
          prayerSchedule = features.prayerSchedule,
          batteryState = features.batteryState,
          citySearchResults = features.citySearchResults,
          isSearchingCity = features.isSearchingCity,
          isIslandHidden = features.isIslandHidden,
          isOverlayExpanded = preview.expanded,
          isReplying = preview.replying,
          isDimmed = preview.dimmed,
          quickMenuCategory = preview.quickMenuCategory,
          installedApps = apps,
          notificationHistory = history,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DynaUiState(),
      )

  init {
    refreshInstalledApps()
    viewModelScope.launch {
      notificationHistoryRepository.purgeIfDayChangedOrRebooted()
      weatherAndPrayerProvider.refreshWeather(force = false)
      // Automatically turn on overlay when core permissions are already granted on first entry
      permissionStateProvider.refresh()
      val perms = permissionStateProvider.permissions.value
      val current = settingsRepository.settings.first()
      if (perms.hasOverlayPermission && current.isOverlayEnabled && !overlayWindowController.isAttached.value) {
        DynaOverlayService.start(application)
      }
    }
  }

  fun refreshPermissions() {
    permissionStateProvider.refresh()
    viewModelScope.launch {
      notificationHistoryRepository.purgeIfDayChangedOrRebooted()
      val snapshot = permissionStateProvider.permissions.value
      val currentSettings = settingsRepository.settings.first()
      if (
        currentSettings.isOverlayEnabled &&
          snapshot.hasOverlayPermission &&
          !overlayWindowController.isAttached.value
      ) {
        DynaOverlayService.start(application)
      } else if (!snapshot.hasOverlayPermission && overlayWindowController.isAttached.value) {
        DynaOverlayService.stop(application)
      }
    }
  }

  fun setOverlayEnabled(enabled: Boolean) {
    viewModelScope.launch {
      permissionStateProvider.refresh()
      val hasOverlayPerm = permissionStateProvider.permissions.value.hasOverlayPermission
      if (enabled && !hasOverlayPerm) {
        settingsRepository.setOverlayEnabled(false)
        statusMessage.value =
          "Berikan izin Tampil di Atas Aplikasi Lain terlebih dahulu."
        return@launch
      }
      settingsRepository.setOverlayEnabled(enabled)
      if (enabled) {
        DynaOverlayService.start(application)
      } else {
        DynaOverlayService.stop(application)
      }
    }
  }

  fun openOverlaySettings() {
    val launched = permissionStateProvider.openOverlaySettings()
    if (!launched) {
      statusMessage.value =
        "Tidak dapat membuka pengaturan overlay secara langsung di perangkat ini."
    }
  }

  fun openNotificationAccessSettings() {
    val launched = permissionStateProvider.openNotificationAccessSettings()
    if (!launched) {
      statusMessage.value =
        "Tidak dapat membuka pengaturan akses notifikasi secara langsung di perangkat ini."
    }
  }

  fun clearStatusMessage() {
    statusMessage.value = null
  }

  fun setDashboardThemeMode(mode: DashboardThemeMode) {
    viewModelScope.launch { settingsRepository.setDashboardThemeMode(mode) }
  }

  fun setLanguage(language: AppLanguage) {
    viewModelScope.launch { settingsRepository.setLanguage(language) }
  }

  fun setCalendarEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setCalendarEnabled(enabled) }
  }

  fun setWeatherEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setWeatherEnabled(enabled) }
  }

  fun setShortcutsEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setShortcutsEnabled(enabled) }
  }

  fun setHideTopDotEnabled(enabled: Boolean) {
    viewModelScope.launch {
      settingsRepository.setHideTopDotEnabled(enabled)
      if (!enabled) activityRepository.restoreIsland()
    }
  }

  fun startTimer(durationSeconds: Int) {
    activityRepository.startTimer(durationSeconds)
  }

  fun toggleTimerPause() {
    activityRepository.toggleTimerPause()
  }

  fun stopTimer() {
    activityRepository.stopTimer()
  }

  fun startStopwatch() {
    activityRepository.startStopwatch()
  }

  fun toggleStopwatchPause() {
    activityRepository.toggleStopwatchPause()
  }

  fun recordStopwatchLap() {
    activityRepository.recordStopwatchLap()
  }

  fun stopStopwatch() {
    activityRepository.stopStopwatch()
  }

  fun updateAppearance(transform: (IslandAppearance) -> IslandAppearance) {
    viewModelScope.launch { settingsRepository.updateAppearance(transform) }
  }

  fun setThemePreset(preset: IslandThemePreset) {
    viewModelScope.launch { settingsRepository.setThemePreset(preset) }
  }

  fun updateCustomColors(colors: IslandThemeColors) {
    viewModelScope.launch { settingsRepository.updateCustomColors(colors) }
  }

  fun setTimerActivityEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setTimerActivityEnabled(enabled) }
  }

  fun setStopwatchEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setStopwatchEnabled(enabled) }
  }

  fun setNotificationActivityEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setNotificationActivityEnabled(enabled) }
  }

  fun setMediaActivityEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setMediaActivityEnabled(enabled) }
  }

  fun setCallActivityEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setCallActivityEnabled(enabled) }
  }

  fun setBatteryActivityEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setBatteryActivityEnabled(enabled) }
  }

  fun setPrayerAlarmEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setPrayerAlarmEnabled(enabled) }
  }

  fun setShowNotificationPreview(show: Boolean) {
    viewModelScope.launch { settingsRepository.setShowNotificationPreview(show) }
  }

  fun setAutoExpandOnNotification(autoExpand: Boolean) {
    viewModelScope.launch { settingsRepository.setAutoExpandOnNotification(autoExpand) }
  }

  fun setStartOnBoot(startOnBoot: Boolean) {
    viewModelScope.launch { settingsRepository.setStartOnBoot(startOnBoot) }
  }

  fun refreshWeather(force: Boolean = true) {
    viewModelScope.launch { weatherAndPrayerProvider.refreshWeather(force = force) }
  }

  fun searchCities(query: String) {
    val trimmed = query.trim()
    if (trimmed.length < 2) {
      citySearchResults.value = emptyList()
      return
    }
    viewModelScope.launch {
      isSearchingCity.value = true
      citySearchResults.value = weatherAndPrayerProvider.searchCity(trimmed)
      isSearchingCity.value = false
    }
  }

  fun clearCitySearch() {
    citySearchResults.value = emptyList()
  }

  fun selectCity(cityName: String, latitude: Double, longitude: Double, isGps: Boolean = false) {
    viewModelScope.launch {
      citySearchResults.value = emptyList()
      weatherAndPrayerProvider.selectCity(cityName, latitude, longitude, isGps)
      statusMessage.value = "Lokasi diperbarui ke $cityName."
    }
  }

  fun useCurrentGpsLocation() {
    permissionStateProvider.refresh()
    viewModelScope.launch {
      if (!coarseLocationHelper.hasPermission()) {
        statusMessage.value = "Izin lokasi belum diberikan."
        return@launch
      }
      val loc = coarseLocationHelper.getCurrentCoarseLocation()
      if (loc == null) {
        statusMessage.value = "Gagal mendeteksi lokasi perangkat. Pilih kota secara manual."
        return@launch
      }
      weatherAndPrayerProvider.selectCity(loc.cityName, loc.latitude, loc.longitude, isGps = true)
      statusMessage.value = "Lokasi diperbarui: ${loc.cityName}."
    }
  }

  fun refreshInstalledApps() {
    viewModelScope.launch {
      val apps =
        withContext(Dispatchers.IO) { appShortcutsProvider.queryInstalledLaunchableApps() }
      installedApps.value = apps
    }
  }

  fun getAppIconBitmap(packageName: String): Bitmap? =
    appShortcutsProvider.getAppIconBitmap(packageName)

  fun addShortcut(packageName: String) {
    viewModelScope.launch { settingsRepository.addShortcut(packageName) }
  }

  fun removeShortcut(packageName: String) {
    viewModelScope.launch { settingsRepository.removeShortcut(packageName) }
  }

  fun reorderShortcut(fromIndex: Int, toIndex: Int) {
    viewModelScope.launch { settingsRepository.reorderShortcut(fromIndex, toIndex) }
  }

  fun setShortcutSlot(slotIndex: Int, packageName: String?) {
    viewModelScope.launch { settingsRepository.setShortcutSlot(slotIndex, packageName) }
  }

  fun deleteNotificationHistoryEntry(id: String) {
    viewModelScope.launch { notificationHistoryRepository.deleteEntry(id) }
  }

  fun clearNotificationHistory() {
    viewModelScope.launch { notificationHistoryRepository.clearAll() }
  }

  fun togglePreviewOverlayExpanded() {
    activityRepository.toggleOverlayExpanded()
  }

  fun setPreviewQuickMenuCategory(category: QuickMenuCategory) {
    activityRepository.setQuickMenuCategory(category)
  }

  fun triggerTestNotification() {
    val autoSec = uiState.value.settings.appearance.autoCollapseSeconds
    activityRepository.triggerTestNotification(autoSec)
  }

  fun triggerTestChatNotification() {
    val autoSec = uiState.value.settings.appearance.autoCollapseSeconds
    activityRepository.triggerTestChatNotification(autoSec)
  }

  fun triggerTestCallAndMedia() {
    activityRepository.triggerTestCallAndMedia()
  }

  fun toggleSimulatedMediaPlayPause() {
    activityRepository.toggleSimulatedMediaPlayPause()
  }

  fun toggleSimulatedMediaFavorite() {
    activityRepository.toggleSimulatedMediaFavorite()
  }

  fun toggleCallMute() {
    activityRepository.toggleCallMute()
  }

  fun toggleCallVideo() {
    activityRepository.toggleCallVideo()
  }

  fun dismissCall() {
    activityRepository.dismissCall()
  }

  fun dismissActiveNotification() {
    activityRepository.dismissActiveNotification()
  }

  fun startReplyInPreview() {
    activityRepository.setReplying(true)
  }

  fun cancelReplyInPreview() {
    activityRepository.setReplying(false)
  }

  fun sendSimulatedReply() {
    activityRepository.dismissActiveNotification()
  }

  fun launchApp(packageName: String) {
    activityRepository.setOverlayExpanded(false)
    appShortcutsProvider.launchApp(packageName)
  }

  fun openGoogleCalendarForDate(date: LocalDate) {
    activityRepository.setOverlayExpanded(false)
    appShortcutsProvider.openGoogleCalendarForDate(date)
  }

  fun toggleQuickMenuItem(item: QuickMenuItem) {
    viewModelScope.launch {
      val current = settingsRepository.settings.first().quickMenuItems.toMutableList()
      if (current.contains(item)) {
        if (current.size <= 1) {
          statusMessage.value = "Minimal 1 ikon menu harus aktif."
          return@launch
        }
        current.remove(item)
      } else {
        current.add(item)
      }
      settingsRepository.setQuickMenuItems(current)
    }
  }

  fun moveQuickMenuItem(item: QuickMenuItem, delta: Int) {
    viewModelScope.launch {
      val current = settingsRepository.settings.first().quickMenuItems.toMutableList()
      val idx = current.indexOf(item)
      if (idx == -1) return@launch
      val nextIdx = (idx + delta).coerceIn(0, current.lastIndex)
      if (idx == nextIdx) return@launch
      current.removeAt(idx)
      current.add(nextIdx, item)
      settingsRepository.setQuickMenuItems(current)
    }
  }

  fun hideIsland() {
    activityRepository.hideIsland()
  }

  fun restoreIsland() {
    activityRepository.restoreIsland()
  }

  fun swapSecondaryActivity(type: com.example.domain.model.ActivityType) {
    activityRepository.swapToSecondaryActivity(type)
  }

  fun resetAppearance() {
    viewModelScope.launch {
      settingsRepository.resetAppearance()
      statusMessage.value = "Ukuran kapsul dikembalikan ke setelan awal."
    }
  }

  fun resetAllSettings() {
    viewModelScope.launch {
      activityRepository.stopTimer()
      activityRepository.stopStopwatch()
      activityRepository.dismissActiveNotification()
      activityRepository.dismissCall()
      activityRepository.dismissChargingBanner()
      activityRepository.dismissLowBatteryAlert()
      activityRepository.dismissPrayerAlarm()
      DynaOverlayService.stop(application)
      settingsRepository.resetAllSettings()
      weatherAndPrayerProvider.refreshSchedule()
      statusMessage.value = "Semua pengaturan Dyna dikembalikan ke awal."
    }
  }

  companion object {
    fun factory(container: AppContainer): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return DynaViewModel(
            application = container.application,
            settingsRepository = container.settingsRepository,
            notificationHistoryRepository = container.notificationHistoryRepository,
            activityRepository = container.activityRepository,
            permissionStateProvider = container.permissionStateProvider,
            overlayWindowController = container.overlayWindowController,
            weatherAndPrayerProvider = container.weatherAndPrayerProvider,
            appShortcutsProvider = container.appShortcutsProvider,
            coarseLocationHelper = container.coarseLocationHelper,
          ) as T
        }
      }
  }
}
