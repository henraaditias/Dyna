package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.CitySearchResult
import com.example.domain.model.DashboardThemeMode
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaConfirmationDialog
import com.example.ui.components.DynaLogoMark
import com.example.ui.components.DynaSecondaryButton
import com.example.ui.components.DynaSettingRow
import com.example.ui.components.DynaStatusBadge
import com.example.ui.components.DynaSwitch
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.LocalDynaStrings
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaUiState

@Composable
fun PengaturanScreen(
  uiState: DynaUiState,
  onRefreshPermissions: () -> Unit,
  onSetLanguage: (AppLanguage) -> Unit,
  onSetDashboardThemeMode: (DashboardThemeMode) -> Unit,
  onSetOverlayEnabled: (Boolean) -> Unit,
  onSetCalendarEnabled: (Boolean) -> Unit,
  onSetWeatherEnabled: (Boolean) -> Unit,
  onSetShortcutsEnabled: (Boolean) -> Unit,
  onSetNotificationActivityEnabled: (Boolean) -> Unit,
  onSetShowNotificationPreview: (Boolean) -> Unit,
  onSetMediaActivityEnabled: (Boolean) -> Unit,
  onSetCallActivityEnabled: (Boolean) -> Unit,
  onSetBatteryActivityEnabled: (Boolean) -> Unit,
  onSetTimerActivityEnabled: (Boolean) -> Unit,
  onSetStopwatchEnabled: (Boolean) -> Unit,
  onSetPrayerAlarmEnabled: (Boolean) -> Unit,
  onSetHideTopDotEnabled: (Boolean) -> Unit,
  onSearchCities: (String) -> Unit,
  onClearCitySearch: () -> Unit,
  onSelectCity: (String, Double, Double) -> Unit,
  onUseCurrentGpsLocation: () -> Unit,
  onResetAllSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  val settings = uiState.settings
  val permissions = uiState.permissions

  var showPrivacyDialog by remember { mutableStateOf(false) }
  var showCityPicker by remember { mutableStateOf(false) }
  var showResetDialog by remember { mutableStateOf(false) }

  val locationLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      onRefreshPermissions()
    }
  val calendarLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      onRefreshPermissions()
    }
  val postNotifLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      onRefreshPermissions()
    }

  if (showPrivacyDialog) {
    AlertDialog(
      onDismissRequest = { showPrivacyDialog = false },
      containerColor = tokens.surfaceCard,
      titleContentColor = tokens.textPrimary,
      textContentColor = tokens.textSecondary,
      title = { Text(text = strings.settingsPrivacyTitle, style = MaterialTheme.typography.titleLarge) },
      text = { Text(text = strings.settingsPrivacyBody, style = MaterialTheme.typography.bodyMedium) },
      confirmButton = {
        TextButton(onClick = { showPrivacyDialog = false }) {
          Text(strings.closeAction, color = tokens.accentPrimary)
        }
      },
    )
  }

  if (showCityPicker) {
    CityPickerDialog(
      currentCity = settings.cityName,
      searchResults = uiState.citySearchResults,
      isSearching = uiState.isSearchingCity,
      onSearch = onSearchCities,
      onUseGps = {
        onUseCurrentGpsLocation()
        showCityPicker = false
      },
      onSelectCity = { city ->
        onSelectCity(city.name, city.latitude, city.longitude)
        showCityPicker = false
      },
      onDismiss = {
        onClearCitySearch()
        showCityPicker = false
      },
    )
  }

  if (showResetDialog) {
    DynaConfirmationDialog(
      title = strings.settingsResetConfirmTitle,
      body = strings.settingsResetConfirmBody,
      confirmLabel = strings.settingsResetAction,
      cancelLabel = strings.cancelAction,
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
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = tokens.bottomNavClearance),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    // Clean Monochrome Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      DynaLogoMark(size = 42.dp)
      Column {
        Text(
          text = strings.settingsTitle,
          style = MaterialTheme.typography.headlineLarge,
          color = tokens.textPrimary,
          modifier = Modifier.testTag("pengaturan_title"),
        )
        Text(
          text = "${strings.appName} ${strings.settingsHeaderSubtitle}",
          style = DynaMonoTypography.LabelMono,
          color = tokens.textSecondary,
        )
      }
    }

    // Optional Permissions & Privacy
    BentoCard(headerLabel = strings.settingsPermSection) {
      OptionalPermissionRow(
        title = strings.settingsLocationPerm,
        description = strings.settingsLocationPermDesc,
        granted = permissions.hasLocationPermission,
        onRequest = { locationLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        testTag = "settings_perm_location",
      )
      HorizontalDivider(color = tokens.borderHairline, modifier = Modifier.padding(vertical = 8.dp))
      OptionalPermissionRow(
        title = strings.settingsCalendarPerm,
        description = strings.settingsCalendarPermDesc,
        granted = permissions.hasCalendarPermission,
        onRequest = { calendarLauncher.launch(Manifest.permission.READ_CALENDAR) },
        testTag = "settings_perm_calendar",
      )
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        HorizontalDivider(color = tokens.borderHairline, modifier = Modifier.padding(vertical = 8.dp))
        OptionalPermissionRow(
          title = strings.settingsPostNotifPerm,
          description = strings.settingsPostNotifPermDesc,
          granted = permissions.hasPostNotificationsPermission,
          onRequest = { postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
          testTag = "settings_perm_post_notif",
        )
      }
      HorizontalDivider(color = tokens.borderHairline, modifier = Modifier.padding(vertical = 8.dp))
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(tokens.radiusSm))
            .clickable { showPrivacyDialog = true }
            .padding(vertical = 8.dp)
            .testTag("settings_privacy_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            tint = tokens.textSecondary,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = strings.settingsPrivacyRow,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = tokens.textPrimary,
          )
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = null,
          tint = tokens.textMuted,
          modifier = Modifier.size(18.dp),
        )
      }
    }

    // General Appearance & System
    BentoCard(headerLabel = strings.settingsGeneralSection) {
      DynaSettingRow(title = strings.settingsLanguageLabel) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          SegmentedPillChip(
            label = "ID",
            selected = settings.language == AppLanguage.ID,
            onClick = { onSetLanguage(AppLanguage.ID) },
            testTag = "settings_lang_id",
          )
          SegmentedPillChip(
            label = "EN",
            selected = settings.language == AppLanguage.EN,
            onClick = { onSetLanguage(AppLanguage.EN) },
            testTag = "settings_lang_en",
          )
        }
      }

      DynaSettingRow(title = strings.settingsThemeLabel) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          SegmentedPillChip(
            label = strings.settingsThemeDark,
            selected = settings.dashboardThemeMode == DashboardThemeMode.DARK,
            onClick = { onSetDashboardThemeMode(DashboardThemeMode.DARK) },
            testTag = "settings_theme_dark",
          )
          SegmentedPillChip(
            label = strings.settingsThemeLight,
            selected = settings.dashboardThemeMode == DashboardThemeMode.LIGHT,
            onClick = { onSetDashboardThemeMode(DashboardThemeMode.LIGHT) },
            testTag = "settings_theme_light",
          )
        }
      }

      DynaSettingRow(
        title = strings.settingsOverlaySwitch,
        description = strings.settingsOverlaySwitchDesc,
      ) {
        DynaSwitch(
          checked = settings.isOverlayEnabled,
          onCheckedChange = onSetOverlayEnabled,
          testTag = "settings_overlay_switch",
        )
      }

      Row(
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(tokens.radiusSm))
            .clickable { showCityPicker = true }
            .padding(vertical = 8.dp)
            .testTag("settings_city_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = strings.settingsCityLabel,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = tokens.textPrimary,
          )
          Text(
            text = settings.cityName,
            style = DynaMonoTypography.LabelMono,
            color = tokens.textSecondary,
          )
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = null,
          tint = tokens.textMuted,
          modifier = Modifier.size(18.dp),
        )
      }
    }

    // Dynamic Island Modules
    BentoCard(headerLabel = strings.settingsFeaturesSection) {
      DynaSettingRow(
        title = strings.settingsCalendarToggle,
        description = strings.settingsCalendarToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.calendarEnabled,
          onCheckedChange = onSetCalendarEnabled,
          testTag = "settings_toggle_calendar",
        )
      }
      DynaSettingRow(
        title = strings.settingsWeatherToggle,
        description = strings.settingsWeatherToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.weatherEnabled,
          onCheckedChange = onSetWeatherEnabled,
          testTag = "settings_toggle_weather",
        )
      }
      DynaSettingRow(
        title = strings.settingsShortcutsToggle,
        description = strings.settingsShortcutsToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.shortcutsEnabled,
          onCheckedChange = onSetShortcutsEnabled,
          testTag = "settings_toggle_shortcuts",
        )
      }
      DynaSettingRow(
        title = strings.settingsNotifToggle,
        description = strings.settingsNotifToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.notificationActivityEnabled,
          onCheckedChange = onSetNotificationActivityEnabled,
          testTag = "settings_toggle_notif",
        )
      }
      DynaSettingRow(
        title = strings.settingsNotifPreviewToggle,
        description = strings.settingsNotifPreviewDesc,
        enabled = settings.notificationActivityEnabled,
      ) {
        DynaSwitch(
          checked = settings.showNotificationPreview,
          onCheckedChange = onSetShowNotificationPreview,
          enabled = settings.notificationActivityEnabled,
          testTag = "settings_toggle_notif_preview",
        )
      }
      DynaSettingRow(
        title = strings.settingsMediaToggle,
        description = strings.settingsMediaToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.mediaActivityEnabled,
          onCheckedChange = onSetMediaActivityEnabled,
          testTag = "settings_toggle_media",
        )
      }
      DynaSettingRow(
        title = strings.settingsCallToggle,
        description = strings.settingsCallToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.callActivityEnabled,
          onCheckedChange = onSetCallActivityEnabled,
          testTag = "settings_toggle_call",
        )
      }
      DynaSettingRow(
        title = strings.settingsBatteryToggle,
        description = strings.settingsBatteryToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.batteryActivityEnabled,
          onCheckedChange = onSetBatteryActivityEnabled,
          testTag = "settings_toggle_battery",
        )
      }
      DynaSettingRow(
        title = strings.settingsTimerToggle,
        description = strings.settingsTimerToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.timerActivityEnabled,
          onCheckedChange = onSetTimerActivityEnabled,
          testTag = "settings_toggle_timer",
        )
      }
      DynaSettingRow(
        title = strings.settingsStopwatchToggle,
        description = strings.settingsStopwatchToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.stopwatchEnabled,
          onCheckedChange = onSetStopwatchEnabled,
          testTag = "settings_toggle_stopwatch",
        )
      }
      DynaSettingRow(
        title = strings.settingsPrayerToggle,
        description = strings.settingsPrayerToggleDesc,
      ) {
        DynaSwitch(
          checked = settings.prayerAlarmEnabled,
          onCheckedChange = onSetPrayerAlarmEnabled,
          testTag = "settings_toggle_prayer",
        )
      }
      DynaSettingRow(
        title = strings.settingsHideDotToggle,
        description = strings.settingsHideDotToggleDesc,
        showDivider = false,
      ) {
        DynaSwitch(
          checked = settings.hideTopDotEnabled,
          onCheckedChange = onSetHideTopDotEnabled,
          testTag = "settings_toggle_hide_dot",
        )
      }
    }

    // Extension Placeholder & Reset
    BentoCard {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp)
            .testTag("settings_extension_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = strings.settingsExtensionTitle,
          style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
          color = tokens.textMuted,
        )
        Surface(
          shape = RoundedCornerShape(tokens.radiusSm),
          color = tokens.surfaceRaised,
          border = BorderStroke(1.dp, tokens.borderHairline),
        ) {
          Text(
            text = strings.settingsExtensionSubtitle,
            style = DynaMonoTypography.LabelMono,
            color = tokens.textMuted,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }

      HorizontalDivider(color = tokens.borderHairline, modifier = Modifier.padding(vertical = 12.dp))

      Text(
        text = strings.settingsResetTitle,
        style = MaterialTheme.typography.titleMedium,
        color = tokens.textPrimary,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = strings.settingsResetDesc,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.textSecondary,
      )
      Spacer(modifier = Modifier.height(12.dp))
      DynaSecondaryButton(
        text = strings.settingsResetTitle,
        onClick = { showResetDialog = true },
        icon = Icons.Outlined.RestartAlt,
        testTag = "settings_reset_button",
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun OptionalPermissionRow(
  title: String,
  description: String,
  granted: Boolean,
  onRequest: () -> Unit,
  testTag: String,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
        color = tokens.textPrimary,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.textSecondary,
      )
    }
    if (granted) {
      DynaStatusBadge(
        text = strings.gateGranted,
        dotColor = tokens.statusActive,
        borderColor = tokens.statusActive.copy(alpha = 0.45f),
      )
    } else {
      Surface(
        modifier =
          Modifier.heightIn(min = 48.dp)
            .clip(RoundedCornerShape(tokens.radiusSm))
            .clickable(onClick = onRequest)
            .testTag(testTag),
        shape = RoundedCornerShape(tokens.radiusSm),
        color = tokens.surfaceRaised,
        border = BorderStroke(1.dp, tokens.borderStrong),
      ) {
        Box(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = strings.gateOpenSettings,
            style = DynaMonoTypography.LabelMono,
            color = tokens.textPrimary,
          )
        }
      }
    }
  }
}

