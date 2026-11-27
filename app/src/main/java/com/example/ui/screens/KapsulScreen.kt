package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.IslandThemePreset
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaInfoCallout
import com.example.ui.components.DynaSecondaryButton
import com.example.ui.components.DynaSettingRow
import com.example.ui.components.DynaStatusBadge
import com.example.ui.components.DynaSwitch
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaUiState
import kotlin.math.roundToInt

private enum class KapsulTab {
  GEOMETRY,
  THEME,
}

@Composable
fun KapsulScreen(
  uiState: DynaUiState,
  onUpdateAppearance: ((IslandAppearance) -> IslandAppearance) -> Unit,
  onResetAppearance: () -> Unit,
  onSelectPreset: (IslandThemePreset) -> Unit,
  onUpdateCustomColors: (IslandThemeColors) -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  var activeTab by rememberSaveable { mutableStateOf(KapsulTab.GEOMETRY) }
  val scrollState = rememberScrollState()

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
        label = stringResource(R.string.kapsul_seg_geometry),
        selected = activeTab == KapsulTab.GEOMETRY,
        onClick = { activeTab = KapsulTab.GEOMETRY },
        modifier = Modifier.weight(1f).testTag("kapsul_tab_geometry"),
      )
      KapsulSegmentButton(
        label = stringResource(R.string.kapsul_seg_theme),
        selected = activeTab == KapsulTab.THEME,
        onClick = { activeTab = KapsulTab.THEME },
        modifier = Modifier.weight(1f).testTag("kapsul_tab_theme"),
      )
    }

    DynaInfoCallout(
      text =
        if (uiState.isOverlayAttached) stringResource(R.string.kapsul_live_hint_on)
        else stringResource(R.string.kapsul_live_hint_off)
    )

    when (activeTab) {
      KapsulTab.GEOMETRY ->
        GeometrySection(
          appearance = uiState.settings.appearance,
          onUpdateAppearance = onUpdateAppearance,
          onResetAppearance = onResetAppearance,
        )
      KapsulTab.THEME ->
        ThemeSection(
          appearance = uiState.settings.appearance,
          onSelectPreset = onSelectPreset,
          onUpdateCustomColors = onUpdateCustomColors,
        )
    }
  }
}

