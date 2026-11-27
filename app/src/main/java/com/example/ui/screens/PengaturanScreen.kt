package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaConfirmationDialog
import com.example.ui.components.DynaInfoCallout
import com.example.ui.components.DynaSecondaryButton
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaUiState

@Composable
fun PengaturanScreen(
  uiState: DynaUiState,
  onResetAllSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val scrollState = rememberScrollState()
  var showResetDialog by remember { mutableStateOf(false) }

  if (showResetDialog) {
    DynaConfirmationDialog(
      title = stringResource(R.string.pengaturan_reset_confirm_title),
      body = stringResource(R.string.pengaturan_reset_confirm_body),
      confirmLabel = stringResource(R.string.pengaturan_confirm),
      cancelLabel = stringResource(R.string.pengaturan_cancel),
      onConfirm = {
        showResetDialog = false
        onResetAllSettings()
      },
      onDismiss = { showResetDialog = false },
    )
  }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(scrollState)
        .padding(horizontal = 20.dp, vertical = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    BentoCard(headerLabel = stringResource(R.string.pengaturan_general_section)) {
      Text(
        text = stringResource(R.string.pengaturan_reset_title),
        style = MaterialTheme.typography.titleMedium,
        color = tokens.textPrimary,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.pengaturan_reset_desc),
        style = MaterialTheme.typography.bodySmall,
        color = tokens.textSecondary,
      )
      Spacer(modifier = Modifier.height(12.dp))
      DynaSecondaryButton(
        text = stringResource(R.string.pengaturan_reset_title),
        onClick = { showResetDialog = true },
        icon = Icons.Outlined.RestartAlt,
        testTag = "reset_all_settings_button",
        modifier = Modifier.fillMaxWidth(),
      )
    }

    BentoCard(headerLabel = stringResource(R.string.pengaturan_limits_section)) {
      DynaInfoCallout(text = stringResource(R.string.pengaturan_limits_body))
    }

    BentoCard(headerLabel = stringResource(R.string.pengaturan_install_guide_section)) {
      DynaInfoCallout(text = stringResource(R.string.pengaturan_install_guide_body))
    }

    BentoCard(headerLabel = stringResource(R.string.pengaturan_about_section)) {
      DiagnosticRow(key = "VERSI", value = stringResource(R.string.pengaturan_version_label))
      HorizontalDivider(color = tokens.surfaceBorder, modifier = Modifier.padding(vertical = 8.dp))
      DiagnosticRow(
        key = "OVERLAY",
        value = if (uiState.isOverlayAttached) "AKTIF" else "NONAKTIF",
      )
      HorizontalDivider(color = tokens.surfaceBorder, modifier = Modifier.padding(vertical = 8.dp))
      DiagnosticRow(
        key = "TEMA",
        value = uiState.settings.appearance.themePreset.displayName,
      )
    }
  }
}

@Composable
private fun DiagnosticRow(key: String, value: String) {
  val tokens = LocalDynaTokens.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = key, style = DynaMonoTypography.LabelMonoCaps, color = tokens.textMuted)
    Text(text = value, style = DynaMonoTypography.LabelCode, color = tokens.textSecondary)
  }
}
