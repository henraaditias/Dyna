package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CloseFullscreen
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.ActivityState
import com.example.domain.model.ActivityType
import com.example.domain.model.IslandAppearance
import com.example.ui.theme.DynaMonoTypography

@Composable
fun DynaIslandPill(
  activity: ActivityState,
  appearance: IslandAppearance,
  isExpanded: Boolean,
  showNotificationPreview: Boolean,
  maxWidthDp: Float = 340f,
  onClick: () -> Unit,
  onToggleTimerPause: () -> Unit = {},
  onStopTimer: () -> Unit = {},
  onDismissNotification: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val colors = appearance.resolvedColors()
  val bgColor = Color(colors.backgroundArgb)
  val fgColor = Color(colors.foregroundArgb)
  val accentColor = Color(colors.accentArgb)
  val iconColor = Color(colors.iconArgb)

  val collapsedWidth =
    when (activity.type) {
      ActivityType.IDLE -> appearance.widthDp
      ActivityType.TIMER -> (appearance.widthDp + 44f).coerceAtMost(maxWidthDp)
      ActivityType.NOTIFICATION -> (appearance.widthDp + 58f).coerceAtMost(maxWidthDp)
    }

  val targetWidthDp =
    if (isExpanded) {
      maxWidthDp.coerceIn(240f, 340f)
    } else {
      collapsedWidth.coerceIn(110f, maxWidthDp)
    }

  val targetHeightDp =
    if (isExpanded) {
      when (activity.type) {
        ActivityType.TIMER -> 132f
        ActivityType.NOTIFICATION -> 114f
        ActivityType.IDLE -> 92f
      }
    } else {
      appearance.heightDp.coerceIn(28f, 54f)
    }

  val targetRadiusDp =
    if (isExpanded) {
      appearance.cornerRadiusDp.coerceIn(16f, 32f)
    } else {
      appearance.cornerRadiusDp.coerceIn(12f, 28f)
    }

  val speed = appearance.animationSpeedMultiplier.coerceIn(0.6f, 1.5f)
  val springSpec =
    if (appearance.animationEnabled) {
      spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow * speed,
      )
    } else {
      snap()
    }

  val animWidth by
    animateDpAsState(targetValue = targetWidthDp.dp, animationSpec = springSpec, label = "pill_w")
  val animHeight by
    animateDpAsState(targetValue = targetHeightDp.dp, animationSpec = springSpec, label = "pill_h")
  val animRadius by
    animateDpAsState(targetValue = targetRadiusDp.dp, animationSpec = springSpec, label = "pill_r")
  val islandDescription = stringResource(R.string.cd_dyna_island)

  Surface(
    modifier =
      modifier
        .testTag("dyna_island_pill")
        .semantics { contentDescription = islandDescription }
        .width(animWidth)
        .heightIn(min = animHeight)
        .clip(RoundedCornerShape(animRadius))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(animRadius),
    color = bgColor,
    border =
      BorderStroke(
        width = 1.dp,
        color =
          if (isExpanded) accentColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.14f),
      ),
  ) {
    AnimatedContent(
      targetState = Pair(isExpanded, activity),
      transitionSpec = {
        val duration = if (appearance.animationEnabled) (180 / speed).toInt() else 0
        (fadeIn(tween(duration)) + slideInVertically(tween(duration)) { it / 8 })
          .togetherWith(fadeOut(tween((duration * 0.6f).toInt())))
      },
      label = "pill_content",
    ) { (expanded, state) ->
      if (!expanded) {
        CollapsedPillContent(
          activity = state,
          fgColor = fgColor,
          accentColor = accentColor,
          iconColor = iconColor,
        )
      } else {
        ExpandedPillContent(
          activity = state,
          fgColor = fgColor,
          accentColor = accentColor,
          iconColor = iconColor,
          showNotificationPreview = showNotificationPreview,
          onCollapse = onClick,
          onToggleTimerPause = onToggleTimerPause,
          onStopTimer = onStopTimer,
          onDismissNotification = onDismissNotification,
        )
      }
    }
  }
}

