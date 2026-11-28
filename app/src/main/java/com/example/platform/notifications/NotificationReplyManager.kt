package com.example.platform.notifications

import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import com.example.domain.model.ReplyCapability
import java.util.concurrent.ConcurrentHashMap

class NotificationReplyManager(
  private val clockMs: () -> Long = { SystemClock.elapsedRealtime() },
) {

  private data class ReplyActionTarget(
    val actionIntent: PendingIntent,
    val remoteInputs: Array<RemoteInput>,
    val primaryResultKey: String,
    val capability: ReplyCapability,
  )

  private data class CallActionTargets(
    val answerIntent: PendingIntent?,
    val declineIntent: PendingIntent?,
  )

  private data class SentReplyRecord(
    val replyText: String,
    val titleSnapshot: String,
    val textSnapshot: String?,
    val sentAtMs: Long,
  )

  private val targets = ConcurrentHashMap<String, ReplyActionTarget>()
  private val markReadIntents = ConcurrentHashMap<String, PendingIntent>()
  private val contentIntents = ConcurrentHashMap<String, PendingIntent>()
  private val callTargets = ConcurrentHashMap<String, CallActionTargets>()
  private val recentReplies = ConcurrentHashMap<String, SentReplyRecord>()

  fun registerContentIntent(key: String, notification: Notification): Boolean {
    val intent = notification.contentIntent
    if (intent != null) {
      contentIntents[key] = intent
      return true
    }
    contentIntents.remove(key)
    return false
  }

  fun registerMarkReadAction(key: String, notification: Notification): Boolean {
    val allActions =
      buildList {
        notification.actions?.let { addAll(it) }
        Notification.WearableExtender(notification).actions?.let { addAll(it) }
      }
    val markAction =
      allActions.firstOrNull { action ->
        if (!action.remoteInputs.isNullOrEmpty()) return@firstOrNull false
        val titleLower = action.title?.toString()?.lowercase().orEmpty()
        action.actionIntent != null &&
          (titleLower.contains("mark") ||
            titleLower.contains("read") ||
            titleLower.contains("dibaca") ||
            titleLower.contains("tandai"))
      }
    val intent = markAction?.actionIntent
    if (intent != null) {
      markReadIntents[key] = intent
      return true
    }
    markReadIntents.remove(key)
    return false
  }

  fun triggerMarkRead(context: Context, key: String): Boolean {
    val intent = markReadIntents.remove(key) ?: return false
    return sendPendingIntent(context, intent)
  }

  fun setSystemMicrophoneMute(context: Context, muted: Boolean) {
    runCatching {
      val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
      audioManager?.isMicrophoneMute = muted
    }
  }

  fun openContentIntent(context: Context, key: String): Boolean {
    val intent = contentIntents[key] ?: return false
    return sendPendingIntent(context, intent)
  }

  fun registerCallActions(key: String, notification: Notification): Pair<Boolean, Boolean> {
    val allActions =
      buildList {
        notification.actions?.let { addAll(it) }
        Notification.WearableExtender(notification).actions?.let { addAll(it) }
      }

    var answer: PendingIntent? = null
    var decline: PendingIntent? = null

    for (action in allActions) {
      val intent = action.actionIntent ?: continue
      val titleLower = action.title?.toString()?.lowercase().orEmpty()
      when {
        titleLower.contains("answer") ||
          titleLower.contains("accept") ||
          titleLower.contains("angkat") ||
          titleLower.contains("terima") ||
          titleLower.contains("jawab") -> if (answer == null) answer = intent
        titleLower.contains("decline") ||
          titleLower.contains("reject") ||
          titleLower.contains("hang") ||
          titleLower.contains("end") ||
          titleLower.contains("tolak") ||
          titleLower.contains("tutup") ||
          titleLower.contains("akhiri") -> if (decline == null) decline = intent
      }
    }

    if (answer == null && decline == null && allActions.size >= 2) {
      decline = allActions[0].actionIntent
      answer = allActions[1].actionIntent
    } else if (decline == null && allActions.size == 1 && allActions[0].actionIntent != answer) {
      decline = allActions[0].actionIntent
    }

    callTargets[key] = CallActionTargets(answerIntent = answer, declineIntent = decline)
    return (answer != null) to (decline != null)
  }

  fun triggerCallAnswer(context: Context, key: String): Boolean {
    val target = callTargets[key]?.answerIntent ?: return false
    return sendPendingIntent(context, target)
  }

  fun triggerCallDecline(context: Context, key: String): Boolean {
    val target = callTargets[key]?.declineIntent ?: return false
    val sent = sendPendingIntent(context, target)
    if (sent) callTargets.remove(key)
    return sent
  }

  fun extractAndRegister(key: String, notification: Notification): ReplyCapability? {
    val allActions =
      buildList {
        notification.actions?.let { addAll(it) }
        Notification.WearableExtender(notification).actions?.let { addAll(it) }
      }

    for (action in allActions) {
      val intent = action.actionIntent ?: continue
      val remoteInputs = action.remoteInputs
      if (remoteInputs.isNullOrEmpty()) continue

      val primaryInput =
        remoteInputs.firstOrNull { it.allowFreeFormInput } ?: remoteInputs.first()
      val resultKey = primaryInput.resultKey ?: continue

      val label = action.title?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: "Balas"
      val placeholder = primaryInput.label?.toString()?.trim()?.takeIf { it.isNotEmpty() }
      val choices =
        primaryInput.choices
          ?.mapNotNull { it?.toString()?.trim()?.takeIf(String::isNotEmpty) }
          ?.distinct()
          ?.take(3)
          .orEmpty()

      val capability =
        ReplyCapability(
          actionLabel = label,
          placeholder = placeholder,
          quickChoices = choices,
        )
      targets[key] =
        ReplyActionTarget(
          actionIntent = intent,
          remoteInputs = remoteInputs,
          primaryResultKey = resultKey,
          capability = capability,
        )
      return capability
    }

    targets.remove(key)
    return null
  }

  fun shouldIgnoreAsReplyEcho(
    key: String,
    notification: Notification,
    title: String,
    text: String?,
  ): Boolean {
    val record = recentReplies[key] ?: return false
    val elapsed = clockMs() - record.sentAtMs
    if (elapsed > REPLY_ECHO_WINDOW_MS) {
      recentReplies.remove(key)
      return false
    }

    val history = notification.extras?.getCharSequenceArray(Notification.EXTRA_REMOTE_INPUT_HISTORY)
    val latestHistoryEntry = history?.firstOrNull()?.toString()?.trim()
    if (!latestHistoryEntry.isNullOrEmpty() && latestHistoryEntry == record.replyText) {
      return true
    }

    if (title == record.titleSnapshot && text == record.textSnapshot) {
      return true
    }

    return false
  }

  fun sendReply(
    context: Context,
    key: String,
    replyText: String,
    titleSnapshot: String = "",
    textSnapshot: String? = null,
  ): Boolean {
    val trimmed = replyText.trim()
    if (trimmed.isEmpty()) return false
    val target = targets[key] ?: return false

    val fillInIntent = Intent()
    val results = Bundle().apply { putCharSequence(target.primaryResultKey, trimmed) }
    RemoteInput.addResultsToIntent(target.remoteInputs, fillInIntent, results)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      RemoteInput.setResultsSource(fillInIntent, RemoteInput.SOURCE_FREE_FORM_INPUT)
    }

    val sent =
      runCatching {
          target.actionIntent.send(context, 0, fillInIntent)
          true
        }
        .getOrDefault(false)

    if (sent) {
      recentReplies[key] =
        SentReplyRecord(
          replyText = trimmed,
          titleSnapshot = titleSnapshot,
          textSnapshot = textSnapshot,
          sentAtMs = clockMs(),
        )
      targets.remove(key)
      markReadIntents.remove(key)
      contentIntents.remove(key)
    }
    return sent
  }

  fun remove(key: String) {
    targets.remove(key)
    markReadIntents.remove(key)
    contentIntents.remove(key)
    callTargets.remove(key)
  }

  fun clear() {
    targets.clear()
    markReadIntents.clear()
    contentIntents.clear()
    callTargets.clear()
    recentReplies.clear()
  }

  private fun sendPendingIntent(context: Context, pendingIntent: PendingIntent): Boolean {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
          val opts =
            ActivityOptions.makeBasic()
              .setPendingIntentBackgroundActivityStartMode(
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
              )
          pendingIntent.send(context, 0, null, null, null, null, opts.toBundle())
        } else {
          pendingIntent.send()
        }
        true
      }
      .getOrElse {
        runCatching {
            pendingIntent.send()
            true
          }
          .getOrDefault(false)
      }
  }

  private companion object {
    const val REPLY_ECHO_WINDOW_MS = 3_000L
  }
}
