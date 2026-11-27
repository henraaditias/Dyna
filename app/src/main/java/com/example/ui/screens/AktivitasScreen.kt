package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.ActivityType
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaInfoCallout
import com.example.ui.components.DynaPrimaryButton
import com.example.ui.components.DynaSecondaryButton
import com.example.ui.components.DynaSettingRow
import com.example.ui.components.DynaStatusBadge
import com.example.ui.components.DynaSwitch
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaUiState

private enum class AktivitasTab {
  TIMER,
  NOTIFIKASI,
}

@Composable
fun AktivitasScreen(
  uiState: DynaUiState,
  onSetTimerActivityEnabled: (Boolean) -> Unit,
  onStartTimer: (Int) -> Unit,
  onToggleTimerPause: () -> Unit,
  onStopTimer: () -> Unit,
  onOpenNotificationSettings: () -> Unit,
  onSetNotificationActivityEnabled: (Boolean) -> Unit,
  onSetShowNotificationPreview: (Boolean) -> Unit,
  onSetAutoExpandOnNotification: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val scrollState = rememberScrollState()
  var activeTab by rememberSaveable { mutableStateOf(AktivitasTab.TIMER) }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      KapsulSegmentButton(
        label = stringResource(R.string.aktivitas_seg_timer),
        selected = activeTab == AktivitasTab.TIMER,
        onClick = { activeTab = AktivitasTab.TIMER },
        modifier = Modifier.weight(1f).testTag("aktivitas_tab_timer"),
      )
      KapsulSegmentButton(
        label = stringResource(R.string.aktivitas_seg_notifikasi),
        selected = activeTab == AktivitasTab.NOTIFIKASI,
        onClick = { activeTab = AktivitasTab.NOTIFIKASI },
        modifier = Modifier.weight(1f).testTag("aktivitas_tab_notifikasi"),
      )
    }

    when (activeTab) {
      AktivitasTab.TIMER ->
        TimerSection(
          uiState = uiState,
          onSetTimerActivityEnabled = onSetTimerActivityEnabled,
          onStartTimer = onStartTimer,
          onToggleTimerPause = onToggleTimerPause,
          onStopTimer = onStopTimer,
        )
      AktivitasTab.NOTIFIKASI ->
        NotifikasiSection(
          uiState = uiState,
          onOpenNotificationSettings = onOpenNotificationSettings,
          onSetNotificationActivityEnabled = onSetNotificationActivityEnabled,
          onSetShowNotificationPreview = onSetShowNotificationPreview,
          onSetAutoExpandOnNotification = onSetAutoExpandOnNotification,
        )
    }
  }
}

