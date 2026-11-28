package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.domain.model.AppLanguage
import com.example.domain.model.BatterySnapshot
import com.example.domain.model.ExpressionState
import com.example.domain.model.IdleDisplayMode
import com.example.domain.model.IdlePlacement
import com.example.domain.model.IslandAppearance
import com.example.domain.model.PrayerSchedule
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.QuickMenuItem
import com.example.domain.model.ShortcutAppItem
import com.example.domain.model.WeatherSnapshot
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.DynaPalette
import com.example.ui.theme.LocalDynaStrings
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DynaIslandPill(
  activity: ActivityState,
  appearance: IslandAppearance,
  isExpanded: Boolean,
  showNotificationPreview: Boolean,
  isReplying: Boolean = false,
  isDimmed: Boolean = false,
  isIslandHidden: Boolean = false,
  quickMenuCategory: QuickMenuCategory = QuickMenuCategory.NONE,
  quickMenuItems: List<QuickMenuItem> = QuickMenuItem.defaultItems,
  transientExpression: ExpressionState? = null,
  batteryState: BatterySnapshot = BatterySnapshot(),
  weather: WeatherSnapshot = WeatherSnapshot(),
  prayerSchedule: PrayerSchedule = PrayerSchedule(),
  shortcutSlots: List<ShortcutAppItem?> = emptyList(),
  installedApps: List<ShortcutAppItem> = emptyList(),
  appIconProvider: (String) -> Bitmap? = { null },
  language: AppLanguage = AppLanguage.ID,
  hideTopDotEnabled: Boolean = true,
  maxWidthDp: Float = 344f,
  onClick: () -> Unit,
  onDoubleClick: () -> Unit = {},
  onLongClick: () -> Unit = {},
  onSwipeUpCollapse: () -> Unit = onClick,
  onHideIsland: () -> Unit = {},
  onRestoreIsland: () -> Unit = {},
  onToggleTimerPause: () -> Unit = {},
  onStopTimer: () -> Unit = {},
  onStartTimer: (Int) -> Unit = {},
  onStartStopwatch: () -> Unit = {},
  onToggleStopwatchPause: () -> Unit = {},
  onLapStopwatch: () -> Unit = {},
  onStopStopwatch: () -> Unit = {},
  onDismissNotification: () -> Unit = {},
  onMarkNotificationRead: () -> Unit = {},
  onOpenNotificationApp: () -> Unit = {},
  onStartReply: () -> Unit = {},
  onCancelReply: () -> Unit = {},
  onSendReply: (String) -> Unit = {},
  onMediaPlayPause: () -> Unit = {},
  onMediaNext: () -> Unit = {},
  onMediaPrevious: () -> Unit = {},
  onMediaFavorite: () -> Unit = {},
  onCallAnswer: () -> Unit = {},
  onCallToggleMute: () -> Unit = {},
  onCallToggleVideo: () -> Unit = {},
  onCallDecline: () -> Unit = {},
  onDismissCall: () -> Unit = {},
  onDismissCharging: () -> Unit = {},
  onDismissLowBattery: () -> Unit = {},
  onDismissPrayerAlarm: () -> Unit = {},
  onSelectQuickCategory: (QuickMenuCategory) -> Unit = {},
  onOpenCalendarDate: (LocalDate) -> Unit = {},
  onRefreshWeather: () -> Unit = {},
  onLaunchShortcut: (String) -> Unit = {},
  onAssignShortcutSlot: (Int, String?) -> Unit = { _, _ -> },
  onOpenShortcutsSettings: () -> Unit = {},
  onSelectExpression: (ExpressionState) -> Unit = {},
  onSelectIdleMode: (IdleDisplayMode) -> Unit = {},
  onSelectIdlePlacement: (IdlePlacement) -> Unit = {},
  onSwapSecondaryActivity: (ActivityType) -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val bgColor = DynaPalette.IslandBg
  val fgColor = DynaPalette.IslandTextPrimary
  val accentColor = DynaPalette.IslandTextPrimary
  val iconColor = DynaPalette.IslandTextSecondary
  val haptic = LocalHapticFeedback.current
  val strings = LocalDynaStrings.current

  if (isIslandHidden) {
    HiddenTopDot(
      bgColor = bgColor,
      accentColor = accentColor,
      restoreDescription = strings.pillRestoreIslandCd,
      onRestore = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onRestoreIsland()
      },
      modifier = modifier,
    )
    return
  }

  val isIdleMenuDockOpen =
    isExpanded &&
      activity.type == ActivityType.IDLE &&
      (quickMenuCategory == QuickMenuCategory.MENU || quickMenuCategory == QuickMenuCategory.NONE)

  val isCardExpanded = isExpanded && !isIdleMenuDockOpen

  val activeExpression =
    transientExpression
      ?: when {
        isIdleMenuDockOpen -> ExpressionState.CHEERFUL
        isCardExpanded && activity.type == ActivityType.IDLE ->
          when (quickMenuCategory) {
            QuickMenuCategory.MOOD_PICKER -> appearance.selectedExpression
            else -> ExpressionState.SMIRK
          }
        else -> activity.expressionOverride ?: appearance.selectedExpression
      }

  val collapsedWidth =
    when (activity.type) {
      ActivityType.IDLE ->
        when (appearance.idleDisplayMode) {
          IdleDisplayMode.CIRCLE_FACE -> appearance.heightDp.coerceIn(32f, 54f)
          IdleDisplayMode.PILL_CENTER_FACE -> appearance.widthDp.coerceIn(92f, maxWidthDp)
          IdleDisplayMode.PILL_LEFT_FACE_INFO -> appearance.widthDp.coerceIn(124f, maxWidthDp)
        }
      ActivityType.TIMER,
      ActivityType.STOPWATCH -> (appearance.widthDp + 36f).coerceIn(156f, maxWidthDp)
      ActivityType.NOTIFICATION,
      ActivityType.MEDIA,
      ActivityType.CALL,
      ActivityType.CHARGING,
      ActivityType.LOW_BATTERY,
      ActivityType.PRAYER_ALARM -> (appearance.widthDp + 56f).coerceIn(184f, maxWidthDp)
    }

  val targetWidthDp =
    when {
      isCardExpanded && activity.type == ActivityType.NOTIFICATION ->
        appearance.notificationWidthDp.coerceIn(240f, maxWidthDp)
      isCardExpanded && activity.type == ActivityType.IDLE ->
        appearance.menuWidthDp.coerceIn(250f, maxWidthDp)
      isCardExpanded -> maxWidthDp.coerceIn(260f, 344f)
      isIdleMenuDockOpen -> appearance.widthDp.coerceIn(138f, 210f)
      else -> collapsedWidth.coerceIn(32f, maxWidthDp)
    }

  val targetHeightDp =
    when {
      isCardExpanded ->
        when (activity.type) {
          ActivityType.IDLE ->
            when (quickMenuCategory) {
              QuickMenuCategory.CALENDAR -> appearance.menuHeightDp.coerceIn(210f, 290f)
              QuickMenuCategory.SHORTCUTS -> appearance.menuHeightDp.coerceIn(220f, 300f)
              QuickMenuCategory.MOOD_PICKER -> appearance.menuHeightDp.coerceIn(220f, 260f)
              QuickMenuCategory.PRAYER -> 154f
              QuickMenuCategory.WEATHER -> 146f
              QuickMenuCategory.MENU,
              QuickMenuCategory.NONE -> 40f
            }
          ActivityType.TIMER -> 132f
          ActivityType.STOPWATCH -> if (activity.lapTimesFormatted.isNotEmpty()) 128f else 98f
          ActivityType.NOTIFICATION -> {
            val baseNotifH =
              when {
                isReplying -> 178f
                activity.replyCapability != null -> 154f
                else -> 118f
              }
            baseNotifH.coerceAtMost(appearance.notificationMaxHeightDp.coerceIn(112f, 240f))
          }
          ActivityType.MEDIA -> 146f
          ActivityType.CALL -> 134f
          ActivityType.CHARGING,
          ActivityType.LOW_BATTERY,
          ActivityType.PRAYER_ALARM -> 96f
        }
      isIdleMenuDockOpen -> appearance.heightDp.coerceIn(34f, 44f)
      else -> appearance.heightDp.coerceIn(30f, 54f)
    }

  val targetRadiusDp =
    when {
      activity.type == ActivityType.IDLE &&
        !isExpanded &&
        appearance.idleDisplayMode == IdleDisplayMode.CIRCLE_FACE -> 999f
      isCardExpanded -> appearance.cornerRadiusDp.coerceIn(18f, 28f)
      appearance.idlePlacement == IdlePlacement.FIXED_TOP && activity.type == ActivityType.IDLE ->
        appearance.cornerRadiusDp.coerceIn(14f, 24f)
      else -> appearance.cornerRadiusDp.coerceIn(16f, 28f)
    }

  val speed = appearance.animationSpeedMultiplier.coerceIn(0.6f, 1.5f)
  val springSpec =
    if (appearance.animationEnabled) {
      spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = 0.86f,
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

  val targetAlpha =
    when {
      activity.type == ActivityType.NOTIFICATION && isCardExpanded ->
        appearance.notificationOpacity.coerceIn(0.70f, 1.0f)
      isDimmed && !isExpanded && appearance.autoDimEnabled ->
        appearance.dimmedOpacity.coerceIn(0.25f, 0.85f)
      else -> 1f
    }

  val animAlpha by
    animateFloatAsState(
      targetValue = targetAlpha,
      animationSpec = tween(durationMillis = if (appearance.animationEnabled) 260 else 0),
      label = "pill_alpha",
    )

  val islandDescription = stringResource(R.string.cd_dyna_island)

  Column(
    modifier = modifier.graphicsLayer { alpha = animAlpha },
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Surface(
        modifier =
          Modifier.testTag("dyna_island_pill")
            .semantics { contentDescription = islandDescription }
            .width(animWidth)
            .heightIn(min = animHeight)
            .clip(RoundedCornerShape(animRadius))
            .pointerInput(isExpanded, isReplying) {
              if (isExpanded && !isReplying) {
                detectVerticalDragGestures { _, dragAmount ->
                  if (dragAmount < -18f) {
                    onSwipeUpCollapse()
                  }
                }
              }
            }
            .combinedClickable(
              enabled = !isReplying,
              onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
              },
              onDoubleClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onDoubleClick()
              },
              onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onLongClick()
              },
            ),
        shape = RoundedCornerShape(animRadius),
        color = bgColor,
        border = BorderStroke(1.dp, DynaPalette.IslandBorder),
      ) {
        AnimatedContent(
          targetState = Triple(isCardExpanded, isIdleMenuDockOpen, activity to quickMenuCategory),
          contentKey = { (cardExp, dockOpen, pair) ->
            "${cardExp}_${dockOpen}_${pair.first.type}_${pair.first.id}_${pair.second}_${pair.first.isCallMuted}_${pair.first.mediaIsPlaying}"
          },
          transitionSpec = {
            val duration = if (appearance.animationEnabled) (180 / speed).toInt() else 0
            (fadeIn(tween(duration)) + slideInVertically(tween(duration)) { it / 8 })
              .togetherWith(fadeOut(tween((duration * 0.6f).toInt())))
          },
          label = "pill_content",
        ) { (cardExpanded, dockOpen, statePair) ->
          val state = statePair.first
          val category = statePair.second
          when {
            dockOpen ->
              CompactMenuOpenHeaderContent(
                expression = activeExpression,
                fgColor = fgColor,
                accentColor = accentColor,
                animationEnabled = appearance.animationEnabled,
              )
            !cardExpanded ->
              CollapsedPillContent(
                activity = state,
                appearance = appearance,
                expression = activeExpression,
                batteryState = batteryState,
                weather = weather,
                prayerSchedule = prayerSchedule,
                fgColor = fgColor,
                accentColor = accentColor,
                iconColor = iconColor,
              )
            else ->
              Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)
              ) {
                when (state.type) {
                  ActivityType.IDLE ->
                    IdleQuickCategoryPanel(
                      category = category,
                      appearance = appearance.copy(selectedExpression = activeExpression),
                      weather = weather,
                      prayerSchedule = prayerSchedule,
                      shortcutSlots = shortcutSlots,
                      installedApps = installedApps,
                      appIconProvider = appIconProvider,
                      language = language,
                      hideTopDotEnabled = hideTopDotEnabled,
                      fgColor = fgColor,
                      accentColor = accentColor,
                      onSelectCategory = onSelectQuickCategory,
                      onCollapse = onClick,
                      onHideIsland = onHideIsland,
                      onOpenCalendarDate = onOpenCalendarDate,
                      onRefreshWeather = onRefreshWeather,
                      onStartTimer = onStartTimer,
                      onStartStopwatch = onStartStopwatch,
                      onLaunchShortcut = onLaunchShortcut,
                      onAssignShortcutSlot = onAssignShortcutSlot,
                      onOpenShortcutsSettings = onOpenShortcutsSettings,
                      onSelectExpression = onSelectExpression,
                      onSelectIdleMode = onSelectIdleMode,
                      onSelectIdlePlacement = onSelectIdlePlacement,
                    )
                  ActivityType.NOTIFICATION ->
                    ExpandedNotificationContent(
                      activity = state,
                      fgColor = fgColor,
                      accentColor = accentColor,
                      iconColor = iconColor,
                      showNotificationPreview = showNotificationPreview,
                      isReplying = isReplying,
                      onCollapse = onClick,
                      onDismissNotification = onDismissNotification,
                      onMarkRead = onMarkNotificationRead,
                      onOpenApp = onOpenNotificationApp,
                      onStartReply = onStartReply,
                      onCancelReply = onCancelReply,
                      onSendReply = onSendReply,
                    )
                  ActivityType.TIMER ->
                    ExpandedTimerOrStopwatchContent(
                      activity = state,
                      isStopwatch = false,
                      fgColor = fgColor,
                      accentColor = accentColor,
                      iconColor = iconColor,
                      onTogglePause = onToggleTimerPause,
                      onLap = {},
                      onStop = onStopTimer,
                    )
                  ActivityType.STOPWATCH ->
                    ExpandedTimerOrStopwatchContent(
                      activity = state,
                      isStopwatch = true,
                      fgColor = fgColor,
                      accentColor = accentColor,
                      iconColor = iconColor,
                      onTogglePause = onToggleStopwatchPause,
                      onLap = onLapStopwatch,
                      onStop = onStopStopwatch,
                    )
                  ActivityType.MEDIA ->
                    ExpandedMediaContent(
                      activity = state,
                      fgColor = fgColor,
                      accentColor = accentColor,
                      iconColor = iconColor,
                      onPrevious = onMediaPrevious,
                      onPlayPause = onMediaPlayPause,
                      onNext = onMediaNext,
                      onToggleFavorite = onMediaFavorite,
                      onCollapse = onClick,
                    )
                  ActivityType.CALL ->
                    ExpandedCallContent(
                      activity = state,
                      fgColor = fgColor,
                      onAnswer = onCallAnswer,
                      onToggleMute = onCallToggleMute,
                      onToggleVideo = onCallToggleVideo,
                      onDecline = onCallDecline,
                      onDismiss = onDismissCall,
                    )
                  ActivityType.CHARGING -> {
                    val (icon, tint) = systemActivityIcon(ActivityType.CHARGING)
                    ExpandedSystemBannerContent(
                      activity = state,
                      icon = icon,
                      tintColor = tint,
                      fgColor = fgColor,
                      onDismiss = onDismissCharging,
                    )
                  }
                  ActivityType.LOW_BATTERY -> {
                    val (icon, tint) = systemActivityIcon(ActivityType.LOW_BATTERY)
                    ExpandedSystemBannerContent(
                      activity = state,
                      icon = icon,
                      tintColor = tint,
                      fgColor = fgColor,
                      onDismiss = onDismissLowBattery,
                    )
                  }
                  ActivityType.PRAYER_ALARM -> {
                    val (icon, tint) = systemActivityIcon(ActivityType.PRAYER_ALARM)
                    ExpandedSystemBannerContent(
                      activity = state,
                      icon = icon,
                      tintColor = tint,
                      fgColor = fgColor,
                      onDismiss = onDismissPrayerAlarm,
                    )
                  }
                }
              }
          }
        }
      }

      val badge = activity.secondaryBadge
      if (!isExpanded && badge != null) {
        val badgeIcon =
          when (badge.type) {
            ActivityType.MEDIA -> Icons.Filled.MusicNote
            ActivityType.TIMER,
            ActivityType.STOPWATCH -> Icons.Filled.Timer
            ActivityType.CHARGING -> Icons.Filled.BatteryChargingFull
            ActivityType.LOW_BATTERY -> Icons.Filled.BatteryAlert
            ActivityType.CALL -> Icons.Filled.Call
            ActivityType.PRAYER_ALARM -> Icons.Filled.Mosque
            else -> Icons.Filled.Notifications
          }
        Surface(
          modifier =
            Modifier.height(appearance.heightDp.coerceIn(30f, 44f).dp)
              .clip(CircleShape)
              .clickable { onSwapSecondaryActivity(badge.type) }
              .testTag("dyna_secondary_badge"),
          shape = CircleShape,
          color = bgColor,
          border = BorderStroke(1.dp, DynaPalette.IslandBorder),
        ) {
          Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = badgeIcon,
              contentDescription = badge.label,
              tint = fgColor,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }

    AnimatedVisibility(
      visible = isIdleMenuDockOpen,
      enter =
        if (appearance.animationEnabled) fadeIn(tween(150)) + expandVertically()
        else fadeIn(snap()),
      exit =
        if (appearance.animationEnabled) fadeOut(tween(110)) + shrinkVertically()
        else fadeOut(snap()),
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(6.dp))
        BelowIslandIconMenuDock(
          items = quickMenuItems,
          bgColor = bgColor,
          fgColor = fgColor,
          accentColor = accentColor,
          onSelectItem = { item ->
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            when {
              item.targetCategory != null -> onSelectQuickCategory(item.targetCategory)
              item == QuickMenuItem.TIMER_1M -> onStartTimer(60)
              item == QuickMenuItem.STOPWATCH -> onStartStopwatch()
              item == QuickMenuItem.HIDE_ISLAND -> onHideIsland()
            }
          },
        )
      }
    }
  }
}