@Composable
private fun GeometrySection(
  appearance: IslandAppearance,
  onUpdateAppearance: ((IslandAppearance) -> IslandAppearance) -> Unit,
  onResetAppearance: () -> Unit,
) {
  val tokens = LocalDynaTokens.current

  BentoCard {
    CompactSlider(
      label = stringResource(R.string.tampilan_width),
      valueText = "${appearance.widthDp.roundToInt()} dp",
      value = appearance.widthDp,
      range = 110f..220f,
      onValueChange = { next -> onUpdateAppearance { it.copy(widthDp = next) } },
      testTag = "slider_width",
    )
    CompactSlider(
      label = stringResource(R.string.tampilan_height),
      valueText = "${appearance.heightDp.roundToInt()} dp",
      value = appearance.heightDp,
      range = 28f..48f,
      onValueChange = { next -> onUpdateAppearance { it.copy(heightDp = next) } },
      testTag = "slider_height",
    )
    CompactSlider(
      label = stringResource(R.string.tampilan_radius),
      valueText = "${appearance.cornerRadiusDp.roundToInt()} dp",
      value = appearance.cornerRadiusDp,
      range = 12f..28f,
      onValueChange = { next -> onUpdateAppearance { it.copy(cornerRadiusDp = next) } },
      testTag = "slider_radius",
    )
    CompactSlider(
      label = stringResource(R.string.tampilan_offset),
      valueText = "${appearance.topOffsetDp.roundToInt()} dp",
      value = appearance.topOffsetDp,
      range = 0f..48f,
      onValueChange = { next -> onUpdateAppearance { it.copy(topOffsetDp = next) } },
      testTag = "slider_offset",
    )

    HorizontalDivider(color = tokens.surfaceBorder, modifier = Modifier.padding(vertical = 8.dp))

    DynaSettingRow(
      title = stringResource(R.string.tampilan_anim_enabled),
      description = stringResource(R.string.tampilan_anim_enabled_desc),
      showDivider = false,
      control = {
        DynaSwitch(
          checked = appearance.animationEnabled,
          onCheckedChange = { enabled ->
            onUpdateAppearance { it.copy(animationEnabled = enabled) }
          },
          testTag = "switch_animation_enabled",
        )
      },
    )

    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      listOf(0 to "Manual", 5 to "5 dtk", 10 to "10 dtk").forEach { (secs, title) ->
        KapsulSegmentButton(
          label = title,
          selected = appearance.autoCollapseSeconds == secs,
          onClick = { onUpdateAppearance { it.copy(autoCollapseSeconds = secs) } },
          modifier = Modifier.weight(1f),
        )
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    DynaSecondaryButton(
      text = stringResource(R.string.tampilan_reset),
      onClick = onResetAppearance,
      icon = Icons.Outlined.RestartAlt,
      testTag = "reset_appearance_button",
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@Composable
private fun CompactSlider(
  label: String,
  valueText: String,
  value: Float,
  range: ClosedFloatingPointRange<Float>,
  onValueChange: (Float) -> Unit,
  testTag: String,
) {
  val tokens = LocalDynaTokens.current
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = tokens.textPrimary,
      )
      Text(
        text = valueText,
        style = DynaMonoTypography.LabelCode.copy(fontFeatureSettings = "tnum"),
        color = tokens.accentCyan,
      )
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = range,
      modifier = Modifier.fillMaxWidth().testTag(testTag),
      colors =
        SliderDefaults.colors(
          thumbColor = tokens.accentCyan,
          activeTrackColor = tokens.accentCyan,
          inactiveTrackColor = tokens.surfaceRaised,
        ),
    )
  }
}

@Composable
private fun ThemeSection(
  appearance: IslandAppearance,
  onSelectPreset: (IslandThemePreset) -> Unit,
  onUpdateCustomColors: (IslandThemeColors) -> Unit,
) {
  val presets = IslandThemePreset.entries.toList()
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    BentoCard {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        presets.chunked(2).forEach { rowPresets ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            rowPresets.forEach { preset ->
              val colors =
                when (preset) {
                  IslandThemePreset.MIDNIGHT -> IslandThemeColors.Midnight
                  IslandThemePreset.MINIMAL -> IslandThemeColors.Minimal
                  IslandThemePreset.AURORA -> IslandThemeColors.Aurora
                  IslandThemePreset.CUSTOM -> appearance.customColors
                }
              ThemeTile(
                name = preset.displayName,
                colors = colors,
                selected = appearance.themePreset == preset,
                onClick = { onSelectPreset(preset) },
                testTag = "theme_tile_${preset.name.lowercase()}",
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }
    }

    if (appearance.themePreset == IslandThemePreset.CUSTOM) {
      CustomColorCard(
        customColors = appearance.customColors,
        onUpdateCustomColors = onUpdateCustomColors,
      )
    }
  }
}

@Composable
private fun ThemeTile(
  name: String,
  colors: IslandThemeColors,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val bg = Color(colors.backgroundArgb)
  val fg = Color(colors.foregroundArgb)
  val accent = Color(colors.accentArgb)
  val iconColor = Color(colors.iconArgb)

  Surface(
    modifier =
      modifier
        .testTag(testTag)
        .heightIn(min = 104.dp)
        .clip(RoundedCornerShape(tokens.bentoRadius))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(tokens.bentoRadius),
    color = tokens.surfaceRaised,
    border =
      BorderStroke(
        width = if (selected) 1.5.dp else 1.dp,
        color = if (selected) tokens.accentCyan else tokens.surfaceBorder,
      ),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .height(28.dp)
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, accent.copy(alpha = 0.4f), CircleShape)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Icon(
          imageVector = Icons.Filled.Notifications,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(13.dp),
        )
        Box(modifier = Modifier.width(36.dp).height(4.dp).clip(CircleShape).background(fg))
        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(accent))
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = name,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = tokens.textPrimary,
        )
        if (selected) {
          DynaStatusBadge(text = stringResource(R.string.tema_selected), dotColor = tokens.accentCyan)
        }
      }
    }
  }
}

