package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.DynaPalette
import com.example.ui.theme.LocalDynaTokens

@Composable
fun BentoCanvasBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  val tokens = LocalDynaTokens.current
  Box(
    modifier =
      modifier.fillMaxSize().background(tokens.canvasBg).drawBehind {
        val stepPx = 24.dp.toPx()
        val dotRadius = 1.dp.toPx()
        var x = stepPx / 2f
        while (x < size.width) {
          var y = stepPx / 2f
          while (y < size.height) {
            drawCircle(color = DynaPalette.DotGrid, radius = dotRadius, center = Offset(x, y))
            y += stepPx
          }
          x += stepPx
        }
      }
  ) {
    content()
  }
}

@Composable
fun BentoCard(
  modifier: Modifier = Modifier,
  headerLabel: String? = null,
  headerAction: (@Composable RowScope.() -> Unit)? = null,
  highlighted: Boolean = false,
  content: @Composable ColumnScope.() -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val borderColor = if (highlighted) tokens.accentCyan.copy(alpha = 0.55f) else tokens.surfaceBorder
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(tokens.bentoRadius),
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
              style = DynaMonoTypography.LabelMonoCaps,
              color = tokens.textSecondary,
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
  borderColor: Color = LocalDynaTokens.current.surfaceBorder,
) {
  val tokens = LocalDynaTokens.current
  Row(
    modifier =
      modifier
        .clip(RoundedCornerShape(tokens.controlRadius))
        .background(tokens.surfaceRaised)
        .border(1.dp, borderColor, RoundedCornerShape(tokens.controlRadius))
        .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(dotColor))
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = text,
      style = DynaMonoTypography.LabelMonoCaps,
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
          !enabled -> tokens.surfaceCard
          checked -> tokens.accentCyan.copy(alpha = 0.22f)
          else -> tokens.surfaceRaised
        },
      label = "switch_track",
    )
  val borderColor by
    animateColorAsState(
      targetValue =
        when {
          !enabled -> tokens.surfaceBorder.copy(alpha = 0.5f)
          checked -> tokens.accentCyan
          else -> tokens.surfaceBorder
        },
      label = "switch_border",
    )
  val thumbColor by
    animateColorAsState(
      targetValue =
        when {
          !enabled -> tokens.textMuted
          checked -> tokens.accentCyan
          else -> tokens.textSecondary
        },
      label = "switch_thumb",
    )
  val thumbOffset by animateDpAsState(targetValue = if (checked) 20.dp else 2.dp, label = "thumb")

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
        Modifier.width(44.dp)
          .height(24.dp)
          .clip(RoundedCornerShape(tokens.controlRadius))
          .background(trackColor)
          .border(1.dp, borderColor, RoundedCornerShape(tokens.controlRadius)),
      contentAlignment = Alignment.CenterStart,
    ) {
      Box(
        modifier =
          Modifier.offset(x = thumbOffset)
            .size(18.dp)
            .clip(RoundedCornerShape(2.dp))
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
    shape = RoundedCornerShape(tokens.controlRadius),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = tokens.textPrimary,
        contentColor = Color.Black,
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
    shape = RoundedCornerShape(tokens.controlRadius),
    border = BorderStroke(1.dp, if (enabled) tokens.surfaceBorder else tokens.borderSubtle),
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
            tint = if (granted) tokens.statusOnline else tokens.textSecondary,
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
        text =
          if (granted) stringResource(R.string.perm_granted)
          else stringResource(R.string.perm_not_granted),
        dotColor = if (granted) tokens.statusOnline else tokens.textMuted,
        borderColor =
          if (granted) tokens.statusOnline.copy(alpha = 0.4f) else tokens.surfaceBorder,
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    DynaSecondaryButton(
      text = stringResource(R.string.action_open_settings),
      onClick = onOpenSettings,
      testTag = testTag,
    )
  }
}

@Composable
fun DynaSettingRow(
  title: String,
  description: String,
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
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = if (enabled) tokens.textSecondary else tokens.textMuted,
        )
      }
      control()
    }
    if (showDivider) {
      HorizontalDivider(color = tokens.surfaceBorder, thickness = 1.dp)
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
        .clip(RoundedCornerShape(tokens.controlRadius))
        .background(if (isWarning) DynaPalette.ErrorContainer.copy(alpha = 0.25f) else tokens.surfaceRaised)
        .border(
          1.dp,
          if (isWarning) DynaPalette.Error.copy(alpha = 0.4f) else tokens.surfaceBorder,
          RoundedCornerShape(tokens.controlRadius),
        )
        .padding(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      imageVector = if (isWarning) Icons.Outlined.WarningAmber else Icons.Outlined.Info,
      contentDescription = null,
      tint = if (isWarning) DynaPalette.Error else tokens.accentCyan,
      modifier = Modifier.size(16.dp).padding(top = 1.dp),
    )
    Spacer(modifier = Modifier.width(10.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodySmall,
      color = if (isWarning) DynaPalette.OnErrorContainer else tokens.textSecondary,
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
        shape = RoundedCornerShape(tokens.controlRadius),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = DynaPalette.ErrorContainer,
            contentColor = DynaPalette.OnErrorContainer,
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
