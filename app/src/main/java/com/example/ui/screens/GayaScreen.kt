package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ActivityType
import com.example.domain.model.IdleDisplayMode
import com.example.domain.model.IdlePlacement
import com.example.domain.model.IslandAppearance
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.ShortcutAppItem
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaIslandPill
import com.example.ui.components.DynaSecondaryButton
import com.example.ui.components.DynaSettingRow
import com.example.ui.components.DynaSwitch
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.LocalDynaStrings
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaUiState
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun GayaScreen(
  uiState: DynaUiState,
  onUpdateAppearance: ((IslandAppearance) -> IslandAppearance) -> Unit,
  onResetAppearance: () -> Unit,
  onTestNotification: () -> Unit,
  onTestChatNotification: () -> Unit,
  onTestCallAndMedia: () -> Unit,
  onToggleOverlayExpanded: () -> Unit,
  onSelectQuickCategory: (QuickMenuCategory) -> Unit,
  onHideIsland: () -> Unit,
  onRestoreIsland: () -> Unit,
  onStartTimer: (Int) -> Unit,
  onToggleTimerPause: () -> Unit,
  onStopTimer: () -> Unit,
  onStartStopwatch: () -> Unit,
  onToggleStopwatchPause: () -> Unit,
  onLapStopwatch: () -> Unit,
  onStopStopwatch: () -> Unit,
  onDismissNotification: () -> Unit,
  onStartReply: () -> Unit,
  onCancelReply: () -> Unit,
  onSendSimulatedReply: () -> Unit,
  onMediaPlayPause: () -> Unit,
  onMediaFavorite: () -> Unit,
  onCallToggleMute: () -> Unit,
  onCallToggleVideo: () -> Unit,
  onDismissCall: () -> Unit,
  onLaunchShortcut: (String) -> Unit,
  onOpenCalendarDate: (LocalDate) -> Unit,
  onRefreshWeather: () -> Unit,
  onSwapSecondaryActivity: (ActivityType) -> Unit,
  getAppIconBitmap: (String) -> Bitmap?,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  val appearance = uiState.settings.appearance
  val coroutineScope = rememberCoroutineScope()
  val pagerState = rememberPagerState(pageCount = { 3 })

  val orderedShortcuts =
    remember(
      uiState.settings.pinnedShortcutPackages,
      uiState.settings.shortcutSlots,
      uiState.installedApps,
    ) {
      uiState.settings.orderedShortcuts().mapNotNull { pkg ->
        uiState.installedApps.firstOrNull { it.packageName == pkg }
          ?: ShortcutAppItem(packageName = pkg, label = pkg.substringAfterLast('.'))
      }
    }

  val slideTitles =
    listOf(
      strings.styleSlideIdleTitle,
      strings.styleSlideNotifTitle,
      strings.styleSlideMenuTitle,
    )

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = tokens.bottomNavClearance),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    Text(
      text = strings.styleTitle,
      style = MaterialTheme.typography.headlineLarge,
      color = tokens.textPrimary,
      modifier = Modifier.testTag("gaya_title"),
    )

    // Top Live Interactive Preview Container
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Surface(
        modifier = Modifier.fillMaxWidth().testTag("gaya_preview_card"),
        shape = RoundedCornerShape(tokens.radiusXl),
        color = tokens.surfaceInset,
        border = BorderStroke(1.dp, tokens.borderHairline),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = strings.stylePreviewLabel,
            style = DynaMonoTypography.LabelMono,
            color = tokens.textMuted,
          )
          Spacer(modifier = Modifier.height(14.dp))
          Box(
            modifier = Modifier.fillMaxWidth().heightIn(min = 124.dp),
            contentAlignment = Alignment.TopCenter,
          ) {
            DynaIslandPill(
              activity = uiState.activity,
              appearance = appearance,
              isExpanded = uiState.isOverlayExpanded,
              showNotificationPreview = uiState.settings.showNotificationPreview,
              isReplying = uiState.isReplying,
              isDimmed = uiState.isDimmed,
              isIslandHidden = uiState.isIslandHidden,
              quickMenuCategory = uiState.quickMenuCategory,
              quickMenuItems = uiState.settings.enabledQuickMenuItems(),
              batteryState = uiState.batteryState,
              weather = uiState.weather,
              prayerSchedule = uiState.prayerSchedule,
              shortcutSlots = orderedShortcuts,
              installedApps = uiState.installedApps,
              appIconProvider = getAppIconBitmap,
              language = uiState.settings.language,
              hideTopDotEnabled = uiState.settings.hideTopDotEnabled,
              maxWidthDp = 312f,
              onClick = onToggleOverlayExpanded,
              onSwipeUpCollapse = onToggleOverlayExpanded,
              onHideIsland = onHideIsland,
              onRestoreIsland = onRestoreIsland,
              onToggleTimerPause = onToggleTimerPause,
              onStopTimer = onStopTimer,
              onStartTimer = onStartTimer,
              onStartStopwatch = onStartStopwatch,
              onToggleStopwatchPause = onToggleStopwatchPause,
              onLapStopwatch = onLapStopwatch,
              onStopStopwatch = onStopStopwatch,
              onDismissNotification = onDismissNotification,
              onMarkNotificationRead = onDismissNotification,
              onStartReply = onStartReply,
              onCancelReply = onCancelReply,
              onSendReply = { onSendSimulatedReply() },
              onMediaPlayPause = onMediaPlayPause,
              onMediaFavorite = onMediaFavorite,
              onCallToggleMute = onCallToggleMute,
              onCallToggleVideo = onCallToggleVideo,
              onCallDecline = onDismissCall,
              onDismissCall = onDismissCall,
              onSelectQuickCategory = onSelectQuickCategory,
              onOpenCalendarDate = onOpenCalendarDate,
              onRefreshWeather = onRefreshWeather,
              onLaunchShortcut = onLaunchShortcut,
              onSelectExpression = { exp ->
                onUpdateAppearance { it.copy(selectedExpression = exp) }
              },
              onSelectIdleMode = { mode ->
                onUpdateAppearance { it.copy(idleDisplayMode = mode) }
              },
              onSelectIdlePlacement = { placement ->
                onUpdateAppearance { it.copy(idlePlacement = placement) }
              },
              onSwapSecondaryActivity = onSwapSecondaryActivity,
            )
          }
        }
      }

      // 3 Quick Motion & Live Overlay Test Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        GayaTestPillButton(
          label = strings.styleTestNotif,
          onClick = onTestNotification,
          testTag = "gaya_test_notif",
          modifier = Modifier.weight(1f),
        )
        GayaTestPillButton(
          label = strings.styleTestChat,
          onClick = onTestChatNotification,
          testTag = "gaya_test_chat",
          modifier = Modifier.weight(1f),
        )
        GayaTestPillButton(
          label = strings.styleTestCallMedia,
          onClick = onTestCallAndMedia,
          testTag = "gaya_test_call_media",
          modifier = Modifier.weight(1f),
        )
      }
    }

    // Bottom 3-Slide Controls Pager
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        slideTitles.forEachIndexed { idx, title ->
          val selected = pagerState.currentPage == idx
          Surface(
            modifier =
              Modifier.weight(1f)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(tokens.radiusMd))
                .clickable { coroutineScope.launch { pagerState.animateScrollToPage(idx) } }
                .testTag("gaya_slide_tab_$idx"),
            shape = RoundedCornerShape(tokens.radiusMd),
            color = if (selected) tokens.accentPrimary else tokens.surfaceCard,
            border =
              BorderStroke(1.dp, if (selected) tokens.accentPrimary else tokens.borderHairline),
          ) {
            Box(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = if (selected) tokens.onAccentPrimary else tokens.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }

      HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth().testTag("gaya_controls_pager"),
      ) { page ->
        when (page) {
          0 ->
            GayaIdleSlide(
              uiState = uiState,
              appearance = appearance,
              onUpdateAppearance = onUpdateAppearance,
            )
          1 ->
            GayaNotificationSlide(
              appearance = appearance,
              onUpdateAppearance = onUpdateAppearance,
            )
          else ->
            GayaMenuSlide(
              appearance = appearance,
              onUpdateAppearance = onUpdateAppearance,
              onResetAppearance = onResetAppearance,
            )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        repeat(3) { idx ->
          val active = pagerState.currentPage == idx
          Box(
            modifier =
              Modifier.padding(horizontal = 4.dp)
                .size(if (active) 7.dp else 6.dp)
                .clip(CircleShape)
                .background(if (active) tokens.accentPrimary else tokens.borderStrong)
          )
        }
      }
    }
  }
}

