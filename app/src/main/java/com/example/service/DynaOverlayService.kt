package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.DynaApplication
import com.example.MainActivity
import com.example.R
import com.example.domain.model.ActivityType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DynaOverlayService : Service() {

  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private var autoCollapseJob: Job? = null

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

    container.overlayWindowController.attachIfPermitted(topOffsetDp = 8f)

    // Observe appearance updates live so sliders/themes immediately reflect at top of screen
    serviceScope.launch {
      container.settingsRepository.settings.collectLatest { settings ->
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
        container.overlayWindowController.attachIfPermitted(settings.appearance.topOffsetDp)
      }
    }

    // Auto-collapse notifications or non-timer expanded panels based on user setting
    serviceScope.launch {
      combine(
          container.settingsRepository.settings,
          container.activityRepository.currentActivity,
          container.activityRepository.isOverlayExpanded,
        ) { settings, activity, expanded ->
          Triple(settings.appearance.autoCollapseSeconds, activity, expanded)
        }
        .collectLatest { (collapseSeconds, activity, expanded) ->
          autoCollapseJob?.cancel()
          if (expanded && collapseSeconds > 0 && activity.type == ActivityType.NOTIFICATION) {
            autoCollapseJob =
              launch {
                delay(collapseSeconds * 1000L)
                container.activityRepository.setOverlayExpanded(false)
              }
          }
        }
    }
  }

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
    (application as DynaApplication).container.overlayWindowController.detach()
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