@Composable
private fun HiddenTopDot(
  bgColor: Color,
  accentColor: Color,
  restoreDescription: String,
  onRestore: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .width(64.dp)
        .height(48.dp)
        .semantics { contentDescription = restoreDescription }
        .pointerInput(Unit) {
          var triggered = false
          var totalDragX = 0f
          var totalDragY = 0f
          detectDragGestures(
            onDragStart = {
              triggered = false
              totalDragX = 0f
              totalDragY = 0f
            },
            onDrag = { _, dragAmount ->
              totalDragX += dragAmount.x
              totalDragY += dragAmount.y
              if (!triggered && (abs(totalDragY) > 10f || abs(totalDragX) > 14f)) {
                triggered = true
                onRestore()
              }
            },
          )
        }
        .clickable(onClick = onRestore)
        .testTag("dyna_hidden_top_dot"),
    contentAlignment = Alignment.TopCenter,
  ) {
    Surface(
      modifier = Modifier.padding(top = 4.dp).width(34.dp).height(9.dp),
      shape = CircleShape,
      color = bgColor.copy(alpha = 0.85f),
      border = BorderStroke(1.dp, DynaPalette.IslandBorder),
    ) {
      Box(contentAlignment = Alignment.Center) {
        Box(
          modifier =
            Modifier.size(4.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.9f))
        )
      }
    }
  }
}

