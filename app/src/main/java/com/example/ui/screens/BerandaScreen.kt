package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaSettingRow
import com.example.ui.components.DynaStatusBadge
import com.example.ui.components.DynaSwitch
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaUiState

@Composable
fun BerandaScreen(
  uiState: DynaUiState,
  onOverlayEnabledChange: (Boolean) -> Unit,
  onOpenOverlaySettings: () -> Unit,
  onOpenNotificationSettings: () -> Unit,
  onRefreshPermissions: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val scrollState = rememberScrollState()
  val hasOverlayPerm = uiState.permissions.hasOverlayPermission

  val postNotificationsLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestPermission(),
      onResult = {
        onRefreshPermissions()
        onOverlayEnabledChange(true)
      },
    )

  fun handleOverlayToggle(nextEnabled: Boolean) {
    if (nextEnabled && !hasOverlayPerm) {
      onOverlayEnabledChange(true)
      return
    }
    if (
      nextEnabled &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        !uiState.permissions.hasPostNotificationsPermission
    ) {
      postNotificationsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      return
    }
    onOverlayEnabledChange(nextEnabled)
  }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    BentoCard(
      headerLabel = stringResource(R.string.beranda_overlay_section),
      headerAction = {
        DynaStatusBadge(
          text =
            when {
              !hasOverlayPerm -> "IZIN BELUM AKTIF"
              uiState.isOverlayAttached -> "AKTIF DI LAYAR"
              else -> "NONAKTIF"
            },
          dotColor =
            when {
              !hasOverlayPerm -> tokens.textMuted
              uiState.isOverlayAttached -> tokens.statusOnline
              else -> tokens.textSecondary
            },
          borderColor =
            if (uiState.isOverlayAttached) tokens.statusOnline.copy(alpha = 0.45f)
            else tokens.surfaceBorder,
        )
      },
      highlighted = uiState.isOverlayAttached,
    ) {
      DynaSettingRow(
        title = stringResource(R.string.beranda_overlay_title),
        description =
          if (hasOverlayPerm) stringResource(R.string.beranda_overlay_desc_ready)
          else stringResource(R.string.beranda_overlay_desc_need_perm),
        showDivider = false,
        control = {
          DynaSwitch(
            checked = uiState.settings.isOverlayEnabled && hasOverlayPerm,
            onCheckedChange = ::handleOverlayToggle,
            testTag = "overlay_master_switch",
          )
        },
      )
    }

    BentoCard(headerLabel = stringResource(R.string.beranda_permissions_section)) {
      CompactPermissionRow(
        title = stringResource(R.string.perm_overlay_title),
        description = stringResource(R.string.perm_overlay_desc),
        granted = hasOverlayPerm,
        onOpenSettings = onOpenOverlaySettings,
        testTag = "open_overlay_settings_button",
      )
      HorizontalDivider(color = tokens.surfaceBorder, modifier = Modifier.padding(vertical = 12.dp))
      CompactPermissionRow(
        title = stringResource(R.string.perm_notif_title),
        description = stringResource(R.string.perm_notif_desc),
        granted = uiState.permissions.hasNotificationAccess,
        onOpenSettings = onOpenNotificationSettings,
        testTag = "open_notification_settings_button",
      )
    }
  }
}

@Composable
private fun CompactPermissionRow(
  title: String,
  description: String,
  granted: Boolean,
  onOpenSettings: () -> Unit,
  testTag: String,
) {
  val tokens = LocalDynaTokens.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = if (granted) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
          contentDescription = null,
          tint = if (granted) tokens.statusOnline else tokens.textMuted,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
          color = tokens.textPrimary,
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.textSecondary,
      )
    }

    OutlinedButton(
      onClick = onOpenSettings,
      modifier = Modifier.heightIn(min = 48.dp).testTag(testTag),
      shape = RoundedCornerShape(tokens.controlRadius),
      border = androidx.compose.foundation.BorderStroke(1.dp, tokens.surfaceBorder),
      colors =
        ButtonDefaults.outlinedButtonColors(
          containerColor = tokens.surfaceRaised,
          contentColor = if (granted) tokens.statusOnline else tokens.accentCyan,
        ),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    ) {
      Text(
        text =
          if (granted) stringResource(R.string.perm_granted)
          else stringResource(R.string.action_open_settings),
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
      )
    }
  }
}
