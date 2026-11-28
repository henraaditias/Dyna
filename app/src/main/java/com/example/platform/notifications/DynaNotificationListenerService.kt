package com.example.platform.notifications

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.DynaApplication
import com.example.domain.model.CallSnapshot
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
    val container = (application as? DynaApplication)?.container ?: return
    container.permissionStateProvider.refresh()
    scope.launch { container.notificationHistoryRepository.purgeIfDayChangedOrRebooted() }
    container.mediaSessionMonitor.start(
      ComponentName(this, DynaNotificationListenerService::class.java)
    )
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    val container = (application as? DynaApplication)?.container ?: return
    container.mediaSessionMonitor.stop()
    container.permissionStateProvider.refresh()
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

    val title = rawTitle ?: appName
    val container = (application as? DynaApplication)?.container ?: return

    val isCallNotification =
      notification.category == Notification.CATEGORY_CALL ||
        (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
          extras.containsKey(Notification.EXTRA_CALL_PERSON))

    if (isCallNotification) {
      val (hasAnswer, hasDecline) =
        container.notificationReplyManager.registerCallActions(sbn.key, notification)
      val isVideo =
        title.contains("video", ignoreCase = true) ||
          rawText.orEmpty().contains("video", ignoreCase = true)
      scope.launch {
        val settings = container.settingsRepository.settings.first()
        if (!settings.callActivityEnabled || !settings.isOverlayEnabled) return@launch
        container.activityRepository.onCallPosted(
          CallSnapshot(
            key = sbn.key,
            appName = appName,
            callerName = title,
            statusLabel = rawText ?: "Panggilan aktif",
            isVideoCall = isVideo,
            hasAnswerAction = hasAnswer,
            hasDeclineAction = hasDecline,
          ),
          autoExpand = true,
        )
      }
      return
    }

    if (
      container.notificationReplyManager.shouldIgnoreAsReplyEcho(
        key = sbn.key,
        notification = notification,
        title = title,
        text = rawText,
      )
    ) {
      return
    }

    val replyCapability =
      container.notificationReplyManager.extractAndRegister(
        key = sbn.key,
        notification = notification,
      )
    val hasMarkReadAction =
      container.notificationReplyManager.registerMarkReadAction(
        key = sbn.key,
        notification = notification,
      )
    val hasContentIntent =
      container.notificationReplyManager.registerContentIntent(sbn.key, notification)

    scope.launch {
      val settings = container.settingsRepository.settings.first()
      if (!settings.notificationActivityEnabled) return@launch

      val bodyForHistory = if (settings.showNotificationPreview) rawText.orEmpty() else ""
      if ((notification.flags and Notification.FLAG_ONGOING_EVENT) == 0) {
        container.notificationHistoryRepository.recordNotification(
          packageName = sbn.packageName,
          appName = appName,
          title = title,
          body = bodyForHistory,
          isChat = replyCapability != null,
        )
      }

      if (!settings.isOverlayEnabled) return@launch

      container.activityRepository.onNotificationPosted(
        key = sbn.key,
        appName = appName,
        title = title,
        text = if (settings.showNotificationPreview) rawText else null,
        autoExpand = settings.autoExpandOnNotification,
        replyCapability = replyCapability,
        hasMarkReadAction = hasMarkReadAction,
        hasContentIntent = hasContentIntent,
        sourcePackageName = sbn.packageName,
      )
    }
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification?) {
    if (sbn == null) return
    val container = (application as? DynaApplication)?.container ?: return
    container.notificationReplyManager.remove(sbn.key)
    container.activityRepository.onCallRemoved(sbn.key)
    container.activityRepository.onNotificationRemoved(sbn.key)
  }

  override fun onDestroy() {
    (application as? DynaApplication)?.container?.mediaSessionMonitor?.stop()
    scope.cancel()
    super.onDestroy()
  }
}
