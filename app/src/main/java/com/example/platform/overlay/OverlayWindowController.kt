package com.example.platform.overlay

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.R
import com.example.domain.model.ActivityType
import com.example.domain.model.DashboardThemeMode
import com.example.domain.model.ExpressionState
import com.example.domain.model.IdleTapAction
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.UserSettings
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.WeatherAndPrayerProvider
import com.example.platform.media.MediaSessionMonitor
import com.example.platform.notifications.NotificationReplyManager
import com.example.platform.shortcuts.AppShortcutsProvider
import com.example.ui.components.DynaIslandPill
import com.example.ui.theme.DynaTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private class OverlayLifecycleOwner :
  LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
  private val lifecycleRegistry = LifecycleRegistry(this)
  private val savedStateController = SavedStateRegistryController.create(this)
  override val viewModelStore = ViewModelStore()

  override val lifecycle: Lifecycle
    get() = lifecycleRegistry

  override val savedStateRegistry: SavedStateRegistry
    get() = savedStateController.savedStateRegistry

  fun onCreate() {
    savedStateController.performRestore(null)
    lifecycleRegistry.currentState = Lifecycle.State.CREATED
  }

  fun onResume() {
    lifecycleRegistry.currentState = Lifecycle.State.STARTED
    lifecycleRegistry.currentState = Lifecycle.State.RESUMED
  }

  fun onDestroy() {
    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    viewModelStore.clear()
  }
}

