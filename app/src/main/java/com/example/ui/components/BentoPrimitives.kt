package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.LocalDynaStrings
import com.example.ui.theme.LocalDynaTokens

@Composable
fun DynaCanvasBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  val tokens = LocalDynaTokens.current
  Box(modifier = modifier.fillMaxSize().background(tokens.canvasBg)) { content() }
}

@Composable
fun BentoCanvasBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  DynaCanvasBackground(modifier = modifier, content = content)
}

@Composable
fun DynaLogoMark(
  modifier: Modifier = Modifier,
  size: Dp = 56.dp,
) {
  val tokens = LocalDynaTokens.current
  Canvas(modifier = modifier.size(size)) {
    val w = this.size.width
    val h = this.size.height
    val pillW = w
    val pillH = h * 0.56f
    val topY = (h - pillH) / 2f
    drawRoundRect(
      color = tokens.accentPrimary,
      topLeft = Offset(0f, topY),
      size = Size(pillW, pillH),
      cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
    )
    // Inner capsule eye/dot motif
    val eyeR = pillH * 0.16f
    drawCircle(
      color = tokens.onAccentPrimary,
      radius = eyeR,
      center = Offset(pillW * 0.34f, h / 2f),
    )
    drawCircle(
      color = tokens.onAccentPrimary,
      radius = eyeR,
      center = Offset(pillW * 0.52f, h / 2f),
    )
    drawRoundRect(
      color = tokens.onAccentPrimary,
      topLeft = Offset(pillW * 0.65f, h / 2f - eyeR * 0.85f),
      size = Size(pillW * 0.16f, eyeR * 1.7f),
      cornerRadius = CornerRadius(eyeR, eyeR),
    )
  }
}

@Composable
fun BentoCard(
  modifier: Modifier = Modifier,
  headerLabel: String? = null,
  headerAction: (@Composable RowScope.() -> Unit)? = null,
  highlighted: Boolean = false,
  cornerRadius: Dp? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val borderColor = if (highlighted) tokens.borderStrong else tokens.borderHairline
  val radius = cornerRadius ?: tokens.radiusLg
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(radius),
    color = tokens.surfaceCard,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Column(modifier = Modifier.padding(tokens.spaceMd)) {
      if (headerLabel != null || headerAction != null) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          if (headerLabel != null) {
            Text(
              text = headerLabel,
              style = MaterialTheme.typography.titleMedium,
              color = tokens.textPrimary,
            )
          }
          if (headerAction != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = headerAction)
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
      }
      content()
    }
  }
}

@Composable
fun DynaStatusBadge(
  text: String,
  dotColor: Color,
  modifier: Modifier = Modifier,
  borderColor: Color = LocalDynaTokens.current.borderHairline,
) {
  val tokens = LocalDynaTokens.current
  Row(
    modifier =
      modifier
        .clip(RoundedCornerShape(tokens.radiusSm))
        .background(tokens.surfaceRaised)
        .border(1.dp, borderColor, RoundedCornerShape(tokens.radiusSm))
        .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = text,
      style = DynaMonoTypography.LabelMono,
      color = tokens.textPrimary,
    )
  }
}

@Composable
fun DynaSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  testTag: String? = null,
) {
  val tokens = LocalDynaTokens.current
  val trackColor by
    animateColorAsState(
      targetValue =
        when {
          !enabled -> tokens.surfaceInset
          checked -> tokens.accentPrimary
          else -> tokens.surfaceRaised
        },
      label = "switch_track",
    )
  val borderColor by
    animateColorAsState(
      targetValue =
        when {
          !enabled -> tokens.borderHairline
          checked -> tokens.accentPrimary
          else -> tokens.borderStrong
        },
      label = "switch_border",
    )
  val thumbColor by
    animateColorAsState(
      targetValue =
        when {
          !enabled -> tokens.textMuted
          checked -> tokens.onAccentPrimary
          else -> tokens.textSecondary
        },
      label = "switch_thumb",
    )
  val thumbOffset by animateDpAsState(targetValue = if (checked) 22.dp else 3.dp, label = "thumb")

  Box(
    modifier =
      modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .minimumInteractiveComponentSize()
        .semantics { stateDescription = if (checked) "Aktif" else "Nonaktif" }
        .clickable(
          enabled = enabled,
          role = Role.Switch,
          onClick = { onCheckedChange(!checked) },
        ),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier.width(46.dp)
          .height(26.dp)
          .clip(CircleShape)
          .background(trackColor)
          .border(1.dp, borderColor, CircleShape),
      contentAlignment = Alignment.CenterStart,
    ) {
      Box(
        modifier =
          Modifier.offset(x = thumbOffset)
            .size(18.dp)
            .clip(CircleShape)
            .background(thumbColor)
      )
    }
  }
}

