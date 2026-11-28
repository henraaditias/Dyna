package com.example.data.activities

import com.example.domain.model.ActivityAction
import com.example.domain.model.ActivityActionType
import com.example.domain.model.ActivityState
import com.example.domain.model.ActivityType
import com.example.domain.model.BatterySnapshot
import com.example.domain.model.CallSnapshot
import com.example.domain.model.ExpressionState
import com.example.domain.model.MediaSnapshot
import com.example.domain.model.PrayerName
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.ReplyCapability
import com.example.domain.model.SecondaryActivityBadge
import com.example.domain.repository.ActivityRepository
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveActivityRepository : ActivityRepository {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private val _isOverlayExpanded = MutableStateFlow(false)
  override val isOverlayExpanded: StateFlow<Boolean> = _isOverlayExpanded.asStateFlow()

  private val _isReplying = MutableStateFlow(false)
  override val isReplying: StateFlow<Boolean> = _isReplying.asStateFlow()

  private val _quickMenuCategory = MutableStateFlow(QuickMenuCategory.NONE)
  override val quickMenuCategory: StateFlow<QuickMenuCategory> = _quickMenuCategory.asStateFlow()

  private val _isDimmed = MutableStateFlow(false)
  override val isDimmed: StateFlow<Boolean> = _isDimmed.asStateFlow()

  private val _isIslandHidden = MutableStateFlow(false)
  override val isIslandHidden: StateFlow<Boolean> = _isIslandHidden.asStateFlow()

  private val _isDeviceLocked = MutableStateFlow(false)
  override val isDeviceLocked: StateFlow<Boolean> = _isDeviceLocked.asStateFlow()

  private val _isLandscape = MutableStateFlow(false)
  override val isLandscape: StateFlow<Boolean> = _isLandscape.asStateFlow()

  private val _transientExpression = MutableStateFlow<ExpressionState?>(null)
  override val transientExpression: StateFlow<ExpressionState?> =
    _transientExpression.asStateFlow()

  private val _batteryState = MutableStateFlow(BatterySnapshot())
  override val batteryState: StateFlow<BatterySnapshot> = _batteryState.asStateFlow()

  private val _currentActivity = MutableStateFlow(ActivityState.Idle)
  override val currentActivity: StateFlow<ActivityState> = _currentActivity.asStateFlow()

  private var callSlot: CallSnapshot? = null
  private var prayerAlarmSlot: PrayerAlarmSnapshot? = null
  private var timerSnapshot: TimerSnapshot? = null
  private var stopwatchSnapshot: StopwatchSnapshot? = null
  private var notificationSnapshot: NotificationSnapshot? = null
  private var mediaSnapshot: MediaSnapshot? = null
  private var chargingBannerActive: Boolean = false
  private var lowBatteryAlertActive: Boolean = false
  private var lowBatteryAlertAcknowledgedLevel: Int? = null
  private var prioritizedOverrideType: ActivityType? = null
  private var expandedBeforeInterrupt: Boolean = false
  private var hasInterruptedBackgroundActivity: Boolean = false

  private var timerTickerJob: Job? = null
  private var stopwatchTickerJob: Job? = null
  private var chargingBannerJob: Job? = null
  private var expressionJob: Job? = null
  private var testAutoCollapseJob: Job? = null
  private var notificationCounter = 0L

  private data class TimerSnapshot(
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isPaused: Boolean,
    val isFinished: Boolean,
  )

  private data class StopwatchSnapshot(
    val elapsedSeconds: Int,
    val isPaused: Boolean,
    val laps: List<String>,
  )

  private data class NotificationSnapshot(
    val key: String,
    val appName: String,
    val title: String,
    val text: String?,
    val replyCapability: ReplyCapability?,
    val hasMarkReadAction: Boolean,
    val hasContentIntent: Boolean,
    val sourcePackageName: String?,
    val isTestSimulation: Boolean,
    val eventVersion: Long,
  )

  private data class PrayerAlarmSnapshot(
    val prayerName: PrayerName,
    val formattedTime: String,
    val cityName: String,
  )

  override fun toggleOverlayExpanded() {
    val next = !_isOverlayExpanded.value
    _isOverlayExpanded.value = next
    _isDimmed.value = false
    if (!next) {
      _isReplying.value = false
      _quickMenuCategory.value = QuickMenuCategory.NONE
    } else if (_currentActivity.value.type == ActivityType.IDLE) {
      if (_quickMenuCategory.value == QuickMenuCategory.NONE) {
        _quickMenuCategory.value = QuickMenuCategory.MENU
      }
      triggerTransientExpression(ExpressionState.CHEERFUL, durationMs = 1_100L)
    }
  }

  override fun setOverlayExpanded(expanded: Boolean) {
    _isOverlayExpanded.value = expanded
    if (expanded) {
      _isDimmed.value = false
      _isIslandHidden.value = false
    } else {
      _isReplying.value = false
      _quickMenuCategory.value = QuickMenuCategory.NONE
    }
  }

  override fun setReplying(replying: Boolean) {
    if (!replying) {
      _isReplying.value = false
      return
    }
    val active = _currentActivity.value
    if (active.type == ActivityType.NOTIFICATION && active.replyCapability != null) {
      _isDimmed.value = false
      _isIslandHidden.value = false
      _isOverlayExpanded.value = true
      _isReplying.value = true
    }
  }

  override fun setQuickMenuCategory(category: QuickMenuCategory) {
    _isDimmed.value = false
    _quickMenuCategory.value = category
    if (category == QuickMenuCategory.NONE) {
      _isOverlayExpanded.value = false
    } else {
      _isIslandHidden.value = false
      _isOverlayExpanded.value = true
      val reaction =
        when (category) {
          QuickMenuCategory.MENU -> ExpressionState.CHEERFUL
          QuickMenuCategory.CALENDAR,
          QuickMenuCategory.WEATHER,
          QuickMenuCategory.PRAYER,
          QuickMenuCategory.SHORTCUTS -> ExpressionState.SMIRK
          else -> ExpressionState.HAPPY
        }
      triggerTransientExpression(reaction, durationMs = 1_000L)
    }
  }

  override fun openQuickMenu(category: QuickMenuCategory) {
    _isDimmed.value = false
    _isIslandHidden.value = false
    _isReplying.value = false
    _quickMenuCategory.value = category
    _isOverlayExpanded.value = true
    triggerTransientExpression(ExpressionState.CHEERFUL, durationMs = 1_100L)
  }

  override fun setDimmed(dimmed: Boolean) {
    if (_isOverlayExpanded.value && dimmed) return
    _isDimmed.value = dimmed
  }

  override fun hideIsland() {
    _isIslandHidden.value = true
    _isOverlayExpanded.value = false
    _isReplying.value = false
    _quickMenuCategory.value = QuickMenuCategory.NONE
    _isDimmed.value = false
  }

  override fun restoreIsland() {
    _isIslandHidden.value = false
    _isDimmed.value = false
    triggerTransientExpression(ExpressionState.WINK, durationMs = 1_200L)
  }

  override fun setDeviceLocked(locked: Boolean) {
    _isDeviceLocked.value = locked
    if (locked) {
      _isOverlayExpanded.value = false
      _isReplying.value = false
      _quickMenuCategory.value = QuickMenuCategory.NONE
    }
  }

  override fun setLandscape(landscape: Boolean) {
    _isLandscape.value = landscape
    if (landscape && _currentActivity.value.type == ActivityType.IDLE) {
      _isOverlayExpanded.value = false
      _quickMenuCategory.value = QuickMenuCategory.NONE
    }
  }

  override fun triggerTransientExpression(expression: ExpressionState, durationMs: Long) {
    expressionJob?.cancel()
    _isDimmed.value = false
    _transientExpression.value = expression
    expressionJob =
      scope.launch {
        delay(durationMs)
        _transientExpression.value = null
      }
  }

  override fun swapToSecondaryActivity(type: ActivityType) {
    prioritizedOverrideType = type
    _isDimmed.value = false
    _isIslandHidden.value = false
    _isOverlayExpanded.value = true
    publishState()
  }

  override fun startTimer(durationSeconds: Int) {
    timerTickerJob?.cancel()
    _isReplying.value = false
    _isDimmed.value = false
    _quickMenuCategory.value = QuickMenuCategory.NONE
    prioritizedOverrideType = null
    val safeDuration = durationSeconds.coerceAtLeast(1)
    timerSnapshot =
      TimerSnapshot(
        totalSeconds = safeDuration,
        remainingSeconds = safeDuration,
        isPaused = false,
        isFinished = false,
      )
    _isOverlayExpanded.value = true
    publishState()
    launchTimerTicker()
  }

  override fun toggleTimerPause() {
    val current = timerSnapshot ?: return
    if (current.isFinished) {
      startTimer(current.totalSeconds)
      return
    }
    val nextPaused = !current.isPaused
    timerSnapshot = current.copy(isPaused = nextPaused)
    publishState()
    if (nextPaused) {
      timerTickerJob?.cancel()
    } else {
      launchTimerTicker()
    }
  }

  override fun stopTimer() {
    timerTickerJob?.cancel()
    timerSnapshot = null
    if (prioritizedOverrideType == ActivityType.TIMER) {
      prioritizedOverrideType = null
    }
    collapseAfterFinishingActivity()
    publishState()
  }

  override fun startStopwatch() {
    stopwatchTickerJob?.cancel()
    _isReplying.value = false
    _isDimmed.value = false
    _quickMenuCategory.value = QuickMenuCategory.NONE
    prioritizedOverrideType = null
    stopwatchSnapshot = StopwatchSnapshot(elapsedSeconds = 0, isPaused = false, laps = emptyList())
    _isOverlayExpanded.value = true
    publishState()
    launchStopwatchTicker()
  }

  override fun toggleStopwatchPause() {
    val current = stopwatchSnapshot ?: return
    val nextPaused = !current.isPaused
    stopwatchSnapshot = current.copy(isPaused = nextPaused)
    publishState()
    if (nextPaused) {
      stopwatchTickerJob?.cancel()
    } else {
      launchStopwatchTicker()
    }
  }

  override fun recordStopwatchLap() {
    val current = stopwatchSnapshot ?: return
    val formatted = formatTime(current.elapsedSeconds)
    stopwatchSnapshot = current.copy(laps = (listOf(formatted) + current.laps).take(4))
    publishState()
  }

  override fun stopStopwatch() {
    stopwatchTickerJob?.cancel()
    stopwatchSnapshot = null
    if (prioritizedOverrideType == ActivityType.STOPWATCH) {
      prioritizedOverrideType = null
    }
    collapseAfterFinishingActivity()
    publishState()
  }

  override fun onNotificationPosted(
    key: String,
    appName: String,
    title: String,
    text: String?,
    autoExpand: Boolean,
    replyCapability: ReplyCapability?,
    hasMarkReadAction: Boolean,
    hasContentIntent: Boolean,
    sourcePackageName: String?,
  ) {
    postNotificationInternal(
      key = key,
      appName = appName,
      title = title,
      text = text,
      autoExpand = autoExpand,
      replyCapability = replyCapability,
      hasMarkReadAction = hasMarkReadAction,
      hasContentIntent = hasContentIntent,
      sourcePackageName = sourcePackageName,
      isTestSimulation = false,
    )
  }

  private fun postNotificationInternal(
    key: String,
    appName: String,
    title: String,
    text: String?,
    autoExpand: Boolean,
    replyCapability: ReplyCapability?,
    hasMarkReadAction: Boolean,
    hasContentIntent: Boolean,
    sourcePackageName: String?,
    isTestSimulation: Boolean,
  ) {
    testAutoCollapseJob?.cancel()
    val sameThreadAndCanReply =
      notificationSnapshot?.key == key && _isReplying.value && replyCapability != null
    if (!sameThreadAndCanReply) {
      _isReplying.value = false
    }

    val hadBackground =
      callSlot != null || mediaSnapshot != null || timerSnapshot != null || stopwatchSnapshot != null
    if (notificationSnapshot == null && hadBackground) {
      expandedBeforeInterrupt = _isOverlayExpanded.value
      hasInterruptedBackgroundActivity = true
    } else if (!hadBackground) {
      expandedBeforeInterrupt = false
      hasInterruptedBackgroundActivity = false
    }

    _isDimmed.value = false
    _quickMenuCategory.value = QuickMenuCategory.NONE
    notificationCounter += 1L
    notificationSnapshot =
      NotificationSnapshot(
        key = key,
        appName = appName,
        title = title,
        text = text,
        replyCapability = replyCapability,
        hasMarkReadAction = hasMarkReadAction,
        hasContentIntent = hasContentIntent,
        sourcePackageName = sourcePackageName,
        isTestSimulation = isTestSimulation,
        eventVersion = notificationCounter,
      )
    prioritizedOverrideType = null
    val alertReaction =
      if (replyCapability != null) ExpressionState.WAVE_ALERT else ExpressionState.SURPRISED
    triggerTransientExpression(alertReaction, durationMs = 1_800L)

    if (
      autoExpand &&
        !_isIslandHidden.value &&
        !_isDeviceLocked.value &&
        prayerAlarmSlot == null
    ) {
      _isOverlayExpanded.value = true
    }
    publishState()
  }

  override fun onNotificationRemoved(key: String) {
    if (notificationSnapshot?.key == key) {
      finishNotificationInternal(reaction = ExpressionState.HAPPY)
    }
  }

  override fun dismissActiveNotification() {
    testAutoCollapseJob?.cancel()
    finishNotificationInternal(reaction = ExpressionState.WINK)
  }

  private fun finishNotificationInternal(reaction: ExpressionState) {
    notificationSnapshot = null
    _isReplying.value = false
    if (prioritizedOverrideType == ActivityType.NOTIFICATION) {
      prioritizedOverrideType = null
    }
    if (hasInterruptedBackgroundActivity) {
      val restoreExpanded = expandedBeforeInterrupt
      hasInterruptedBackgroundActivity = false
      expandedBeforeInterrupt = false
      _isOverlayExpanded.value = restoreExpanded
    } else {
      collapseAfterFinishingActivity()
    }
    publishState()
    if (_currentActivity.value.type == ActivityType.IDLE) {
      triggerTransientExpression(reaction, durationMs = 1_000L)
    }
  }

  override fun onMediaUpdated(media: MediaSnapshot?) {
    mediaSnapshot = media
    if (media == null) {
      if (prioritizedOverrideType == ActivityType.MEDIA) {
        prioritizedOverrideType = null
      }
      collapseAfterFinishingActivity()
    }
    publishState()
  }

  override fun onCallPosted(call: CallSnapshot, autoExpand: Boolean) {
    callSlot = call
    _isDimmed.value = false
    _isReplying.value = false
    _quickMenuCategory.value = QuickMenuCategory.NONE
    prioritizedOverrideType = null
    if (autoExpand && !_isDeviceLocked.value) {
      _isOverlayExpanded.value = true
    }
    publishState()
  }

  override fun onCallRemoved(key: String) {
    if (callSlot?.key == key) {
      callSlot = null
      collapseAfterFinishingActivity()
      publishState()
    }
  }

  override fun toggleCallMute(muted: Boolean?): Boolean {
    val current = callSlot ?: return false
    val nextMuted = muted ?: !current.isMuted
    callSlot = current.copy(isMuted = nextMuted)
    publishState()
    return nextMuted
  }

  override fun toggleCallVideo(): Boolean {
    val current = callSlot ?: return false
    val nextVideo = !current.isVideoActive
    callSlot =
      current.copy(
        isVideoActive = nextVideo,
        isVideoCall = current.isVideoCall || nextVideo,
      )
    publishState()
    return nextVideo
  }

  override fun dismissCall() {
    callSlot = null
    if (prioritizedOverrideType == ActivityType.CALL) {
      prioritizedOverrideType = null
    }
    collapseAfterFinishingActivity()
    publishState()
  }

  override fun onBatteryUpdated(snapshot: BatterySnapshot, showChargingBanner: Boolean) {
    val previous = _batteryState.value
    _batteryState.value = snapshot

    if (snapshot.isCharging) {
      lowBatteryAlertActive = false
      lowBatteryAlertAcknowledgedLevel = null
    } else if (
      snapshot.isLow && lowBatteryAlertAcknowledgedLevel != snapshot.levelPercent
    ) {
      lowBatteryAlertActive = true
      _isDimmed.value = false
    }

    if (showChargingBanner && snapshot.isCharging && !previous.isCharging) {
      chargingBannerActive = true
      _isDimmed.value = false
      chargingBannerJob?.cancel()
      chargingBannerJob =
        scope.launch {
          delay(4_000L)
          chargingBannerActive = false
          collapseAfterFinishingActivity()
          publishState()
        }
    } else if (!snapshot.isCharging) {
      chargingBannerJob?.cancel()
      chargingBannerActive = false
      collapseAfterFinishingActivity()
    }

    publishState()
  }

  override fun dismissChargingBanner() {
    chargingBannerJob?.cancel()
    chargingBannerActive = false
    collapseAfterFinishingActivity()
    publishState()
  }

  override fun dismissLowBatteryAlert() {
    lowBatteryAlertActive = false
    lowBatteryAlertAcknowledgedLevel = _batteryState.value.levelPercent
    collapseAfterFinishingActivity()
    publishState()
  }

  override fun triggerPrayerAlarm(prayerName: PrayerName, formattedTime: String, cityName: String) {
    prayerAlarmSlot = PrayerAlarmSnapshot(prayerName, formattedTime, cityName)
    _isDimmed.value = false
    _quickMenuCategory.value = QuickMenuCategory.NONE
    _isOverlayExpanded.value = true
    publishState()
  }

  override fun dismissPrayerAlarm() {
    prayerAlarmSlot = null
    collapseAfterFinishingActivity()
    publishState()
  }

  override fun triggerTestNotification(autoCollapseSeconds: Int) {
    postNotificationInternal(
      key = "test_standard_notif",
      appName = "Dyna",
      title = "Pratinjau Notifikasi",
      text = "Tampilan notifikasi standar menyesuaikan lebar, tinggi, dan opasitas dari menu Gaya.",
      autoExpand = true,
      replyCapability = null,
      hasMarkReadAction = true,
      hasContentIntent = false,
      sourcePackageName = "com.example",
      isTestSimulation = true,
    )
    scheduleTestAutoCollapse(autoCollapseSeconds)
  }

  override fun triggerTestChatNotification(autoCollapseSeconds: Int) {
    postNotificationInternal(
      key = "test_chat_notif",
      appName = "Pesan",
      title = "Raka Pratama",
      text = "Halo, apakah kapsul balas cepat sudah berjalan mulus?",
      autoExpand = true,
      replyCapability =
        ReplyCapability(
          actionLabel = "Balas Cepat",
          placeholder = "Ketik balasan...",
          quickChoices = listOf("Sudah", "Sebentar ya"),
        ),
      hasMarkReadAction = true,
      hasContentIntent = false,
      sourcePackageName = "com.example",
      isTestSimulation = true,
    )
    scheduleTestAutoCollapse(autoCollapseSeconds.coerceAtLeast(6))
  }

  override fun triggerTestCallAndMedia() {
    testAutoCollapseJob?.cancel()
    notificationSnapshot = null
    _isReplying.value = false
    _quickMenuCategory.value = QuickMenuCategory.NONE
    _isDimmed.value = false
    _isIslandHidden.value = false
    prioritizedOverrideType = null

    mediaSnapshot =
      MediaSnapshot(
        packageName = "com.example.music",
        appName = "Musik",
        title = "Midnight Study",
        artist = "Lo-Fi Ensemble",
        isPlaying = true,
        durationMs = 210_000L,
        positionMs = 84_000L,
        hasPrevious = true,
        hasNext = true,
        hasFavoriteAction = true,
        isFavorited = false,
      )
    callSlot =
      CallSnapshot(
        key = "test_call_1",
        appName = "Telepon",
        callerName = "Nadia Putri",
        statusLabel = "01:24",
        isVideoCall = false,
        isMuted = false,
        isVideoActive = false,
        hasAnswerAction = false,
        hasDeclineAction = true,
      )
    _isOverlayExpanded.value = true
    publishState()
  }

  override fun toggleSimulatedMediaPlayPause() {
    val current = mediaSnapshot ?: return
    mediaSnapshot = current.copy(isPlaying = !current.isPlaying)
    publishState()
  }

  override fun toggleSimulatedMediaFavorite() {
    val current = mediaSnapshot ?: return
    mediaSnapshot = current.copy(isFavorited = !current.isFavorited)
    publishState()
  }

  private fun scheduleTestAutoCollapse(seconds: Int) {
    if (seconds <= 0) return
    testAutoCollapseJob?.cancel()
    testAutoCollapseJob =
      scope.launch {
        delay(seconds * 1000L)
        if (notificationSnapshot?.isTestSimulation == true && !_isReplying.value) {
          dismissActiveNotification()
        }
      }
  }

  private fun collapseAfterFinishingActivity() {
    val hasActiveNonIdle =
      callSlot != null ||
        prayerAlarmSlot != null ||
        lowBatteryAlertActive ||
        timerSnapshot != null ||
        stopwatchSnapshot != null ||
        notificationSnapshot != null ||
        chargingBannerActive ||
        mediaSnapshot != null
    if (!hasActiveNonIdle && _quickMenuCategory.value == QuickMenuCategory.NONE) {
      _isOverlayExpanded.value = false
    }
  }

  private fun publishState() {
    val candidates = buildActiveCandidates()
    val primary =
      prioritizedOverrideType?.let { wanted ->
        candidates.firstOrNull { it.type == wanted }
      } ?: candidates.firstOrNull()

    if (primary == null) {
      prioritizedOverrideType = null
      _currentActivity.value = ActivityState.Idle
      return
    }

    val secondary = candidates.firstOrNull { it.type != primary.type }
    val badge =
      secondary?.let {
        SecondaryActivityBadge(
          type = it.type,
          label =
            when (it.type) {
              ActivityType.MEDIA -> "Musik"
              ActivityType.TIMER -> it.title
              ActivityType.STOPWATCH -> it.title
              ActivityType.NOTIFICATION -> it.sourceAppName ?: "Notif"
              ActivityType.CHARGING -> "${it.batteryLevel ?: _batteryState.value.levelPercent}%"
              ActivityType.CALL -> "Panggilan"
              ActivityType.PRAYER_ALARM -> "Shalat"
              ActivityType.LOW_BATTERY -> "Baterai"
              ActivityType.IDLE -> ""
            },
        )
      }

    _currentActivity.value = primary.copy(secondaryBadge = badge)
  }

  private fun buildActiveCandidates(): List<ActivityState> {
    val list = mutableListOf<ActivityState>()

    // Incoming notification temporarily interrupts ongoing media or call (Priority 6)
    notificationSnapshot?.let { notification ->
      list.add(
        ActivityState(
          id = notification.key,
          type = ActivityType.NOTIFICATION,
          title = notification.title,
          subtitle = notification.text,
          isOngoing = false,
          priority = 6,
          sourceAppName = notification.appName,
          sourcePackageName = notification.sourcePackageName,
          replyCapability = notification.replyCapability,
          hasMarkReadAction = notification.hasMarkReadAction,
          hasContentIntent = notification.hasContentIntent,
          eventVersion = notification.eventVersion,
          isTestSimulation = notification.isTestSimulation,
          expressionOverride =
            if (notification.replyCapability != null) ExpressionState.WAVE_ALERT
            else ExpressionState.SURPRISED,
          actions =
            buildList {
              if (notification.replyCapability != null) {
                add(
                  ActivityAction(
                    ActivityActionType.REPLY,
                    notification.replyCapability.actionLabel,
                    isPrimary = true,
                  )
                )
              } else if (notification.hasContentIntent) {
                add(ActivityAction(ActivityActionType.OPEN_APP, "Buka", isPrimary = true))
              }
              if (notification.hasMarkReadAction) {
                add(ActivityAction(ActivityActionType.MARK_READ, "Dibaca"))
              }
              add(ActivityAction(ActivityActionType.COLLAPSE, "Ciutkan"))
              add(ActivityAction(ActivityActionType.DISMISS, "Tutup"))
            },
        )
      )
    }

    callSlot?.let { call ->
      list.add(
        ActivityState(
          id = call.key,
          type = ActivityType.CALL,
          title = call.callerName,
          subtitle = call.statusLabel,
          isOngoing = true,
          priority = 5,
          sourceAppName = call.appName,
          isVideoCall = call.isVideoCall,
          isCallMuted = call.isMuted,
          isCallVideoActive = call.isVideoActive,
          isTestSimulation = call.key.startsWith("test_"),
          expressionOverride = ExpressionState.WAVE_ALERT,
          actions =
            buildList {
              if (call.hasAnswerAction) {
                add(ActivityAction(ActivityActionType.CALL_ANSWER, "Angkat", isPrimary = true))
              }
              add(ActivityAction(ActivityActionType.CALL_TOGGLE_MUTE, "Bisu"))
              add(ActivityAction(ActivityActionType.CALL_TOGGLE_VIDEO, "Video"))
              if (call.hasDeclineAction) {
                add(ActivityAction(ActivityActionType.CALL_DECLINE, "Akhiri"))
              } else {
                add(ActivityAction(ActivityActionType.DISMISS, "Tutup"))
              }
            },
        )
      )
    }

    prayerAlarmSlot?.let { prayer ->
      list.add(
        ActivityState(
          id = "prayer_${prayer.prayerName.name}",
          type = ActivityType.PRAYER_ALARM,
          title = "Waktu ${prayer.prayerName.label}",
          subtitle = "${prayer.formattedTime}, ${prayer.cityName}",
          isOngoing = true,
          priority = 4,
          sourceAppName = "Jadwal Shalat",
          expressionOverride = ExpressionState.CHEERFUL,
          actions = listOf(ActivityAction(ActivityActionType.DISMISS_ALARM, "Selesai", true)),
        )
      )
    }

    if (lowBatteryAlertActive) {
      val pct = _batteryState.value.levelPercent
      list.add(
        ActivityState(
          id = "low_battery",
          type = ActivityType.LOW_BATTERY,
          title = "Baterai Lemah $pct%",
          subtitle = "Segera hubungkan pengisi daya",
          isOngoing = false,
          priority = 4,
          batteryLevel = pct,
          expressionOverride = ExpressionState.WORRIED,
          actions = listOf(ActivityAction(ActivityActionType.DISMISS, "Mengerti", true)),
        )
      )
    }

    timerSnapshot?.let { timer ->
      val progress =
        if (timer.totalSeconds > 0) {
          timer.remainingSeconds.toFloat() / timer.totalSeconds.toFloat()
        } else {
          0f
        }
      list.add(
        ActivityState(
          id = "live_timer",
          type = ActivityType.TIMER,
          title = if (timer.isFinished) "Timer Selesai" else formatTime(timer.remainingSeconds),
          subtitle =
            when {
              timer.isFinished -> "Waktu habis"
              timer.isPaused -> "Timer Dijeda"
              else -> "Timer Aktif"
            },
          progress = progress.coerceIn(0f, 1f),
          remainingSeconds = timer.remainingSeconds,
          totalSeconds = timer.totalSeconds,
          isPaused = timer.isPaused,
          isFinished = timer.isFinished,
          isOngoing = !timer.isFinished,
          priority = if (timer.isFinished) 4 else 3,
          expressionOverride =
            if (timer.isFinished) ExpressionState.WAVE_ALERT else ExpressionState.SMIRK,
          actions =
            listOf(
              ActivityAction(
                type = ActivityActionType.PAUSE_RESUME,
                label = if (timer.isPaused) "Lanjut" else "Jeda",
                isPrimary = true,
              ),
              ActivityAction(type = ActivityActionType.STOP, label = "Hentikan"),
            ),
        )
      )
    }

    stopwatchSnapshot?.let { sw ->
      list.add(
        ActivityState(
          id = "live_stopwatch",
          type = ActivityType.STOPWATCH,
          title = formatTime(sw.elapsedSeconds),
          subtitle = if (sw.isPaused) "Stopwatch Dijeda" else "Stopwatch Berjalan",
          elapsedSeconds = sw.elapsedSeconds,
          lapTimesFormatted = sw.laps,
          isPaused = sw.isPaused,
          isOngoing = !sw.isPaused,
          priority = 3,
          expressionOverride = ExpressionState.CHEERFUL,
          actions =
            listOf(
              ActivityAction(
                type = ActivityActionType.PAUSE_RESUME,
                label = if (sw.isPaused) "Lanjut" else "Jeda",
                isPrimary = true,
              ),
              ActivityAction(type = ActivityActionType.LAP, label = "Putaran"),
              ActivityAction(type = ActivityActionType.STOP, label = "Hentikan"),
            ),
        )
      )
    }

    if (chargingBannerActive) {
      val battery = _batteryState.value
      list.add(
        ActivityState(
          id = "charging_pulse",
          type = ActivityType.CHARGING,
          title = "Mengisi Daya ${battery.levelPercent}%",
          subtitle = battery.pluggedTypeLabel,
          isOngoing = false,
          priority = 2,
          batteryLevel = battery.levelPercent,
          progress = (battery.levelPercent / 100f).coerceIn(0f, 1f),
          expressionOverride = ExpressionState.HAPPY,
          actions = listOf(ActivityAction(ActivityActionType.DISMISS, "Tutup")),
        )
      )
    }

    mediaSnapshot?.let { media ->
      list.add(
        ActivityState(
          id = "media_${media.packageName}",
          type = ActivityType.MEDIA,
          title = media.title,
          subtitle = media.artist ?: media.appName,
          mediaArtist = media.artist,
          mediaIsPlaying = media.isPlaying,
          mediaAlbumArt = media.albumArt,
          mediaHasFavoriteAction = media.hasFavoriteAction,
          mediaIsFavorited = media.isFavorited,
          progress =
            if (media.durationMs > 0L) {
              (media.positionMs.toFloat() / media.durationMs.toFloat()).coerceIn(0f, 1f)
            } else {
              null
            },
          isOngoing = media.isPlaying,
          priority = 1,
          sourceAppName = media.appName,
          isTestSimulation = media.packageName == "com.example.music",
          expressionOverride =
            if (media.isPlaying) ExpressionState.SUNGLASSES else ExpressionState.SMILE,
          actions =
            buildList {
              if (media.hasPrevious) {
                add(ActivityAction(ActivityActionType.MEDIA_PREVIOUS, "Sebelumnya"))
              }
              add(
                ActivityAction(
                  ActivityActionType.MEDIA_PLAY_PAUSE,
                  if (media.isPlaying) "Jeda" else "Putar",
                  isPrimary = true,
                )
              )
              if (media.hasNext) {
                add(ActivityAction(ActivityActionType.MEDIA_NEXT, "Berikutnya"))
              }
              if (media.hasFavoriteAction) {
                add(ActivityAction(ActivityActionType.MEDIA_FAVORITE, "Favorit"))
              }
            },
        )
      )
    }

    return list.sortedByDescending { it.priority }
  }

  private fun launchTimerTicker() {
    timerTickerJob?.cancel()
    timerTickerJob =
      scope.launch {
        while (true) {
          delay(1000L)
          val current = timerSnapshot ?: break
          if (current.isPaused || current.isFinished) break
          val nextRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)
          val finished = nextRemaining == 0
          timerSnapshot =
            current.copy(
              remainingSeconds = nextRemaining,
              isFinished = finished,
              isPaused = finished,
            )
          if (finished) {
            _isOverlayExpanded.value = true
            _isDimmed.value = false
            publishState()
            break
          }
          publishState()
        }
      }
  }

  private fun launchStopwatchTicker() {
    stopwatchTickerJob?.cancel()
    stopwatchTickerJob =
      scope.launch {
        while (true) {
          delay(1000L)
          val current = stopwatchSnapshot ?: break
          if (current.isPaused) break
          stopwatchSnapshot = current.copy(elapsedSeconds = current.elapsedSeconds + 1)
          publishState()
        }
      }
  }

  private fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
  }
}