class OverlayWindowController(
  private val context: Context,
  private val settingsRepository: SettingsRepository,
  private val activityRepository: ActivityRepository,
  private val notificationReplyManager: NotificationReplyManager,
  private val mediaSessionMonitor: MediaSessionMonitor,
  private val weatherAndPrayerProvider: WeatherAndPrayerProvider,
  private val appShortcutsProvider: AppShortcutsProvider,
  private val appScope: CoroutineScope,
) {
  private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
  private var overlayContainer: FrameLayout? = null
  private var lifecycleOwner: OverlayLifecycleOwner? = null
  private var layoutParams: WindowManager.LayoutParams? = null

  private val _isAttached = MutableStateFlow(false)
  val isAttached: StateFlow<Boolean> = _isAttached.asStateFlow()

  private inner class OverlayRootContainer(context: Context) : FrameLayout(context) {
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
      if (
        event.keyCode == KeyEvent.KEYCODE_BACK &&
          event.action == KeyEvent.ACTION_UP &&
          !event.isCanceled &&
          activityRepository.isReplying.value
      ) {
        activityRepository.setReplying(false)
        return true
      }
      return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
      if (ev.action == MotionEvent.ACTION_OUTSIDE) {
        if (activityRepository.isReplying.value) {
          activityRepository.setReplying(false)
        }
        if (
          activityRepository.isOverlayExpanded.value &&
            activityRepository.currentActivity.value.type == ActivityType.IDLE
        ) {
          activityRepository.setOverlayExpanded(false)
        }
        activityRepository.setDimmed(true)
      }
      return super.dispatchTouchEvent(ev)
    }
  }

  fun attachIfPermitted(topOffsetDp: Float, horizontalOffsetDp: Float = 0f): Boolean {
    if (!Settings.canDrawOverlays(context)) {
      detach()
      return false
    }
    if (overlayContainer != null) {
      updatePosition(topOffsetDp = topOffsetDp, horizontalOffsetDp = horizontalOffsetDp)
      return true
    }

    val owner = OverlayLifecycleOwner().apply { onCreate() }
    val params = createLayoutParams(topOffsetDp = topOffsetDp, horizontalOffsetDp = horizontalOffsetDp)

    val composeView =
      ComposeView(context).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setViewTreeLifecycleOwner(owner)
        setViewTreeViewModelStoreOwner(owner)
        setViewTreeSavedStateRegistryOwner(owner)
        setContent {
          val settings by
            settingsRepository.settings.collectAsStateWithLifecycle(initialValue = UserSettings())
          DynaTheme(
            darkTheme = settings.dashboardThemeMode == DashboardThemeMode.DARK,
            language = settings.language,
          ) {
            val activity by activityRepository.currentActivity.collectAsStateWithLifecycle()
            val isExpanded by activityRepository.isOverlayExpanded.collectAsStateWithLifecycle()
            val isReplying by activityRepository.isReplying.collectAsStateWithLifecycle()
            val isDimmed by activityRepository.isDimmed.collectAsStateWithLifecycle()
            val isIslandHidden by activityRepository.isIslandHidden.collectAsStateWithLifecycle()
            val quickCategory by activityRepository.quickMenuCategory.collectAsStateWithLifecycle()
            val transientExpression by
              activityRepository.transientExpression.collectAsStateWithLifecycle()
            val batteryState by activityRepository.batteryState.collectAsStateWithLifecycle()
            val weather by weatherAndPrayerProvider.weather.collectAsStateWithLifecycle()
            val prayerSchedule by
              weatherAndPrayerProvider.prayerSchedule.collectAsStateWithLifecycle()

            val installedApps = remember { appShortcutsProvider.queryInstalledLaunchableApps() }
            val orderedShortcuts =
              remember(settings.pinnedShortcutPackages, settings.shortcutSlots, installedApps) {
                settings.orderedShortcuts().mapNotNull { pkg ->
                  installedApps.firstOrNull { it.packageName == pkg }
                }
              }
            val enabledMenuItems =
              remember(
                settings.quickMenuItems,
                settings.calendarEnabled,
                settings.weatherEnabled,
                settings.shortcutsEnabled,
                settings.prayerAlarmEnabled,
                settings.timerActivityEnabled,
                settings.stopwatchEnabled,
                settings.hideTopDotEnabled,
              ) {
                settings.enabledQuickMenuItems()
              }

            LaunchedEffect(isReplying) { updateFocusability(focusable = isReplying) }

            DynaIslandPill(
              activity = activity,
              appearance = settings.appearance,
              isExpanded = isExpanded,
              showNotificationPreview = settings.showNotificationPreview,
              isReplying = isReplying,
              isDimmed = isDimmed,
              isIslandHidden = isIslandHidden,
              quickMenuCategory = quickCategory,
              quickMenuItems = enabledMenuItems,
              transientExpression = transientExpression,
              batteryState = batteryState,
              weather = weather,
              prayerSchedule = prayerSchedule,
              shortcutSlots = orderedShortcuts,
              installedApps = installedApps,
              appIconProvider = { pkg -> appShortcutsProvider.getAppIconBitmap(pkg) },
              maxWidthDp = 344f,
              onClick = {
                activityRepository.setDimmed(false)
                if (
                  activity.type == ActivityType.IDLE &&
                    !isExpanded &&
                    settings.appearance.idleTapAction == IdleTapAction.PLAY_REACTION
                ) {
                  val next = settings.appearance.selectedExpression.nextIdleCycle()
                  activityRepository.triggerTransientExpression(next)
                } else {
                  activityRepository.toggleOverlayExpanded()
                }
              },
              onDoubleClick = {
                if (activity.type == ActivityType.IDLE) {
                  val next = settings.appearance.selectedExpression.nextIdleCycle()
                  appScope.launch {
                    settingsRepository.updateAppearance { it.copy(selectedExpression = next) }
                  }
                  activityRepository.triggerTransientExpression(next, durationMs = 1500L)
                } else {
                  activityRepository.toggleOverlayExpanded()
                }
              },
              onLongClick = {
                if (activity.type == ActivityType.IDLE) {
                  activityRepository.openQuickMenu(QuickMenuCategory.MOOD_PICKER)
                } else {
                  activityRepository.setOverlayExpanded(true)
                }
              },
              onSwipeUpCollapse = { activityRepository.setOverlayExpanded(false) },
              onHideIsland = {
                if (settings.hideTopDotEnabled) {
                  activityRepository.hideIsland()
                }
              },
              onRestoreIsland = { activityRepository.restoreIsland() },
              onToggleTimerPause = { activityRepository.toggleTimerPause() },
              onStopTimer = { activityRepository.stopTimer() },
              onStartTimer = { seconds -> activityRepository.startTimer(seconds) },
              onStartStopwatch = { activityRepository.startStopwatch() },
              onToggleStopwatchPause = { activityRepository.toggleStopwatchPause() },
              onLapStopwatch = { activityRepository.recordStopwatchLap() },
              onStopStopwatch = { activityRepository.stopStopwatch() },
              onDismissNotification = {
                notificationReplyManager.remove(activity.id)
                activityRepository.dismissActiveNotification()
              },
              onMarkNotificationRead = {
                if (!activity.isTestSimulation) {
                  notificationReplyManager.triggerMarkRead(context, activity.id)
                }
                notificationReplyManager.remove(activity.id)
                activityRepository.dismissActiveNotification()
              },
              onOpenNotificationApp = {
                if (!activity.isTestSimulation) {
                  notificationReplyManager.openContentIntent(context, activity.id)
                }
                activityRepository.dismissActiveNotification()
              },
              onStartReply = { activityRepository.setReplying(true) },
              onCancelReply = { activityRepository.setReplying(false) },
              onSendReply = { replyText ->
                if (activity.type == ActivityType.NOTIFICATION) {
                  if (!activity.isTestSimulation) {
                    val sent =
                      notificationReplyManager.sendReply(
                        context = context,
                        key = activity.id,
                        replyText = replyText,
                        titleSnapshot = activity.title,
                        textSnapshot = activity.subtitle,
                      )
                    if (!sent) {
                      Toast.makeText(
                          context,
                          context.getString(R.string.pill_reply_failed),
                          Toast.LENGTH_SHORT,
                        )
                        .show()
                    }
                  }
                  activityRepository.dismissActiveNotification()
                }
              },
              onMediaPlayPause = {
                if (activity.isTestSimulation) {
                  activityRepository.toggleSimulatedMediaPlayPause()
                } else {
                  mediaSessionMonitor.togglePlayPause()
                }
              },
              onMediaNext = {
                if (!activity.isTestSimulation) mediaSessionMonitor.skipToNext()
              },
              onMediaPrevious = {
                if (!activity.isTestSimulation) mediaSessionMonitor.skipToPrevious()
              },
              onMediaFavorite = {
                if (activity.isTestSimulation) {
                  activityRepository.toggleSimulatedMediaFavorite()
                } else {
                  mediaSessionMonitor.toggleFavorite()
                }
              },
              onCallAnswer = {
                if (!activity.isTestSimulation) {
                  notificationReplyManager.triggerCallAnswer(context, activity.id)
                }
              },
              onCallToggleMute = {
                val nowMuted = activityRepository.toggleCallMute()
                if (!activity.isTestSimulation) {
                  notificationReplyManager.setSystemMicrophoneMute(context, nowMuted)
                }
              },
              onCallToggleVideo = { activityRepository.toggleCallVideo() },
              onCallDecline = {
                if (!activity.isTestSimulation) {
                  notificationReplyManager.triggerCallDecline(context, activity.id)
                }
                activityRepository.dismissCall()
              },
              onDismissCall = { activityRepository.dismissCall() },
              onDismissCharging = { activityRepository.dismissChargingBanner() },
              onDismissLowBattery = { activityRepository.dismissLowBatteryAlert() },
              onDismissPrayerAlarm = { activityRepository.dismissPrayerAlarm() },
              onSelectQuickCategory = { cat ->
                activityRepository.setQuickMenuCategory(cat)
                if (cat == QuickMenuCategory.WEATHER) {
                  appScope.launch { weatherAndPrayerProvider.refreshWeather(force = false) }
                } else if (cat == QuickMenuCategory.PRAYER) {
                  weatherAndPrayerProvider.refreshSchedule()
                }
              },
              onOpenCalendarDate = { date ->
                activityRepository.setOverlayExpanded(false)
                appShortcutsProvider.openGoogleCalendarForDate(date)
              },
              onRefreshWeather = {
                appScope.launch { weatherAndPrayerProvider.refreshWeather(force = true) }
              },
              onLaunchShortcut = { pkg ->
                activityRepository.setOverlayExpanded(false)
                appShortcutsProvider.launchApp(pkg)
              },
              onAssignShortcutSlot = { _, pkg ->
                if (pkg != null) {
                  appScope.launch { settingsRepository.addShortcut(pkg) }
                }
              },
              onOpenShortcutsSettings = {
                activityRepository.setOverlayExpanded(false)
                runCatching {
                  context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                      putExtra(MainActivity.EXTRA_START_ROUTE, "beranda")
                    }
                  )
                }
              },
              onSelectExpression = { mood: ExpressionState ->
                appScope.launch {
                  settingsRepository.updateAppearance { it.copy(selectedExpression = mood) }
                }
                activityRepository.triggerTransientExpression(mood, durationMs = 1500L)
              },
              onSelectIdleMode = { mode ->
                appScope.launch {
                  settingsRepository.updateAppearance { it.copy(idleDisplayMode = mode) }
                }
              },
              onSelectIdlePlacement = { placement ->
                appScope.launch {
                  settingsRepository.updateAppearance { it.copy(idlePlacement = placement) }
                }
              },
              onSwapSecondaryActivity = { targetType ->
                activityRepository.swapToSecondaryActivity(targetType)
              },
            )
          }
        }
      }

    val container =
      OverlayRootContainer(context).apply {
        setViewTreeLifecycleOwner(owner)
        setViewTreeViewModelStoreOwner(owner)
        setViewTreeSavedStateRegistryOwner(owner)
        addView(
          composeView,
          FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
          ),
        )
      }

    val added =
      runCatching {
          windowManager.addView(container, params)
          owner.onResume()
          overlayContainer = container
          lifecycleOwner = owner
          layoutParams = params
          _isAttached.value = true
          true
        }
        .getOrElse {
          owner.onDestroy()
          _isAttached.value = false
          false
        }

    return added
  }

  fun setVisible(visible: Boolean) {
    val container = overlayContainer ?: return
    val nextVisibility = if (visible) View.VISIBLE else View.GONE
    if (container.visibility != nextVisibility) {
      container.visibility = nextVisibility
    }
  }

  fun updatePosition(topOffsetDp: Float, horizontalOffsetDp: Float = 0f) {
    val view = overlayContainer ?: return
    val params = layoutParams ?: return
    val nextY = dpToPx(topOffsetDp)
    val nextX = dpToPxSigned(horizontalOffsetDp)
    if (params.y != nextY || params.x != nextX) {
      params.y = nextY
      params.x = nextX
      runCatching { windowManager.updateViewLayout(view, params) }
    }
  }

  private fun updateFocusability(focusable: Boolean) {
    val view = overlayContainer ?: return
    val params = layoutParams ?: return
    val baseFlags =
      WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
    val nextFlags =
      if (focusable) {
        baseFlags
      } else {
        baseFlags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
      }
    val nextSoftInput =
      if (focusable) {
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or
          WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
      } else {
        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
      }

    if (params.flags != nextFlags || params.softInputMode != nextSoftInput) {
      params.flags = nextFlags
      params.softInputMode = nextSoftInput
      runCatching { windowManager.updateViewLayout(view, params) }
    }
  }

  fun detach() {
    val view = overlayContainer
    val owner = lifecycleOwner
    overlayContainer = null
    lifecycleOwner = null
    layoutParams = null
    _isAttached.value = false
    activityRepository.setReplying(false)

    if (view != null) {
      runCatching { windowManager.removeViewImmediate(view) }
    }
    owner?.onDestroy()
  }

  private fun createLayoutParams(
    topOffsetDp: Float,
    horizontalOffsetDp: Float,
  ): WindowManager.LayoutParams {
    val flags =
      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

    return WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        flags,
        PixelFormat.TRANSLUCENT,
      )
      .apply {
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        x = dpToPxSigned(horizontalOffsetDp)
        y = dpToPx(topOffsetDp)
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
          layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
      }
  }

  private fun dpToPx(dp: Float): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).roundToInt().coerceAtLeast(0)
  }

  private fun dpToPxSigned(dp: Float): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).roundToInt()
  }
}
