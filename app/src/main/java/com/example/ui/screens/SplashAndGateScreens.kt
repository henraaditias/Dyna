package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.domain.model.PermissionSnapshot
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaLogoMark
import com.example.ui.components.DynaPrimaryButton
import com.example.ui.components.PermissionStatusRow
import com.example.ui.theme.LocalDynaStrings
import com.example.ui.theme.LocalDynaTokens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  permissions: PermissionSnapshot,
  onSplashCompleted: (hasRequiredPermissions: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  var visible by remember { mutableStateOf(false) }
  val alpha by
    animateFloatAsState(
      targetValue = if (visible) 1f else 0f,
      animationSpec = tween(durationMillis = 360),
      label = "splash_alpha",
    )

  LaunchedEffect(Unit) {
    visible = true
    delay(750L)
    onSplashCompleted(permissions.hasOverlayPermission && permissions.hasNotificationAccess)
  }

  Box(
    modifier = modifier.fillMaxSize().padding(24.dp).testTag("splash_screen"),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier = Modifier.graphicsLayer { this.alpha = alpha },
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      DynaLogoMark(size = 68.dp)
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        text = strings.appName,
        style = MaterialTheme.typography.displayLarge,
        color = tokens.textPrimary,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = strings.splashTagline,
        style = MaterialTheme.typography.bodyMedium,
        color = tokens.textSecondary,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
fun PermissionGateScreen(
  permissions: PermissionSnapshot,
  onOpenOverlaySettings: () -> Unit,
  onOpenNotificationSettings: () -> Unit,
  onContinue: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  val canContinue = permissions.hasOverlayPermission && permissions.hasNotificationAccess

  Box(
    modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier.widthIn(max = 520.dp)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .testTag("permission_gate_screen"),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DynaLogoMark(size = 44.dp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = strings.gateTitle,
          style = MaterialTheme.typography.headlineLarge,
          color = tokens.textPrimary,
        )
        Text(
          text = strings.gateSubtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = tokens.textSecondary,
        )
      }

      BentoCard {
        PermissionStatusRow(
          title = strings.gateOverlayTitle,
          description = strings.gateOverlayDesc,
          granted = permissions.hasOverlayPermission,
          onOpenSettings = onOpenOverlaySettings,
          testTag = "gate_overlay_settings_button",
        )
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = tokens.borderHairline, thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))
        PermissionStatusRow(
          title = strings.gateNotifTitle,
          description = strings.gateNotifDesc,
          granted = permissions.hasNotificationAccess,
          onOpenSettings = onOpenNotificationSettings,
          testTag = "gate_notif_settings_button",
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DynaPrimaryButton(
          text = strings.gateContinue,
          onClick = onContinue,
          enabled = canContinue,
          testTag = "gate_continue_button",
          modifier = Modifier.fillMaxWidth(),
        )
        if (!canContinue) {
          Text(
            text = strings.gateNeedBothHint,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