@Composable
fun DynaPrimaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  testTag: String? = null,
) {
  val tokens = LocalDynaTokens.current
  Button(
    onClick = onClick,
    enabled = enabled,
    modifier =
      modifier.heightIn(min = 48.dp).then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    shape = RoundedCornerShape(tokens.radiusMd),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = tokens.accentPrimary,
        contentColor = tokens.onAccentPrimary,
        disabledContainerColor = tokens.surfaceRaised,
        disabledContentColor = tokens.textMuted,
      ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    )
  }
}

@Composable
fun DynaSecondaryButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null,
  testTag: String? = null,
) {
  val tokens = LocalDynaTokens.current
  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier =
      modifier.heightIn(min = 48.dp).then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
    shape = RoundedCornerShape(tokens.radiusMd),
    border = BorderStroke(1.dp, if (enabled) tokens.borderStrong else tokens.borderHairline),
    colors =
      ButtonDefaults.outlinedButtonColors(
        containerColor = tokens.surfaceRaised,
        contentColor = tokens.textPrimary,
        disabledContainerColor = tokens.surfaceCard,
        disabledContentColor = tokens.textMuted,
      ),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
  ) {
    if (icon != null) {
      Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
    )
  }
}

@Composable
fun PermissionStatusRow(
  title: String,
  description: String,
  granted: Boolean,
  onOpenSettings: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.Top,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = if (granted) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = if (granted) tokens.statusActive else tokens.textSecondary,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = tokens.textPrimary,
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.textSecondary,
        )
      }
      DynaStatusBadge(
        text = if (granted) strings.gateGranted else strings.gatePending,
        dotColor = if (granted) tokens.statusActive else tokens.statusMuted,
        borderColor =
          if (granted) tokens.statusActive.copy(alpha = 0.45f) else tokens.borderHairline,
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    DynaSecondaryButton(
      text = strings.gateOpenSettings,
      onClick = onOpenSettings,
      testTag = testTag,
    )
  }
}

@Composable
fun DynaSettingRow(
  title: String,
  description: String? = null,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  showDivider: Boolean = true,
  control: @Composable () -> Unit,
) {
  val tokens = LocalDynaTokens.current
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
          color = if (enabled) tokens.textPrimary else tokens.textMuted,
        )
        if (!description.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) tokens.textSecondary else tokens.textMuted,
          )
        }
      }
      control()
    }
    if (showDivider) {
      HorizontalDivider(color = tokens.borderHairline, thickness = 1.dp)
    }
  }
}

@Composable
fun DynaInfoCallout(
  text: String,
  modifier: Modifier = Modifier,
  isWarning: Boolean = false,
) {
  val tokens = LocalDynaTokens.current
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(tokens.radiusMd))
        .background(
          if (isWarning) tokens.statusDanger.copy(alpha = 0.12f) else tokens.surfaceRaised
        )
        .border(
          1.dp,
          if (isWarning) tokens.statusDanger.copy(alpha = 0.45f) else tokens.borderHairline,
          RoundedCornerShape(tokens.radiusMd),
        )
        .padding(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      imageVector = if (isWarning) Icons.Outlined.WarningAmber else Icons.Outlined.Info,
      contentDescription = null,
      tint = if (isWarning) tokens.statusDanger else tokens.textSecondary,
      modifier = Modifier.size(16.dp).padding(top = 1.dp),
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = if (isWarning) tokens.textPrimary else tokens.textSecondary,
    )
  }
}

@Composable
fun DynaConfirmationDialog(
  title: String,
  body: String,
  confirmLabel: String,
  cancelLabel: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  isDanger: Boolean = true,
) {
  val tokens = LocalDynaTokens.current
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = tokens.surfaceCard,
    titleContentColor = tokens.textPrimary,
    textContentColor = tokens.textSecondary,
    title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
    text = { Text(text = body, style = MaterialTheme.typography.bodyMedium) },
    confirmButton = {
      Button(
        onClick = onConfirm,
        shape = RoundedCornerShape(tokens.radiusMd),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = if (isDanger) tokens.statusDanger else tokens.accentPrimary,
            contentColor = tokens.onAccentPrimary,
          ),
      ) {
        Text(confirmLabel)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(cancelLabel, color = tokens.textSecondary)
      }
    },
  )
}
