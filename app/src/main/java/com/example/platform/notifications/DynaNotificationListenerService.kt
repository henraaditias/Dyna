package com.example.platform.notifications

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.DynaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DynaNotificationListenerService : NotificationListenerService() {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  override fun onListenerConnected() {
    super.onListenerConnected()
    (application as? DynaApplication)?.container?.permissionStateProvider?.refresh()
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    (application as? DynaApplication)?.container?.permissionStateProvider?.refresh()
  }

  override fun onNotificationPosted(sbn: StatusBarNotification?) {
    if (sbn == null) return
    if (sbn.packageName == packageName) return

    val notification = sbn.notification ?: return
    if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return

    val extras = notification.extras ?: return
    val rawTitle = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim()
    val rawText =
      (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
          ?: extras.getCharSequence(Notification.EXTRA_TEXT))
        ?.toString()
        ?.trim()

    if (rawTitle.isNullOrEmpty() && rawText.isNullOrEmpty()) return

    val appName =
      runCatching {
          val info = packageManager.getApplicationInfo(sbn.packageName, 0)
          packageManager.getApplicationLabel(info).toString()
        }
        .getOrDefault(sbn.packageName)

    val container = (application as? DynaApplication)?.container ?: return
    scope.launch {
      val settings = container.settingsRepository.settings.first()
      if (!settings.notificationActivityEnabled || !settings.isOverlayEnabled) return@launch

      container.activityRepository.onNotificationPosted(
        key = sbn.key,
        appName = appName,
        title = rawTitle ?: appName,
        text = if (settings.showNotificationPreview) rawText else null,
        autoExpand = settings.autoExpandOnNotification,
      )
    }
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    if (sbn == null) return
    (application as? DynaApplication)?.container?.activityRepository?.onNotificationRemoved(sbn.key)
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }
}
