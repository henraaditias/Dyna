package com.example

import android.app.Application
import com.example.data.activities.LiveActivityRepository
import com.example.data.settings.DataStoreSettingsRepository
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.PermissionStateProvider
import com.example.domain.repository.SettingsRepository
import com.example.platform.overlay.OverlayWindowController
import com.example.platform.permissions.AndroidPermissionStateProvider

class AppContainer(val application: Application) {
  val settingsRepository: SettingsRepository by lazy { DataStoreSettingsRepository(application) }
  val activityRepository: ActivityRepository by lazy { LiveActivityRepository() }
  val permissionStateProvider: PermissionStateProvider by lazy {
    AndroidPermissionStateProvider(application)
  }
  val overlayWindowController: OverlayWindowController by lazy {
    OverlayWindowController(
      context = application,
      settingsRepository = settingsRepository,
      activityRepository = activityRepository,
    )
  }
}

class DynaApplication : Application() {
  lateinit var container: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    container = AppContainer(this)
  }
}
