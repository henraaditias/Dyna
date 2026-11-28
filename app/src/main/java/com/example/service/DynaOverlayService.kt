package com.example.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.DynaApplication
import com.example.MainActivity
import com.example.R
import com.example.domain.model.ActivityType
import com.example.domain.model.IdlePlacement
import com.example.domain.model.QuickMenuCategory
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DynaOverlayService : Service() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var autoCollapseJob: Job? = null
  private var autoDimJob: Job? = null
  private var midnightPurgeJob: Job? = null
  private var systemEventsReceiver: BroadcastReceiver? = null

  override fun onCreate() {
    super.onCreate()
    createNotificationChannel()
    val container = (application as DynaApplication).container

    if (!Settings.canDrawOverlays(this)) {
      serviceScope.launch { container.settingsRepository.setOverlayEnabled(false) }
      stopSelf()
      return
    }

    val notification = buildForegroundNotification()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }

    syncDeviceState()
    registerSystemEventsReceiver()
    scheduleMidnightHistoryPurge()

    container.batteryMonitor.start()
    container.weatherAndPrayerProvider.refreshSchedule()
    serviceScope.launch {
      container.notificationHistoryRepository.purgeIfDayChangedOrRebooted()
      container.weatherAndPrayerProvider.refreshWeather(force = false)
    }
    container.overlayWindowController.attachIfPermitted(topOffsetDp = 10f, horizontalOffsetDp = 0f)

    // Observe appearance updates live so sliders/themes/placement immediately reflect at top
    serviceScope.launch {
      combine(
          container.settingsRepository.settings,
          container.activityRepository.isIslandHidden,
        ) { settings, hidden -> settings to hidden }
        .collectLatest { (settings, hidden) ->
          container.permissionStateProvider.refresh()
          if (!Settings.canDrawOverlays(this@DynaOverlayService)) {
            container.overlayWindowController.detach()
            container.settingsRepository.setOverlayEnabled(false)
            stopSelf()
            return@collectLatest
          }
          if (!settings.isOverlayEnabled) {
            container.overlayWindowController.detach()
            stopSelf()
            return@collectLatest
          }
          val effectiveTopOffset =
            when {
              hidden -> 0f
              settings.appearance.idlePlacement == IdlePlacement.FIXED_TOP -> 0f
              else -> settings.appearance.topOffsetDp
            }
          val effectiveHorizontalOffset =
            if (hidden) 0f else settings.appearance.horizontalOffsetDp
          container.overlayWindowController.attachIfPermitted(
            topOffsetDp = effectiveTopOffset,
            horizontalOffsetDp = effectiveHorizontalOffset,
          )
        }
    }

    // Hide overlay on lock screen and in landscape unless showing a temporary notification
    serviceScope.launch {
      combine(
          container.activityRepository.isDeviceLocked,
          container.activityRepository.isLandscape,
          container.activityRepository.currentActivity,
        ) { locked, landscape, activity ->
          !(locked || (landscape && activity.type != ActivityType.NOTIFICATION))
        }
        .collectLatest { shouldBeVisible ->
          container.overlayWindowController.setVisible(shouldBeVisible)
        }
    }

    // Auto-finish notifications directly back to Idle unless user is actively replying
    serviceScope.launch {
      combine(
          container.settingsRepository.settings,
          container.activityRepository.currentActivity,
          container.activityRepository.isReplying,
          container.activityRepository.isOverlayExpanded,
          container.activityRepository.quickMenuCategory,
        ) { settings, activity, isReplying, expanded, quickCategory ->
          TimeoutObservation(
            autoSeconds = settings.appearance.autoCollapseSeconds,
            autoDimEnabled = settings.appearance.autoDimEnabled,
            autoDimDelaySeconds = settings.appearance.autoDimDelaySeconds,
            activityType = activity.type,
            activityId = activity.id,
            eventVersion = activity.eventVersion,
            isReplying = isReplying,
            expanded = expanded,
            quickCategory = quickCategory,
          )
        }
        .collectLatest { obs ->
          autoCollapseJob?.cancel()
          autoDimJob?.cancel()

          if (obs.autoSeconds > 0 && obs.activityType == ActivityType.NOTIFICATION && !obs.isReplying) {
            autoCollapseJob =
              launch {
                delay(obs.autoSeconds * 1000L)
                container.notificationReplyManager.remove(obs.activityId)
                container.activityRepository.dismissActiveNotification()
              }
          } else if (
            obs.autoSeconds > 0 &&
              obs.activityType == ActivityType.IDLE &&
              obs.expanded &&
              obs.quickCategory != QuickMenuCategory.NONE
          ) {
            autoCollapseJob =
              launch {
                delay((obs.autoSeconds.coerceAtLeast(8)) * 1000L)
                container.activityRepository.setOverlayExpanded(false)
              }
          }

          if (
            obs.autoDimEnabled &&
              obs.activityType == ActivityType.IDLE &&
              !obs.expanded &&
              !container.activityRepository.isIslandHidden.value
          ) {
            autoDimJob =
              launch {
                delay(obs.autoDimDelaySeconds.coerceIn(2, 30) * 1000L)
                container.activityRepository.setDimmed(true)
              }
          }
        }
    }
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    val container = (application as DynaApplication).container
    container.activityRepository.setLandscape(
      newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
    )
  }

  private fun syncDeviceState() {
    val container = (application as DynaApplication).container
    val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    container.activityRepository.setDeviceLocked(keyguard?.isKeyguardLocked == true)
    container.activityRepository.setLandscape(
      resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    )
  }

  private fun registerSystemEventsReceiver() {
    val container = (application as DynaApplication).container
    val receiver =
      object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
          when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> container.activityRepository.setDeviceLocked(true)
            Intent.ACTION_USER_PRESENT -> {
              val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
              container.activityRepository.setDeviceLocked(keyguard?.isKeyguardLocked == true)
            }
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
              serviceScope.launch {
                container.notificationHistoryRepository.purgeIfDayChangedOrRebooted()
              }
            }
          }
        }
      }
    val filter =
      IntentFilter().apply {
        addAction(Intent.ACTION_SCREEN_OFF)
        addAction(Intent.ACTION_USER_PRESENT)
        addAction(Intent.ACTION_DATE_CHANGED)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
      }
    ContextCompat.registerReceiver(this, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    systemEventsReceiver = receiver
  }

  private fun scheduleMidnightHistoryPurge() {
    val container = (application as DynaApplication).container
    midnightPurgeJob?.cancel()
    midnightPurgeJob =
      serviceScope.launch {
        while (isActive) {
          val now = LocalDateTime.now()
          val nextMidnight = LocalDate.now().plusDays(1).atStartOfDay().plusSeconds(2)
          val waitMs = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(10_000L)
          delay(waitMs)
          container.notificationHistoryRepository.purgeIfDayChangedOrRebooted()
        }
      }
  }

  private data class TimeoutObservation(
    val autoSeconds: Int,
    val autoDimEnabled: Boolean,
    val autoDimDelaySeconds: Int,
    val activityType: ActivityType,
    val activityId: String,
    val eventVersion: Long,
    val isReplying: Boolean,
    val expanded: Boolean,
    val quickCategory: QuickMenuCategory,
  )

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP_OVERLAY) {
      val container = (application as DynaApplication).container
      container.overlayWindowController.detach()
      serviceScope.launch {
        container.settingsRepository.setOverlayEnabled(false)
        stopSelf()
      }
      return START_NOT_STICKY
    }

    if (!Settings.canDrawOverlays(this)) {
      stopSelf()
      return START_NOT_STICKY
    }

    return START_STICKY
  }

  override fun onDestroy() {
    autoCollapseJob?.cancel()
    autoDimJob?.cancel()
    midnightPurgeJob?.cancel()
    systemEventsReceiver?.let { runCatching { unregisterReceiver(it) } }
    systemEventsReceiver = null
    val container = (application as DynaApplication).container
    container.batteryMonitor.stop()
    container.overlayWindowController.detach()
    serviceScope.cancel()
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  private fun createNotificationChannel() {
    val manager = getSystemService(NotificationManager::class.java)
    val channel =
      NotificationChannel(
          CHANNEL_ID,
          getString(R.string.fgs_channel_name),
          NotificationManager.IMPORTANCE_LOW,
        )
        .apply {
          description = getString(R.string.fgs_channel_desc)
          setShowBadge(false)
        }
    manager.createNotificationChannel(channel)
  }

  private fun buildForegroundNotification(): Notification {
    val openAppIntent =
      PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )
    val stopIntent =
      PendingIntent.getService(
        this,
        1,
        Intent(this, DynaOverlayService::class.java).setAction(ACTION_STOP_OVERLAY),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
      )

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher_round)
      .setContentTitle(getString(R.string.fgs_notif_title))
      .setContentText(getString(R.string.fgs_notif_body))
      .setContentIntent(openAppIntent)
      .setOngoing(true)
      .setSilent(true)
      .addAction(0, getString(R.string.fgs_action_stop), stopIntent)
      .build()
  }

  companion object {
    private const val CHANNEL_ID = "dyna_overlay_channel"
    private const val NOTIFICATION_ID = 1001
    const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"

    fun start(context: Context) {
      if (!Settings.canDrawOverlays(context)) return
      val intent = Intent(context, DynaOverlayService::class.java)
      ContextCompat.startForegroundService(context, intent)
    }

    fun stop(context: Context) {
      context.stopService(Intent(context, DynaOverlayService::class.java))
    }
  }
}
