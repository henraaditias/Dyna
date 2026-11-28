package com.example.domain.model

import android.graphics.Bitmap

enum class ActivityType {
  IDLE,
  TIMER,
  STOPWATCH,
  NOTIFICATION,
  MEDIA,
  CALL,
  CHARGING,
  LOW_BATTERY,
  PRAYER_ALARM,
}

enum class ActivityActionType {
  PAUSE_RESUME,
  STOP,
  LAP,
  COLLAPSE,
  DISMISS,
  REPLY,
  MARK_READ,
  OPEN_APP,
  MEDIA_PLAY_PAUSE,
  MEDIA_NEXT,
  MEDIA_PREVIOUS,
  MEDIA_FAVORITE,
  CALL_ANSWER,
  CALL_DECLINE,
  CALL_TOGGLE_MUTE,
  CALL_TOGGLE_VIDEO,
  DISMISS_ALARM,
}

data class ReplyCapability(
  val actionLabel: String = "Balas",
  val placeholder: String? = null,
  val quickChoices: List<String> = emptyList(),
)

data class SecondaryActivityBadge(
  val type: ActivityType,
  val label: String,
)

data class ActivityAction(
  val type: ActivityActionType,
  val label: String,
  val isPrimary: Boolean = false,
)

data class ActivityState(
  val id: String,
  val type: ActivityType,
  val title: String,
  val subtitle: String? = null,
  val progress: Float? = null,
  val remainingSeconds: Int? = null,
  val totalSeconds: Int? = null,
  val elapsedSeconds: Int? = null,
  val lapTimesFormatted: List<String> = emptyList(),
  val isPaused: Boolean = false,
  val isFinished: Boolean = false,
  val isOngoing: Boolean = false,
  val priority: Int = 0,
  val sourceAppName: String? = null,
  val sourcePackageName: String? = null,
  val replyCapability: ReplyCapability? = null,
  val hasMarkReadAction: Boolean = false,
  val hasContentIntent: Boolean = false,
  val isVideoCall: Boolean = false,
  val isCallMuted: Boolean = false,
  val isCallVideoActive: Boolean = false,
  val batteryLevel: Int? = null,
  val mediaArtist: String? = null,
  val mediaIsPlaying: Boolean = false,
  val mediaAlbumArt: Bitmap? = null,
  val mediaHasFavoriteAction: Boolean = false,
  val mediaIsFavorited: Boolean = false,
  val expressionOverride: ExpressionState? = null,
  val secondaryBadge: SecondaryActivityBadge? = null,
  val eventVersion: Long = 0L,
  val isTestSimulation: Boolean = false,
  val actions: List<ActivityAction> = emptyList(),
) {
  companion object {
    val Idle =
      ActivityState(
        id = "idle",
        type = ActivityType.IDLE,
        title = "Siap",
        subtitle = null,
        isOngoing = false,
        priority = 0,
      )
  }
}
