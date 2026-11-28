package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ActivityActionType
import com.example.domain.model.ActivityState
import com.example.domain.model.ActivityType
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.DynaPalette
import com.example.ui.theme.LocalDynaStrings

@Composable
fun ExpandedNotificationContent(
  activity: ActivityState,
  fgColor: Color,
  accentColor: Color,
  iconColor: Color,
  showNotificationPreview: Boolean,
  isReplying: Boolean,
  onCollapse: () -> Unit,
  onDismissNotification: () -> Unit,
  onMarkRead: () -> Unit,
  onOpenApp: () -> Unit,
  onStartReply: () -> Unit,
  onCancelReply: () -> Unit,
  onSendReply: (String) -> Unit,
) {
  val strings = LocalDynaStrings.current
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
        Box(
          modifier =
            Modifier.size(34.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(DynaPalette.IslandSurface)
              .border(1.dp, DynaPalette.IslandBorder, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center,
        ) {
          val expr = activity.expressionOverride
          if (expr != null) {
            DynamicIslandFace(
              expression = expr,
              featureColor = fgColor,
              accentColor = accentColor,
              sizeDp = 20.dp,
            )
          } else {
            Icon(
              imageVector = Icons.Filled.Notifications,
              contentDescription = null,
              tint = iconColor,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.widthIn(max = 180.dp)) {
          Text(
            text = activity.sourceAppName ?: "Dyna",
            style = DynaMonoTypography.LabelMono.copy(fontSize = 10.sp),
            color = DynaPalette.IslandTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = activity.title,
            style = MaterialTheme.typography.titleMedium,
            color = fgColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text =
              if (showNotificationPreview && !activity.subtitle.isNullOrBlank()) {
                activity.subtitle
              } else {
                strings.historySectionTitle
              },
            style = MaterialTheme.typography.bodySmall,
            color = DynaPalette.IslandTextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Row {
        if (activity.hasMarkReadAction) {
          PillIconButton(
            icon = Icons.Filled.DoneAll,
            tint = fgColor,
            contentDescription = strings.pillMarkReadAction,
            testTag = "pill_notif_mark_read",
            onClick = onMarkRead,
          )
        }
        if (activity.hasContentIntent && activity.replyCapability == null) {
          PillIconButton(
            icon = Icons.AutoMirrored.Filled.OpenInNew,
            tint = fgColor,
            contentDescription = strings.pillOpenAction,
            testTag = "pill_notif_open_app",
            onClick = onOpenApp,
          )
        }
        PillIconButton(
          icon = Icons.Outlined.CloseFullscreen,
          tint = DynaPalette.IslandTextSecondary,
          contentDescription = strings.closeAction,
          onClick = onCollapse,
        )
        PillIconButton(
          icon = Icons.Filled.Close,
          tint = DynaPalette.IslandTextSecondary,
          contentDescription = strings.closeAction,
          testTag = "pill_notif_dismiss",
          onClick = onDismissNotification,
        )
      }
    }

    val replyCapability = activity.replyCapability
    if (replyCapability != null) {
      Spacer(modifier = Modifier.height(6.dp))
      if (!isReplying) {
        NotificationReplyActionsRow(
          actionLabel =
            replyCapability.actionLabel.ifBlank { strings.pillReplyAction },
          quickChoices = replyCapability.quickChoices,
          fgColor = fgColor,
          accentColor = accentColor,
          onStartReply = onStartReply,
          onSendQuickReply = onSendReply,
        )
      } else {
        NotificationReplyComposer(
          notificationId = activity.id,
          placeholder = replyCapability.placeholder ?: strings.pillReplyPlaceholder,
          fgColor = fgColor,
          accentColor = accentColor,
          onCancelReply = onCancelReply,
          onSendReply = onSendReply,
        )
      }
    }
  }
}

@Composable
fun ExpandedTimerOrStopwatchContent(
  activity: ActivityState,
  isStopwatch: Boolean,
  fgColor: Color,
  accentColor: Color,
  iconColor: Color,
  onTogglePause: () -> Unit,
  onLap: () -> Unit,
  onStop: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Timer,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = activity.subtitle ?: if (isStopwatch) "Stopwatch" else "Timer",
            style = MaterialTheme.typography.bodySmall,
            color = DynaPalette.IslandTextSecondary,
          )
          Text(
            text = activity.title,
            style = DynaMonoTypography.TabularTimer,
            color = fgColor,
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        PillIconButton(
          icon = if (activity.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
          tint = fgColor,
          background = DynaPalette.IslandSurface,
          contentDescription = if (activity.isPaused) "Lanjut" else "Jeda",
          onClick = onTogglePause,
        )
        if (isStopwatch) {
          PillIconButton(
            icon = Icons.Filled.Flag,
            tint = fgColor,
            background = DynaPalette.IslandSurface,
            contentDescription = "Lap",
            testTag = "pill_stopwatch_lap",
            onClick = onLap,
          )
        }
        PillIconButton(
          icon = Icons.Filled.Stop,
          tint = DynaPalette.IslandIndicatorEnd,
          background = DynaPalette.IslandSurface,
          contentDescription = "Stop",
          onClick = onStop,
        )
      }
    }

    if (!isStopwatch) {
      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = DynaPalette.IslandBorder)
      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { activity.progress ?: 0f },
        modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
        color = accentColor,
        trackColor = DynaPalette.IslandSurface,
      )
    } else if (activity.lapTimesFormatted.isNotEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = activity.lapTimesFormatted.joinToString(" | "),
        style = DynaMonoTypography.LabelCode.copy(fontSize = 11.sp),
        color = DynaPalette.IslandTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun ExpandedMediaContent(
  activity: ActivityState,
  fgColor: Color,
  accentColor: Color,
  iconColor: Color,
  onPrevious: () -> Unit,
  onPlayPause: () -> Unit,
  onNext: () -> Unit,
  onToggleFavorite: () -> Unit,
  onCollapse: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier.size(42.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(DynaPalette.IslandSurface)
              .border(1.dp, DynaPalette.IslandBorder, RoundedCornerShape(10.dp)),
          contentAlignment = Alignment.Center,
        ) {
          val art = activity.mediaAlbumArt
          if (art != null) {
            Image(
              bitmap = art.asImageBitmap(),
              contentDescription = activity.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(10.dp)),
            )
          } else {
            Icon(
              imageVector = Icons.Filled.MusicNote,
              contentDescription = null,
              tint = iconColor,
              modifier = Modifier.size(18.dp),
            )
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = activity.sourceAppName ?: "Musik",
            style = DynaMonoTypography.LabelMono.copy(fontSize = 10.sp),
            color = DynaPalette.IslandTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = activity.title,
            style = MaterialTheme.typography.titleMedium,
            color = fgColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = activity.mediaArtist ?: activity.subtitle ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = DynaPalette.IslandTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      PillIconButton(
        icon = Icons.Outlined.CloseFullscreen,
        tint = DynaPalette.IslandTextSecondary,
        contentDescription = "Ciutkan",
        onClick = onCollapse,
      )
    }

    activity.progress?.let { progressValue ->
      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { progressValue },
        modifier = Modifier.fillMaxWidth().height(3.dp).clip(CircleShape),
        color = accentColor,
        trackColor = DynaPalette.IslandSurface,
      )
    }

    Spacer(modifier = Modifier.height(6.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PillIconButton(
        icon = Icons.Filled.SkipPrevious,
        tint = fgColor,
        contentDescription = "Sebelumnya",
        testTag = "pill_media_prev",
        onClick = onPrevious,
      )
      Spacer(modifier = Modifier.width(6.dp))
      PillIconButton(
        icon = if (activity.mediaIsPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
        tint = DynaPalette.IslandBg,
        background = fgColor,
        contentDescription = if (activity.mediaIsPlaying) "Jeda" else "Putar",
        testTag = "pill_media_play_pause",
        onClick = onPlayPause,
      )
      Spacer(modifier = Modifier.width(6.dp))
      PillIconButton(
        icon = Icons.Filled.SkipNext,
        tint = fgColor,
        contentDescription = "Berikutnya",
        testTag = "pill_media_next",
        onClick = onNext,
      )
      if (activity.mediaHasFavoriteAction) {
        Spacer(modifier = Modifier.width(6.dp))
        PillIconButton(
          icon =
            if (activity.mediaIsFavorited) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
          tint =
            if (activity.mediaIsFavorited) DynaPalette.IslandTextPrimary
            else DynaPalette.IslandTextSecondary,
          background = DynaPalette.IslandSurface,
          contentDescription = "Favorit",
          testTag = "pill_media_favorite",
          onClick = onToggleFavorite,
        )
      }
    }
  }
}

@Composable
fun ExpandedCallContent(
  activity: ActivityState,
  fgColor: Color,
  onAnswer: () -> Unit,
  onToggleMute: () -> Unit,
  onToggleVideo: () -> Unit,
  onDecline: () -> Unit,
  onDismiss: () -> Unit,
) {
  val strings = LocalDynaStrings.current
  val hasAnswer = activity.actions.any { it.type == ActivityActionType.CALL_ANSWER }
  val hasDecline = activity.actions.any { it.type == ActivityActionType.CALL_DECLINE }

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier.size(36.dp)
              .clip(CircleShape)
              .background(DynaPalette.IslandSurface)
              .border(1.dp, DynaPalette.IslandBorder, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector =
              if (activity.isCallVideoActive || activity.isVideoCall) Icons.Filled.Videocam
              else Icons.Filled.Call,
            contentDescription = null,
            tint =
              if (activity.isCallMuted) DynaPalette.IslandIndicatorMute
              else DynaPalette.IslandIndicatorLive,
            modifier = Modifier.size(18.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = activity.sourceAppName ?: "Panggilan",
            style = DynaMonoTypography.LabelMono.copy(fontSize = 10.sp),
            color = DynaPalette.IslandTextSecondary,
            maxLines = 1,
          )
          Text(
            text = activity.title,
            style = MaterialTheme.typography.titleMedium,
            color = fgColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text =
              buildString {
                append(activity.subtitle ?: "")
                if (activity.isCallMuted) {
                  if (isNotEmpty()) append(" • ")
                  append(strings.pillCallMicMuted)
                }
              },
            style = MaterialTheme.typography.bodySmall,
            color = DynaPalette.IslandTextSecondary,
            maxLines = 1,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (hasAnswer) {
        PillIconButton(
          icon = Icons.Filled.Call,
          tint = DynaPalette.IslandBg,
          background = DynaPalette.IslandIndicatorLive,
          contentDescription = strings.pillAnswerCall,
          testTag = "pill_call_answer",
          onClick = onAnswer,
        )
      }
      PillIconButton(
        icon = if (activity.isCallMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
        tint =
          if (activity.isCallMuted) DynaPalette.IslandIndicatorMute
          else DynaPalette.IslandIndicatorLive,
        background = DynaPalette.IslandSurface,
        contentDescription =
          if (activity.isCallMuted) strings.pillUnmuteMic else strings.pillMuteMic,
        testTag = "pill_call_mute",
        onClick = onToggleMute,
      )
      PillIconButton(
        icon = if (activity.isCallVideoActive) Icons.Filled.VideocamOff else Icons.Filled.Videocam,
        tint = fgColor,
        background = DynaPalette.IslandSurface,
        contentDescription =
          if (activity.isCallVideoActive) strings.pillPauseVideo else strings.pillSwitchVideo,
        testTag = "pill_call_video",
        onClick = onToggleVideo,
      )
      PillIconButton(
        icon = Icons.Filled.CallEnd,
        tint = DynaPalette.IslandTextPrimary,
        background = DynaPalette.IslandIndicatorEnd,
        contentDescription = strings.pillEndCall,
        testTag = "pill_call_decline",
        onClick = if (hasDecline) onDecline else onDismiss,
      )
    }
  }
}

@Composable
fun ExpandedSystemBannerContent(
  activity: ActivityState,
  icon: ImageVector,
  tintColor: Color,
  fgColor: Color,
  onDismiss: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier.size(36.dp)
            .clip(CircleShape)
            .background(DynaPalette.IslandSurface)
            .border(1.dp, DynaPalette.IslandBorder, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tintColor,
          modifier = Modifier.size(18.dp),
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = activity.title,
          style = MaterialTheme.typography.titleMedium,
          color = fgColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        activity.subtitle?.let { sub ->
          Text(
            text = sub,
            style = MaterialTheme.typography.bodySmall,
            color = DynaPalette.IslandTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
    PillIconButton(
      icon = Icons.Filled.Close,
      tint = DynaPalette.IslandTextSecondary,
      contentDescription = "Tutup",
      onClick = onDismiss,
    )
  }
}

@Composable
private fun NotificationReplyActionsRow(
  actionLabel: String,
  quickChoices: List<String>,
  fgColor: Color,
  accentColor: Color,
  onStartReply: () -> Unit,
  onSendQuickReply: (String) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Box(
      modifier =
        Modifier.heightIn(min = 48.dp)
          .clip(CircleShape)
          .clickable(onClick = onStartReply)
          .testTag("pill_reply_button"),
      contentAlignment = Alignment.Center,
    ) {
      Row(
        modifier =
          Modifier.clip(CircleShape)
            .background(DynaPalette.IslandSurface)
            .border(1.dp, DynaPalette.IslandBorder, CircleShape)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Reply,
          contentDescription = null,
          tint = accentColor,
          modifier = Modifier.size(15.dp),
        )
        Text(
          text = actionLabel,
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
          color = fgColor,
          maxLines = 1,
        )
      }
    }

    quickChoices.take(2).forEachIndexed { index, choice ->
      Box(
        modifier =
          Modifier.heightIn(min = 48.dp)
            .clip(CircleShape)
            .clickable { onSendQuickReply(choice) }
            .testTag("pill_quick_reply_$index"),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier =
            Modifier.clip(CircleShape)
              .background(DynaPalette.IslandSurface)
              .border(1.dp, DynaPalette.IslandBorder, CircleShape)
              .padding(horizontal = 10.dp, vertical = 7.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = choice,
            style = MaterialTheme.typography.labelMedium,
            color = fgColor.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@Composable
private fun NotificationReplyComposer(
  notificationId: String,
  placeholder: String,
  fgColor: Color,
  accentColor: Color,
  onCancelReply: () -> Unit,
  onSendReply: (String) -> Unit,
) {
  var replyText by remember(notificationId) { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val canSend = replyText.isNotBlank()

  LaunchedEffect(notificationId) {
    focusRequester.requestFocus()
    keyboardController?.show()
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(2.dp),
  ) {
    Box(
      modifier =
        Modifier.weight(1f)
          .heightIn(min = 42.dp)
          .clip(RoundedCornerShape(21.dp))
          .background(DynaPalette.IslandSurface)
          .border(1.dp, DynaPalette.IslandBorder, RoundedCornerShape(21.dp))
          .padding(horizontal = 14.dp, vertical = 10.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      if (replyText.isEmpty()) {
        Text(
          text = placeholder,
          style = MaterialTheme.typography.bodySmall,
          color = DynaPalette.IslandTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      BasicTextField(
        value = replyText,
        onValueChange = { replyText = it },
        singleLine = true,
        textStyle = MaterialTheme.typography.bodySmall.copy(color = fgColor),
        cursorBrush = SolidColor(accentColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions =
          KeyboardActions(
            onSend = {
              if (replyText.isNotBlank()) {
                val outgoing = replyText
                replyText = ""
                onSendReply(outgoing)
              }
            }
          ),
        modifier =
          Modifier.fillMaxWidth().focusRequester(focusRequester).testTag("pill_reply_input"),
      )
    }

    PillIconButton(
      icon = Icons.Filled.Close,
      tint = DynaPalette.IslandTextSecondary,
      contentDescription = "Batal",
      testTag = "pill_reply_cancel_button",
      onClick = {
        replyText = ""
        onCancelReply()
      },
    )

    PillIconButton(
      icon = Icons.AutoMirrored.Filled.Send,
      tint = if (canSend) DynaPalette.IslandBg else DynaPalette.IslandTextSecondary,
      background = if (canSend) fgColor else DynaPalette.IslandSurface,
      contentDescription = "Kirim",
      testTag = "pill_reply_send_button",
      enabled = canSend,
      onClick = {
        if (replyText.isNotBlank()) {
          val outgoing = replyText
          replyText = ""
          onSendReply(outgoing)
        }
      },
    )
  }
}

@Composable
fun PillIconButton(
  icon: ImageVector,
  tint: Color,
  contentDescription: String,
  onClick: () -> Unit,
  background: Color = DynaPalette.IslandSurface,
  enabled: Boolean = true,
  testTag: String? = null,
) {
  val baseModifier =
    Modifier.size(48.dp)
      .clip(CircleShape)
      .clickable(enabled = enabled, onClick = onClick)
      .let { if (testTag != null) it.testTag(testTag) else it }

  Box(modifier = baseModifier, contentAlignment = Alignment.Center) {
    Box(
      modifier = Modifier.size(34.dp).clip(CircleShape).background(background),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
fun systemActivityIcon(type: ActivityType): Pair<ImageVector, Color> =
  when (type) {
    ActivityType.CHARGING ->
      Icons.Filled.BatteryChargingFull to DynaPalette.IslandIndicatorLive
    ActivityType.LOW_BATTERY ->
      Icons.Filled.BatteryAlert to DynaPalette.IslandIndicatorEnd
    ActivityType.PRAYER_ALARM ->
      Icons.Filled.Mosque to DynaPalette.IslandTextPrimary
    else -> Icons.Filled.Notifications to DynaPalette.IslandTextPrimary
  }