@Composable
private fun CustomColorCard(
  customColors: IslandThemeColors,
  onUpdateCustomColors: (IslandThemeColors) -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val contrastOk = customColors.hasReadableContrast
  val ratioLabel = String.format("%.1f:1", customColors.contrastRatio())

  BentoCard(
    headerLabel = stringResource(R.string.tema_custom_section),
    headerAction = {
      DynaStatusBadge(
        text = "KONTRAS $ratioLabel",
        dotColor = if (contrastOk) tokens.statusOnline else Color(0xFFFFB4AB),
      )
    },
  ) {
    CompactColorPicker(
      label = stringResource(R.string.tema_color_bg),
      selectedArgb = customColors.backgroundArgb,
      options = listOf(0xFF050507, 0xFF141416, 0xFF0A192F, 0xFF1E1B2E, 0xFFF4F4F5),
      onSelect = { onUpdateCustomColors(customColors.copy(backgroundArgb = it)) },
    )
    CompactColorPicker(
      label = stringResource(R.string.tema_color_fg),
      selectedArgb = customColors.foregroundArgb,
      options = listOf(0xFFFFFFFF, 0xFFE5E1E4, 0xFFBAE6FD, 0xFF09090B, 0xFF1E293B),
      onSelect = { onUpdateCustomColors(customColors.copy(foregroundArgb = it)) },
    )
    CompactColorPicker(
      label = stringResource(R.string.tema_color_accent),
      selectedArgb = customColors.accentArgb,
      options = listOf(0xFF38BDF8, 0xFF10B981, 0xFF60A5FA, 0xFFF59E0B, 0xFFF43F5E),
      onSelect = { onUpdateCustomColors(customColors.copy(accentArgb = it)) },
    )
    CompactColorPicker(
      label = stringResource(R.string.tema_color_icon),
      selectedArgb = customColors.iconArgb,
      options = listOf(0xFF38BDF8, 0xFFFFFFFF, 0xFF10B981, 0xFF60A5FA, 0xFFFBBF24),
      onSelect = { onUpdateCustomColors(customColors.copy(iconArgb = it)) },
    )

    Spacer(modifier = Modifier.height(6.dp))
    DynaInfoCallout(
      text =
        if (contrastOk) stringResource(R.string.tema_contrast_ok)
        else stringResource(R.string.tema_contrast_low),
      isWarning = !contrastOk,
    )
  }
}

@Composable
private fun CompactColorPicker(
  label: String,
  selectedArgb: Long,
  options: List<Long>,
  onSelect: (Long) -> Unit,
) {
  val tokens = LocalDynaTokens.current
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = tokens.textPrimary)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      options.forEach { argb ->
        val isSelected = argb == selectedArgb
        val checkTint =
          if (argb == 0xFFF4F4F5 || argb == 0xFFFFFFFF || argb == 0xFFBAE6FD) Color.Black
          else Color.White
        Box(
          modifier =
            Modifier.size(40.dp)
              .clip(RoundedCornerShape(tokens.controlRadius))
              .clickable { onSelect(argb) },
          contentAlignment = Alignment.Center,
        ) {
          Box(
            modifier =
              Modifier.size(28.dp)
                .clip(RoundedCornerShape(tokens.controlRadius))
                .background(Color(argb))
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) tokens.accentCyan else tokens.surfaceBorder,
                  shape = RoundedCornerShape(tokens.controlRadius),
                ),
            contentAlignment = Alignment.Center,
          ) {
            if (isSelected) {
              Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = "Dipilih",
                tint = checkTint,
                modifier = Modifier.size(14.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun KapsulSegmentButton(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  Surface(
    modifier =
      modifier
        .heightIn(min = 48.dp)
        .clip(RoundedCornerShape(tokens.controlRadius))
        .clickable(onClick = onClick),
    shape = RoundedCornerShape(tokens.controlRadius),
    color = if (selected) tokens.accentGlow else tokens.surfaceRaised,
    border = BorderStroke(1.dp, if (selected) tokens.accentCyan else tokens.surfaceBorder),
  ) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = if (selected) tokens.textPrimary else tokens.textSecondary,
      )
    }
  }
}
