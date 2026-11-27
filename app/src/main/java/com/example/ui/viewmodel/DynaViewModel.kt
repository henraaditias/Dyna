package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AppContainer
import com.example.domain.model.ActivityState
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.PermissionSnapshot
import com.example.domain.model.UserSettings
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.PermissionStateProvider
import com.example.domain.repository.SettingsRepository
import com.example.platform.overlay.OverlayWindowController
import com.example.service.DynaOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DynaUiState(
  val settings: UserSettings = UserSettings(),
  val activity: ActivityState = ActivityState.Idle,
  val permissions: PermissionSnapshot = PermissionSnapshot(),
  val isOverlayAttached: Boolean = false,
  val statusMessage: String? = null,
)

class DynaViewModel(
  private val application: Application,
  private val settingsRepository: SettingsRepository,
  private val activityRepository: ActivityRepository,
  private val permissionStateProvider: PermissionStateProvider,
  private val overlayWindowController: OverlayWindowController,
) : ViewModel() {

  private val statusMessage = MutableStateFlow<String?>(null)

  val uiState: StateFlow<DynaUiState> =
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
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DynaUiState(),
      )

  fun refreshPermissions() {
    permissionStateProvider.refresh()
    viewModelScope.launch {
      val snapshot = permissionStateProvider.permissions.value
      val currentSettings = settingsRepository.settings.first()
      if (currentSettings.isOverlayEnabled && snapshot.hasOverlayPermission && !overlayWindowController.isAttached.value) {
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

  fun startTimer(durationSeconds: Int) {
    activityRepository.startTimer(durationSeconds)
  }

  fun toggleTimerPause() {
    activityRepository.toggleTimerPause()
  }

  fun stopTimer() {
    activityRepository.stopTimer()
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

  fun setNotificationActivityEnabled(enabled: Boolean) {
    viewModelScope.launch { settingsRepository.setNotificationActivityEnabled(enabled) }
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

  fun resetAppearance() {
    viewModelScope.launch {
      settingsRepository.resetAppearance()
      statusMessage.value = "Geometri kapsul dikembalikan ke bawaan."
    }
  }

  fun resetAllSettings() {
    viewModelScope.launch {
      activityRepository.stopTimer()
      activityRepository.dismissActiveNotification()
      DynaOverlayService.stop(application)
      settingsRepository.resetAllSettings()
      statusMessage.value = "Semua setelan Dyna dikembalikan ke bawaan."
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
            activityRepository = container.activityRepository,
            permissionStateProvider = container.permissionStateProvider,
            overlayWindowController = container.overlayWindowController,
          ) as T
        }
      }
  }
}
