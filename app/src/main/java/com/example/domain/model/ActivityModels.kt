package com.example.domain.model

enum class ActivityType {
  IDLE,
  TIMER,
  NOTIFICATION,
}

enum class ActivityActionType {
  PAUSE_RESUME,
  STOP,
  COLLAPSE,
  DISMISS,
}

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
  val isPaused: Boolean = false,
  val isFinished: Boolean = false,
  val isOngoing: Boolean = false,
  val priority: Int = 0,
  val sourceAppName: String? = null,
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
      )
  }
}