@Composable
private fun TimerSection(
  uiState: DynaUiState,
  onSetTimerActivityEnabled: (Boolean) -> Unit,
  onStartTimer: (Int) -> Unit,
  onToggleTimerPause: () -> Unit,
  onStopTimer: () -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val isTimerRunning = uiState.activity.type == ActivityType.TIMER

  BentoCard(
    headerAction = {
      DynaStatusBadge(
        text =
          when {
            !isTimerRunning -> "SIAGA"
            uiState.activity.isFinished -> "SELESAI"
            uiState.activity.isPaused -> "DIJEDA"
            else -> "BERJALAN"
          },
        dotColor =
          if (isTimerRunning && !uiState.activity.isPaused) tokens.accentCyan else tokens.textMuted,
      )
    }
  ) {
    DynaSettingRow(
      title = stringResource(R.string.aktivitas_timer_enable),
      description = stringResource(R.string.aktivitas_timer_enable_desc),
      control = {
        DynaSwitch(
          checked = uiState.settings.timerActivityEnabled,
          onCheckedChange = onSetTimerActivityEnabled,
          testTag = "switch_timer_activity",
        )
      },
    )

    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text =
        if (isTimerRunning) uiState.activity.title
        else stringResource(R.string.aktivitas_timer_idle),
      style =
        if (isTimerRunning) DynaMonoTypography.TabularTimer else MaterialTheme.typography.bodyLarge,
      color = if (isTimerRunning) tokens.textPrimary else tokens.textSecondary,
    )
    if (isTimerRunning) {
      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { uiState.activity.progress ?: 0f },
        modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
        color = tokens.accentCyan,
        trackColor = tokens.surfaceRaised,
      )
    }

    Spacer(modifier = Modifier.height(14.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      if (!isTimerRunning) {
        DynaPrimaryButton(
          text = stringResource(R.string.aktivitas_start_1m),
          onClick = { onStartTimer(60) },
          enabled = uiState.settings.timerActivityEnabled,
          icon = Icons.Filled.PlayArrow,
          testTag = "start_timer_1m",
          modifier = Modifier.weight(1f),
        )
        DynaSecondaryButton(
          text = stringResource(R.string.aktivitas_start_5m),
          onClick = { onStartTimer(300) },
          enabled = uiState.settings.timerActivityEnabled,
          icon = Icons.Filled.PlayArrow,
          testTag = "start_timer_5m",
          modifier = Modifier.weight(1f),
        )
      } else {
        DynaPrimaryButton(
          text =
            if (uiState.activity.isPaused) stringResource(R.string.aktivitas_resume)
            else stringResource(R.string.aktivitas_pause),
          onClick = onToggleTimerPause,
          icon = if (uiState.activity.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
          testTag = "toggle_timer_pause",
          modifier = Modifier.weight(1f),
        )
        DynaSecondaryButton(
          text = stringResource(R.string.aktivitas_stop),
          onClick = onStopTimer,
          icon = Icons.Filled.Stop,
          testTag = "stop_timer_button",
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun NotifikasiSection(
  uiState: DynaUiState,
  onOpenNotificationSettings: () -> Unit,
  onSetNotificationActivityEnabled: (Boolean) -> Unit,
  onSetShowNotificationPreview: (Boolean) -> Unit,
  onSetAutoExpandOnNotification: (Boolean) -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val hasAccess = uiState.permissions.hasNotificationAccess

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    DynaInfoCallout(text = stringResource(R.string.notif_privacy_banner))

    BentoCard(
      headerAction = {
        DynaStatusBadge(
          text = if (hasAccess) "AKSES AKTIF" else "AKSES MATI",
          dotColor = if (hasAccess) tokens.statusOnline else tokens.textMuted,
        )
      }
    ) {
      if (!hasAccess) {
        DynaInfoCallout(text = stringResource(R.string.notif_locked_notice))
        Spacer(modifier = Modifier.height(10.dp))
        DynaSecondaryButton(
          text = stringResource(R.string.action_open_settings),
          onClick = onOpenNotificationSettings,
          testTag = "notif_screen_open_settings",
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(12.dp))
      }

      DynaSettingRow(
        title = stringResource(R.string.notif_enable_feature),
        description = stringResource(R.string.notif_enable_feature_desc),
        enabled = hasAccess,
        control = {
          DynaSwitch(
            checked = uiState.settings.notificationActivityEnabled,
            onCheckedChange = onSetNotificationActivityEnabled,
            enabled = hasAccess,
            testTag = "switch_notif_feature",
          )
        },
      )

      DynaSettingRow(
        title = stringResource(R.string.notif_show_preview),
        description = stringResource(R.string.notif_show_preview_desc),
        enabled = hasAccess,
        control = {
          DynaSwitch(
            checked = uiState.settings.showNotificationPreview,
            onCheckedChange = onSetShowNotificationPreview,
            enabled = hasAccess,
            testTag = "switch_notif_preview",
          )
        },
      )

      DynaSettingRow(
        title = stringResource(R.string.notif_auto_expand),
        description = stringResource(R.string.notif_auto_expand_desc),
        enabled = hasAccess,
        showDivider = false,
        control = {
          DynaSwitch(
            checked = uiState.settings.autoExpandOnNotification,
            onCheckedChange = onSetAutoExpandOnNotification,
            enabled = hasAccess,
            testTag = "switch_notif_auto_expand",
          )
        },
      )
    }
  }
}
