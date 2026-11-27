package com.example.data.activities

import com.example.domain.model.ActivityAction
import com.example.domain.model.ActivityActionType
import com.example.domain.model.ActivityState
import com.example.domain.model.ActivityType
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

  private val _currentActivity = MutableStateFlow(ActivityState.Idle)
  override val currentActivity: StateFlow<ActivityState> = _currentActivity.asStateFlow()

  private var timerSnapshot: TimerSnapshot? = null
  private var notificationSnapshot: NotificationSnapshot? = null
  private var tickerJob: Job? = null

  private data class TimerSnapshot(
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val isPaused: Boolean,
    val isFinished: Boolean,
  )

  private data class NotificationSnapshot(
    val key: String,
    val appName: String,
    val title: String,
    val text: String?,
  )

  override fun toggleOverlayExpanded() {
    _isOverlayExpanded.value = !_isOverlayExpanded.value
  }

  override fun setOverlayExpanded(expanded: Boolean) {
    _isOverlayExpanded.value = expanded
  }

  override fun startTimer(durationSeconds: Int) {
    tickerJob?.cancel()
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
    launchTicker()
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
      tickerJob?.cancel()
    } else {
      launchTicker()
    }
  }

  override fun stopTimer() {
    tickerJob?.cancel()
    timerSnapshot = null
    if (notificationSnapshot == null) {
      _isOverlayExpanded.value = false
    }
    publishState()
  }

  override fun onNotificationPosted(
    key: String,
    appName: String,
    title: String,
    text: String?,
    autoExpand: Boolean,
  ) {
    notificationSnapshot =
      NotificationSnapshot(key = key, appName = appName, title = title, text = text)
    if (autoExpand && timerSnapshot == null) {
      _isOverlayExpanded.value = true
    }
    publishState()
  }

  override fun onNotificationRemoved(key: String) {
    if (notificationSnapshot?.key == key) {
      notificationSnapshot = null
      if (timerSnapshot == null) {
        _isOverlayExpanded.value = false
      }
      publishState()
    }
  }

  override fun dismissActiveNotification() {
    notificationSnapshot = null
    if (timerSnapshot == null) {
      _isOverlayExpanded.value = false
    }
    publishState()
  }

  private fun publishState() {
    val timer = timerSnapshot
    val notification = notificationSnapshot
    _currentActivity.value =
      when {
        timer != null -> {
          val progress =
            if (timer.totalSeconds > 0) {
              timer.remainingSeconds.toFloat() / timer.totalSeconds.toFloat()
            } else {
              0f
            }
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
            priority = 3,
            actions =
              listOf(
                ActivityAction(
                  type = ActivityActionType.PAUSE_RESUME,
                  label = if (timer.isPaused) "Lanjut" else "Jeda",
                  isPrimary = true,
                ),
                ActivityAction(
                  type = ActivityActionType.STOP,
                  label = "Hentikan",
                  isPrimary = false,
                ),
              ),
          )
        }
        notification != null -> {
          ActivityState(
            id = notification.key,
            type = ActivityType.NOTIFICATION,
            title = notification.title,
            subtitle = notification.text,
            isOngoing = false,
            priority = 2,
            sourceAppName = notification.appName,
            actions =
              listOf(
                ActivityAction(ActivityActionType.COLLAPSE, "Ciutkan"),
                ActivityAction(ActivityActionType.DISMISS, "Tutup"),
              ),
          )
        }
        else -> ActivityState.Idle
      }
  }

  private fun launchTicker() {
    tickerJob?.cancel()
    tickerJob =
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
            publishState()
            break
          }
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