@Composable
private fun CollapsedPillContent(
  activity: ActivityState,
  fgColor: Color,
  accentColor: Color,
  iconColor: Color,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    when (activity.type) {
      ActivityType.IDLE -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
          Text(
            text = "DYNA",
            style = DynaMonoTypography.LabelMonoCaps.copy(fontSize = 10.sp),
            color = fgColor.copy(alpha = 0.72f),
          )
          Icon(
            imageVector = Icons.Outlined.RadioButtonChecked,
            contentDescription = null,
            tint = iconColor.copy(alpha = 0.75f),
            modifier = Modifier.size(14.dp),
          )
        }
      }
      ActivityType.NOTIFICATION -> {
        Icon(
          imageVector = Icons.Filled.Notifications,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = activity.title,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          color = fgColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accentColor))
      }
      ActivityType.TIMER -> {
        Icon(
          imageVector = Icons.Filled.Timer,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = activity.title,
          style =
            DynaMonoTypography.LabelCode.copy(
              fontWeight = FontWeight.Medium,
              fontFeatureSettings = "tnum",
            ),
          color = accentColor,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun ExpandedPillContent(
  activity: ActivityState,
  fgColor: Color,
  accentColor: Color,
  iconColor: Color,
  showNotificationPreview: Boolean,
  onCollapse: () -> Unit,
  onToggleTimerPause: () -> Unit,
  onStopTimer: () -> Unit,
  onDismissNotification: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.SpaceBetween,
  ) {
    when (activity.type) {
      ActivityType.IDLE -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(accentColor))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Dyna Aktif",
                style = MaterialTheme.typography.titleMedium,
                color = fgColor,
              )
              Text(
                text = "Tidak ada aktivitas berjalan",
                style = MaterialTheme.typography.bodySmall,
                color = fgColor.copy(alpha = 0.68f),
              )
            }
          }
          PillIconButton(
            icon = Icons.Outlined.CloseFullscreen,
            tint = fgColor.copy(alpha = 0.8f),
            contentDescription = "Ciutkan kapsul",
            onClick = onCollapse,
          )
        }
      }
      ActivityType.NOTIFICATION -> {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.Top,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Top) {
            Box(
              modifier =
                Modifier.size(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(accentColor.copy(alpha = 0.16f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.widthIn(max = 190.dp)) {
              Text(
                text = activity.sourceAppName ?: "Notifikasi",
                style = DynaMonoTypography.LabelMonoCaps.copy(fontSize = 10.sp),
                color = accentColor,
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
                    "Notifikasi masuk"
                  },
                style = MaterialTheme.typography.bodySmall,
                color = fgColor.copy(alpha = 0.72f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
          Row {
            PillIconButton(
              icon = Icons.Outlined.CloseFullscreen,
              tint = fgColor.copy(alpha = 0.8f),
              contentDescription = "Ciutkan notifikasi",
              onClick = onCollapse,
            )
            PillIconButton(
              icon = Icons.Filled.Close,
              tint = fgColor.copy(alpha = 0.8f),
              contentDescription = "Tutup notifikasi",
              onClick = onDismissNotification,
            )
          }
        }
      }
      ActivityType.TIMER -> {
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
                text = activity.subtitle ?: "Timer",
                style = MaterialTheme.typography.bodySmall,
                color = fgColor.copy(alpha = 0.72f),
              )
              Text(
                text = activity.title,
                style = DynaMonoTypography.TabularTimer,
                color = fgColor,
              )
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PillIconButton(
              icon = if (activity.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
              tint = fgColor,
              background = accentColor.copy(alpha = 0.25f),
              contentDescription = if (activity.isPaused) "Lanjutkan timer" else "Jeda timer",
              onClick = onToggleTimerPause,
            )
            PillIconButton(
              icon = Icons.Filled.Stop,
              tint = fgColor,
              background = Color.White.copy(alpha = 0.12f),
              contentDescription = "Hentikan timer",
              onClick = onStopTimer,
            )
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = fgColor.copy(alpha = 0.1f))
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
          progress = { activity.progress ?: 0f },
          modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
          color = accentColor,
          trackColor = fgColor.copy(alpha = 0.14f),
        )
      }
    }
  }
}

@Composable
private fun PillIconButton(
  icon: ImageVector,
  tint: Color,
  contentDescription: String,
  onClick: () -> Unit,
  background: Color = Color.White.copy(alpha = 0.08f),
) {
  Box(
    modifier = Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
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