@Composable
private fun CompactMenuOpenHeaderContent(
  expression: ExpressionState,
  fgColor: Color,
  accentColor: Color,
  animationEnabled: Boolean,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    DynamicIslandFace(
      expression = expression,
      featureColor = fgColor,
      accentColor = accentColor,
      animationEnabled = animationEnabled,
      sizeDp = 22.dp,
    )
    Text(
      text = "MENU",
      style = DynaMonoTypography.LabelMono.copy(fontSize = 10.sp),
      color = DynaPalette.IslandTextSecondary,
    )
    Icon(
      imageVector = Icons.Filled.Close,
      contentDescription = "Tutup",
      tint = DynaPalette.IslandTextSecondary,
      modifier = Modifier.size(14.dp),
    )
  }
}

@Composable
private fun CollapsedPillContent(
  activity: ActivityState,
  appearance: IslandAppearance,
  expression: ExpressionState,
  batteryState: BatterySnapshot,
  weather: WeatherSnapshot,
  prayerSchedule: PrayerSchedule,
  fgColor: Color,
  accentColor: Color,
  iconColor: Color,
) {
  val horizontalPad =
    if (activity.type == ActivityType.IDLE && appearance.idleDisplayMode == IdleDisplayMode.CIRCLE_FACE) {
      4.dp
    } else {
      12.dp
    }

  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPad, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    when (activity.type) {
      ActivityType.IDLE -> {
        when (appearance.idleDisplayMode) {
          IdleDisplayMode.CIRCLE_FACE,
          IdleDisplayMode.PILL_CENTER_FACE -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              DynamicIslandFace(
                expression = expression,
                featureColor = fgColor,
                accentColor = accentColor,
                animationEnabled = appearance.animationEnabled,
                sizeDp = (appearance.heightDp * 0.64f).coerceIn(18f, 28f).dp,
              )
            }
          }
          IdleDisplayMode.PILL_LEFT_FACE_INFO -> {
            val sideInfo =
              weather.temperatureC?.let { "${it.roundToInt()}°C" }
                ?: prayerSchedule.nextPrayer?.let { "${it.name.label.take(3)} ${it.formattedTime}" }
                ?: "${batteryState.levelPercent}%"
            DynamicIslandFace(
              expression = expression,
              featureColor = fgColor,
              accentColor = accentColor,
              animationEnabled = appearance.animationEnabled,
              sizeDp = (appearance.heightDp * 0.64f).coerceIn(18f, 28f).dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = sideInfo,
              style = DynaMonoTypography.LabelMono.copy(fontSize = 11.sp),
              color = fgColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
      ActivityType.NOTIFICATION -> {
        Icon(
          imageVector =
            if (activity.replyCapability != null) Icons.AutoMirrored.Filled.Reply
            else Icons.Filled.Notifications,
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
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(fgColor))
      }
      ActivityType.TIMER,
      ActivityType.STOPWATCH -> {
        Icon(
          imageVector = Icons.Filled.Timer,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = activity.title,
          style = DynaMonoTypography.LabelMono,
          color = fgColor,
          maxLines = 1,
        )
      }
      ActivityType.MEDIA -> {
        val art = activity.mediaAlbumArt
        if (art != null) {
          Image(
            bitmap = art.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(18.dp).clip(RoundedCornerShape(5.dp)),
          )
        } else {
          Icon(
            imageVector = Icons.Filled.MusicNote,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(15.dp),
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = activity.title,
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
          color = fgColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
      }
      ActivityType.CALL -> {
        val indicatorColor =
          if (activity.isCallMuted) DynaPalette.IslandIndicatorMute
          else DynaPalette.IslandIndicatorLive
        Icon(
          imageVector = Icons.Filled.Call,
          contentDescription = null,
          tint = indicatorColor,
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
        Box(
          modifier =
            Modifier.size(7.dp)
              .clip(CircleShape)
              .background(indicatorColor)
              .testTag("pill_call_mic_indicator")
        )
      }
      ActivityType.CHARGING,
      ActivityType.LOW_BATTERY,
      ActivityType.PRAYER_ALARM -> {
        val (icon, tint) = systemActivityIcon(activity.type)
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
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
      }
    }
  }
}