@Composable
private fun GayaTestPillButton(
  label: String,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  Surface(
    modifier =
      modifier.heightIn(min = 48.dp).clip(CircleShape).clickable(onClick = onClick).testTag(testTag),
    shape = CircleShape,
    color = tokens.surfaceCard,
    border = BorderStroke(1.dp, tokens.borderStrong),
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
        color = tokens.textPrimary,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun GayaIdleSlide(
  uiState: DynaUiState,
  appearance: IslandAppearance,
  onUpdateAppearance: ((IslandAppearance) -> IslandAppearance) -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current

  BentoCard(headerLabel = strings.styleSlideIdleTitle) {
    Text(
      text = strings.styleIdleLayoutLabel,
      style = MaterialTheme.typography.bodySmall,
      color = tokens.textSecondary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      IdleDisplayMode.entries.forEach { mode ->
        val selected = appearance.idleDisplayMode == mode
        Surface(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(tokens.radiusMd))
              .clickable { onUpdateAppearance { it.copy(idleDisplayMode = mode) } }
              .testTag("gaya_idle_mode_${mode.name}"),
          shape = RoundedCornerShape(tokens.radiusMd),
          color = if (selected) tokens.accentPrimary else tokens.surfaceRaised,
          border =
            BorderStroke(1.dp, if (selected) tokens.accentPrimary else tokens.borderHairline),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = mode.localizedLabel(uiState.settings.language),
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
              color = if (selected) tokens.onAccentPrimary else tokens.textPrimary,
            )
            Box(
              modifier =
                Modifier.size(8.dp)
                  .clip(CircleShape)
                  .background(if (selected) tokens.onAccentPrimary else tokens.borderStrong)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    HorizontalDivider(color = tokens.borderHairline)
    Spacer(modifier = Modifier.height(12.dp))

    GayaSliderControl(
      label = strings.styleWidthLabel,
      valueText = "${appearance.widthDp.roundToInt()} dp",
      value = appearance.widthDp,
      valueRange = 92f..220f,
      onValueChange = { v -> onUpdateAppearance { it.copy(widthDp = v) } },
      testTag = "gaya_slider_idle_width",
    )
    GayaSliderControl(
      label = strings.styleHeightLabel,
      valueText = "${appearance.heightDp.roundToInt()} dp",
      value = appearance.heightDp,
      valueRange = 30f..54f,
      onValueChange = { v -> onUpdateAppearance { it.copy(heightDp = v) } },
      testTag = "gaya_slider_idle_height",
    )
    GayaSliderControl(
      label = strings.styleTopOffsetLabel,
      valueText = "${appearance.topOffsetDp.roundToInt()} dp",
      value = appearance.topOffsetDp,
      valueRange = 0f..48f,
      onValueChange = { v -> onUpdateAppearance { it.copy(topOffsetDp = v) } },
      testTag = "gaya_slider_top_offset",
    )
    GayaSliderControl(
      label = strings.styleHorizontalOffsetLabel,
      valueText = "${appearance.horizontalOffsetDp.roundToInt()} dp",
      value = appearance.horizontalOffsetDp,
      valueRange = -64f..64f,
      onValueChange = { v -> onUpdateAppearance { it.copy(horizontalOffsetDp = v) } },
      testTag = "gaya_slider_horizontal_offset",
    )

    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = strings.stylePlacementLabel,
      style = MaterialTheme.typography.bodySmall,
      color = tokens.textSecondary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      val placements =
        listOf(
          IdlePlacement.FLOATING to strings.stylePlacementFloating,
          IdlePlacement.FIXED_TOP to strings.stylePlacementFixed,
        )
      placements.forEach { (placement, label) ->
        val selected = appearance.idlePlacement == placement
        Surface(
          modifier =
            Modifier.weight(1f)
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(tokens.radiusMd))
              .clickable { onUpdateAppearance { it.copy(idlePlacement = placement) } }
              .testTag("gaya_placement_${placement.name}"),
          shape = RoundedCornerShape(tokens.radiusMd),
          color = if (selected) tokens.accentPrimary else tokens.surfaceRaised,
          border =
            BorderStroke(1.dp, if (selected) tokens.accentPrimary else tokens.borderHairline),
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
              text = label,
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = if (selected) tokens.onAccentPrimary else tokens.textPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))
    DynaSettingRow(
      title = strings.styleAutoDimLabel,
      showDivider = appearance.autoDimEnabled,
    ) {
      DynaSwitch(
        checked = appearance.autoDimEnabled,
        onCheckedChange = { enabled -> onUpdateAppearance { it.copy(autoDimEnabled = enabled) } },
        testTag = "gaya_auto_dim_switch",
      )
    }
    if (appearance.autoDimEnabled) {
      Spacer(modifier = Modifier.height(6.dp))
      GayaSliderControl(
        label = strings.styleAutoDimDelayLabel,
        valueText = "${appearance.autoDimDelaySeconds} s",
        value = appearance.autoDimDelaySeconds.toFloat(),
        valueRange = 2f..15f,
        onValueChange = { v -> onUpdateAppearance { it.copy(autoDimDelaySeconds = v.roundToInt()) } },
        testTag = "gaya_slider_dim_delay",
      )
    }
  }
}

@Composable
private fun GayaNotificationSlide(
  appearance: IslandAppearance,
  onUpdateAppearance: ((IslandAppearance) -> IslandAppearance) -> Unit,
) {
  val strings = LocalDynaStrings.current
  BentoCard(headerLabel = strings.styleSlideNotifTitle) {
    GayaSliderControl(
      label = strings.styleNotifWidthLabel,
      valueText = "${appearance.notificationWidthDp.roundToInt()} dp",
      value = appearance.notificationWidthDp,
      valueRange = 240f..344f,
      onValueChange = { v -> onUpdateAppearance { it.copy(notificationWidthDp = v) } },
      testTag = "gaya_slider_notif_width",
    )
    GayaSliderControl(
      label = strings.styleNotifMaxHeightLabel,
      valueText = "${appearance.notificationMaxHeightDp.roundToInt()} dp",
      value = appearance.notificationMaxHeightDp,
      valueRange = 112f..240f,
      onValueChange = { v -> onUpdateAppearance { it.copy(notificationMaxHeightDp = v) } },
      testTag = "gaya_slider_notif_height",
    )
    GayaSliderControl(
      label = strings.styleNotifOpacityLabel,
      valueText = "${(appearance.notificationOpacity * 100).roundToInt()}%",
      value = appearance.notificationOpacity,
      valueRange = 0.70f..1.00f,
      onValueChange = { v -> onUpdateAppearance { it.copy(notificationOpacity = v) } },
      testTag = "gaya_slider_notif_opacity",
    )
    GayaSliderControl(
      label = strings.styleAutoCollapseLabel,
      valueText = "${appearance.autoCollapseSeconds} s",
      value = appearance.autoCollapseSeconds.toFloat(),
      valueRange = 3f..15f,
      onValueChange = { v -> onUpdateAppearance { it.copy(autoCollapseSeconds = v.roundToInt()) } },
      testTag = "gaya_slider_auto_collapse",
    )
  }
}

@Composable
private fun GayaMenuSlide(
  appearance: IslandAppearance,
  onUpdateAppearance: ((IslandAppearance) -> IslandAppearance) -> Unit,
  onResetAppearance: () -> Unit,
) {
  val strings = LocalDynaStrings.current
  BentoCard(headerLabel = strings.styleSlideMenuTitle) {
    GayaSliderControl(
      label = strings.styleMenuWidthLabel,
      valueText = "${appearance.menuWidthDp.roundToInt()} dp",
      value = appearance.menuWidthDp,
      valueRange = 250f..344f,
      onValueChange = { v -> onUpdateAppearance { it.copy(menuWidthDp = v) } },
      testTag = "gaya_slider_menu_width",
    )
    GayaSliderControl(
      label = strings.styleMenuHeightLabel,
      valueText = "${appearance.menuHeightDp.roundToInt()} dp",
      value = appearance.menuHeightDp,
      valueRange = 210f..300f,
      onValueChange = { v -> onUpdateAppearance { it.copy(menuHeightDp = v) } },
      testTag = "gaya_slider_menu_height",
    )
    Spacer(modifier = Modifier.height(10.dp))
    DynaSecondaryButton(
      text = strings.styleResetGeometry,
      onClick = onResetAppearance,
      icon = Icons.Outlined.RestartAlt,
      testTag = "gaya_reset_dimensions",
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun GayaSliderControl(
  label: String,
  valueText: String,
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
  testTag: String,
) {
  val tokens = LocalDynaTokens.current
  Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = tokens.textPrimary,
      )
      Text(
        text = valueText,
        style = DynaMonoTypography.LabelMono,
        color = tokens.textSecondary,
      )
    }
    Slider(
      value = value.coerceIn(valueRange.start, valueRange.endInclusive),
      onValueChange = onValueChange,
      valueRange = valueRange,
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      colors =
        SliderDefaults.colors(
          thumbColor = tokens.accentPrimary,
          activeTrackColor = tokens.accentPrimary,
          inactiveTrackColor = tokens.surfaceRaised,
        ),
    )
  }
}