@Composable
private fun SegmentedPillChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  testTag: String,
) {
  val tokens = LocalDynaTokens.current
  Surface(
    modifier =
      Modifier.heightIn(min = 48.dp)
        .clip(RoundedCornerShape(tokens.radiusSm))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(tokens.radiusSm),
    color = if (selected) tokens.accentPrimary else tokens.surfaceRaised,
    border = BorderStroke(1.dp, if (selected) tokens.accentPrimary else tokens.borderHairline),
  ) {
    Box(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = label,
        style = DynaMonoTypography.LabelMono,
        color = if (selected) tokens.onAccentPrimary else tokens.textSecondary,
      )
    }
  }
}

@Composable
private fun CityPickerDialog(
  currentCity: String,
  searchResults: List<CitySearchResult>,
  isSearching: Boolean,
  onSearch: (String) -> Unit,
  onUseGps: () -> Unit,
  onSelectCity: (CitySearchResult) -> Unit,
  onDismiss: () -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  var query by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = tokens.surfaceCard,
    titleContentColor = tokens.textPrimary,
    title = {
      Column {
        Text(text = strings.settingsCityDialogTitle, style = MaterialTheme.typography.titleLarge)
        Text(
          text = currentCity,
          style = DynaMonoTypography.LabelMono,
          color = tokens.textSecondary,
        )
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(tokens.radiusMd))
              .background(tokens.surfaceInset)
              .border(1.dp, tokens.borderHairline, RoundedCornerShape(tokens.radiusMd))
              .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = tokens.textMuted,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
              Text(
                text = strings.settingsCitySearchHint,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
              )
            }
            BasicTextField(
              value = query,
              onValueChange = {
                query = it
                onSearch(it)
              },
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyMedium.copy(color = tokens.textPrimary),
              cursorBrush = SolidColor(tokens.accentPrimary),
              modifier = Modifier.fillMaxWidth().testTag("settings_city_search_input"),
            )
          }
        }

        DynaSecondaryButton(
          text = strings.settingsUseGps,
          onClick = onUseGps,
          icon = Icons.Filled.LocationOn,
          testTag = "settings_use_gps_button",
          modifier = Modifier.fillMaxWidth(),
        )

        if (!isSearching && searchResults.isNotEmpty()) {
          LazyColumn(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(searchResults) { city ->
              Surface(
                modifier =
                  Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(tokens.radiusMd))
                    .clickable { onSelectCity(city) },
                shape = RoundedCornerShape(tokens.radiusMd),
                color = tokens.surfaceRaised,
                border = BorderStroke(1.dp, tokens.borderHairline),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = listOfNotNull(city.name, city.country).joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.closeAction, color = tokens.textSecondary)
      }
    },
  )
}
