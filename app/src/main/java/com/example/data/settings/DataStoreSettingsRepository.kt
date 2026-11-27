package com.example.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.UserSettings
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dynaDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "dyna_settings")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

  private object Keys {
    val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    val WIDTH_DP = floatPreferencesKey("width_dp")
    val HEIGHT_DP = floatPreferencesKey("height_dp")
    val CORNER_RADIUS_DP = floatPreferencesKey("corner_radius_dp")
    val TOP_OFFSET_DP = floatPreferencesKey("top_offset_dp")
    val ANIMATION_ENABLED = booleanPreferencesKey("animation_enabled")
    val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
    val AUTO_COLLAPSE_SECONDS = intPreferencesKey("auto_collapse_seconds")
    val THEME_PRESET = stringPreferencesKey("theme_preset")
    val CUSTOM_BG = longPreferencesKey("custom_bg")
    val CUSTOM_FG = longPreferencesKey("custom_fg")
    val CUSTOM_ACCENT = longPreferencesKey("custom_accent")
    val CUSTOM_ICON = longPreferencesKey("custom_icon")
    val TIMER_ENABLED = booleanPreferencesKey("timer_enabled")
    val NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    val NOTIF_SHOW_PREVIEW = booleanPreferencesKey("notif_show_preview")
    val NOTIF_AUTO_EXPAND = booleanPreferencesKey("notif_auto_expand")
    val START_ON_BOOT = booleanPreferencesKey("start_on_boot")
  }

  override val settings: Flow<UserSettings> =
    context.dynaDataStore.data.map { prefs ->
      val defaults = UserSettings()
      val defaultApp = defaults.appearance
      val defaultCustom = defaultApp.customColors
      val preset =
        prefs[Keys.THEME_PRESET]?.let { name ->
          IslandThemePreset.entries.firstOrNull { it.name == name }
        } ?: defaultApp.themePreset

      UserSettings(
        isOverlayEnabled = prefs[Keys.OVERLAY_ENABLED] ?: defaults.isOverlayEnabled,
        appearance =
          IslandAppearance(
            widthDp = prefs[Keys.WIDTH_DP] ?: defaultApp.widthDp,
            heightDp = prefs[Keys.HEIGHT_DP] ?: defaultApp.heightDp,
            cornerRadiusDp = prefs[Keys.CORNER_RADIUS_DP] ?: defaultApp.cornerRadiusDp,
            topOffsetDp = prefs[Keys.TOP_OFFSET_DP] ?: defaultApp.topOffsetDp,
            animationEnabled = prefs[Keys.ANIMATION_ENABLED] ?: defaultApp.animationEnabled,
            animationSpeedMultiplier =
              prefs[Keys.ANIMATION_SPEED] ?: defaultApp.animationSpeedMultiplier,
            autoCollapseSeconds =
              prefs[Keys.AUTO_COLLAPSE_SECONDS] ?: defaultApp.autoCollapseSeconds,
            themePreset = preset,
            customColors =
              IslandThemeColors(
                backgroundArgb = prefs[Keys.CUSTOM_BG] ?: defaultCustom.backgroundArgb,
                foregroundArgb = prefs[Keys.CUSTOM_FG] ?: defaultCustom.foregroundArgb,
                accentArgb = prefs[Keys.CUSTOM_ACCENT] ?: defaultCustom.accentArgb,
                iconArgb = prefs[Keys.CUSTOM_ICON] ?: defaultCustom.iconArgb,
              ),
          ),
        timerActivityEnabled = prefs[Keys.TIMER_ENABLED] ?: defaults.timerActivityEnabled,
        notificationActivityEnabled =
          prefs[Keys.NOTIF_ENABLED] ?: defaults.notificationActivityEnabled,
        showNotificationPreview =
          prefs[Keys.NOTIF_SHOW_PREVIEW] ?: defaults.showNotificationPreview,
        autoExpandOnNotification =
          prefs[Keys.NOTIF_AUTO_EXPAND] ?: defaults.autoExpandOnNotification,
        startOnBoot = prefs[Keys.START_ON_BOOT] ?: defaults.startOnBoot,
      )
    }

  override suspend fun setOverlayEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.OVERLAY_ENABLED] = enabled }
  }

  override suspend fun updateAppearance(transform: (IslandAppearance) -> IslandAppearance) {
    context.dynaDataStore.edit { prefs ->
      val defaultApp = IslandAppearance()
      val current =
        IslandAppearance(
          widthDp = prefs[Keys.WIDTH_DP] ?: defaultApp.widthDp,
          heightDp = prefs[Keys.HEIGHT_DP] ?: defaultApp.heightDp,
          cornerRadiusDp = prefs[Keys.CORNER_RADIUS_DP] ?: defaultApp.cornerRadiusDp,
          topOffsetDp = prefs[Keys.TOP_OFFSET_DP] ?: defaultApp.topOffsetDp,
          animationEnabled = prefs[Keys.ANIMATION_ENABLED] ?: defaultApp.animationEnabled,
          animationSpeedMultiplier =
            prefs[Keys.ANIMATION_SPEED] ?: defaultApp.animationSpeedMultiplier,
          autoCollapseSeconds =
            prefs[Keys.AUTO_COLLAPSE_SECONDS] ?: defaultApp.autoCollapseSeconds,
          themePreset =
            prefs[Keys.THEME_PRESET]?.let { name ->
              IslandThemePreset.entries.firstOrNull { it.name == name }
            } ?: defaultApp.themePreset,
          customColors =
            IslandThemeColors(
              backgroundArgb = prefs[Keys.CUSTOM_BG] ?: IslandThemeColors.Midnight.backgroundArgb,
              foregroundArgb = prefs[Keys.CUSTOM_FG] ?: IslandThemeColors.Midnight.foregroundArgb,
              accentArgb = prefs[Keys.CUSTOM_ACCENT] ?: IslandThemeColors.Midnight.accentArgb,
              iconArgb = prefs[Keys.CUSTOM_ICON] ?: IslandThemeColors.Midnight.iconArgb,
            ),
        )
      val updated = transform(current)
      prefs[Keys.WIDTH_DP] = updated.widthDp
      prefs[Keys.HEIGHT_DP] = updated.heightDp
      prefs[Keys.CORNER_RADIUS_DP] = updated.cornerRadiusDp
      prefs[Keys.TOP_OFFSET_DP] = updated.topOffsetDp
      prefs[Keys.ANIMATION_ENABLED] = updated.animationEnabled
      prefs[Keys.ANIMATION_SPEED] = updated.animationSpeedMultiplier
      prefs[Keys.AUTO_COLLAPSE_SECONDS] = updated.autoCollapseSeconds
      prefs[Keys.THEME_PRESET] = updated.themePreset.name
    }
  }

  override suspend fun setThemePreset(preset: IslandThemePreset) {
    context.dynaDataStore.edit { it[Keys.THEME_PRESET] = preset.name }
  }

  override suspend fun updateCustomColors(colors: IslandThemeColors) {
    context.dynaDataStore.edit { prefs ->
      prefs[Keys.THEME_PRESET] = IslandThemePreset.CUSTOM.name
      prefs[Keys.CUSTOM_BG] = colors.backgroundArgb
      prefs[Keys.CUSTOM_FG] = colors.foregroundArgb
      prefs[Keys.CUSTOM_ACCENT] = colors.accentArgb
      prefs[Keys.CUSTOM_ICON] = colors.iconArgb
    }
  }

  override suspend fun setTimerActivityEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.TIMER_ENABLED] = enabled }
  }

  override suspend fun setNotificationActivityEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.NOTIF_ENABLED] = enabled }
  }

  override suspend fun setShowNotificationPreview(show: Boolean) {
    context.dynaDataStore.edit { it[Keys.NOTIF_SHOW_PREVIEW] = show }
  }

  override suspend fun setAutoExpandOnNotification(autoExpand: Boolean) {
    context.dynaDataStore.edit { it[Keys.NOTIF_AUTO_EXPAND] = autoExpand }
  }

  override suspend fun setStartOnBoot(startOnBoot: Boolean) {
    context.dynaDataStore.edit { it[Keys.START_ON_BOOT] = startOnBoot }
  }

  override suspend fun resetAppearance() {
    val defaultApp = IslandAppearance()
    context.dynaDataStore.edit { prefs ->
      prefs[Keys.WIDTH_DP] = defaultApp.widthDp
      prefs[Keys.HEIGHT_DP] = defaultApp.heightDp
      prefs[Keys.CORNER_RADIUS_DP] = defaultApp.cornerRadiusDp
      prefs[Keys.TOP_OFFSET_DP] = defaultApp.topOffsetDp
      prefs[Keys.ANIMATION_ENABLED] = defaultApp.animationEnabled
      prefs[Keys.ANIMATION_SPEED] = defaultApp.animationSpeedMultiplier
      prefs[Keys.AUTO_COLLAPSE_SECONDS] = defaultApp.autoCollapseSeconds
    }
  }

  override suspend fun resetAllSettings() {
    context.dynaDataStore.edit { it.clear() }
  }
}
