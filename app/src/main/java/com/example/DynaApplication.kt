package com.example

import android.app.Application
import com.example.data.activities.LiveActivityRepository
import com.example.data.notifications.LocalNotificationHistoryRepository
import com.example.data.settings.DataStoreSettingsRepository
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.NotificationHistoryRepository
import com.example.domain.repository.PermissionStateProvider
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.WeatherAndPrayerProvider
import com.example.platform.battery.BatteryMonitor
import com.example.platform.location.CoarseLocationHelper
import com.example.platform.media.MediaSessionMonitor
import com.example.platform.notifications.NotificationReplyManager
import com.example.platform.overlay.OverlayWindowController
import com.example.platform.permissions.AndroidPermissionStateProvider
import com.example.platform.prayer.PrayerAlarmScheduler
import com.example.platform.shortcuts.AppShortcutsProvider
import com.example.platform.weather.AndroidWeatherAndPrayerProvider
import com.example.platform.weather.OpenMeteoWeatherClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(val application: Application) {
  private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  val settingsRepository: SettingsRepository by lazy { DataStoreSettingsRepository(application) }
  val notificationHistoryRepository: NotificationHistoryRepository by lazy {
    LocalNotificationHistoryRepository(application)
  }
  val activityRepository: ActivityRepository by lazy { LiveActivityRepository() }
  val notificationReplyManager: NotificationReplyManager by lazy { NotificationReplyManager() }
  val permissionStateProvider: PermissionStateProvider by lazy {
    AndroidPermissionStateProvider(application)
  }
  val coarseLocationHelper: CoarseLocationHelper by lazy { CoarseLocationHelper(application) }
  val openMeteoWeatherClient: OpenMeteoWeatherClient by lazy { OpenMeteoWeatherClient() }
  val prayerAlarmScheduler: PrayerAlarmScheduler by lazy { PrayerAlarmScheduler(application) }
  val weatherAndPrayerProvider: WeatherAndPrayerProvider by lazy {
    AndroidWeatherAndPrayerProvider(
      settingsRepository = settingsRepository,
      weatherClient = openMeteoWeatherClient,
      locationHelper = coarseLocationHelper,
      prayerAlarmScheduler = prayerAlarmScheduler,
    )
  }
  val appShortcutsProvider: AppShortcutsProvider by lazy { AppShortcutsProvider(application) }
  val mediaSessionMonitor: MediaSessionMonitor by lazy {
    MediaSessionMonitor(
      context = application,
      activityRepository = activityRepository,
      settingsRepository = settingsRepository,
      scope = appScope,
    )
  }
  val batteryMonitor: BatteryMonitor by lazy {
    BatteryMonitor(
      context = application,
      activityRepository = activityRepository,
      settingsRepository = settingsRepository,
      scope = appScope,
    )
  }
  val overlayWindowController: OverlayWindowController by lazy {
    OverlayWindowController(
      context = application,
      settingsRepository = settingsRepository,
      activityRepository = activityRepository,
      notificationReplyManager = notificationReplyManager,
      mediaSessionMonitor = mediaSessionMonitor,
      weatherAndPrayerProvider = weatherAndPrayerProvider,
      appShortcutsProvider = appShortcutsProvider,
      appScope = appScope,
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
