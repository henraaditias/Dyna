package com.example.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppLanguage
import com.example.domain.model.DashboardThemeMode
import com.example.domain.model.ExpressionState
import com.example.domain.model.IdleDisplayMode
import com.example.domain.model.IdlePlacement
import com.example.domain.model.IdleTapAction
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.IslandThemePreset
import com.example.domain.model.QuickMenuItem
import com.example.domain.model.UserSettings
import com.example.domain.model.WeatherSnapshot
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dynaDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "dyna_settings")

class DataStoreSettingsRepository(private val context: Context) : SettingsRepository {

  private object Keys {
    val OVERLAY_ENABLED = booleanPreferencesKey("overlay_enabled")
    val DASHBOARD_THEME = stringPreferencesKey("dashboard_theme_mode")
    val APP_LANGUAGE = stringPreferencesKey("app_language")

    val WIDTH_DP = floatPreferencesKey("width_dp")
    val HEIGHT_DP = floatPreferencesKey("height_dp")
    val CORNER_RADIUS_DP = floatPreferencesKey("corner_radius_dp")
    val TOP_OFFSET_DP = floatPreferencesKey("top_offset_dp")
    val HORIZONTAL_OFFSET_DP = floatPreferencesKey("horizontal_offset_dp")
    val NOTIF_WIDTH_DP = floatPreferencesKey("notif_width_dp")
    val NOTIF_MAX_HEIGHT_DP = floatPreferencesKey("notif_max_height_dp")
    val NOTIF_OPACITY = floatPreferencesKey("notif_opacity")
    val MENU_WIDTH_DP = floatPreferencesKey("menu_width_dp")
    val MENU_HEIGHT_DP = floatPreferencesKey("menu_height_dp")

    val ANIMATION_ENABLED = booleanPreferencesKey("animation_enabled")
    val ANIMATION_SPEED = floatPreferencesKey("animation_speed")
    val AUTO_COLLAPSE_SECONDS = intPreferencesKey("auto_collapse_seconds")
    val AUTO_DIM_DELAY_SECONDS = intPreferencesKey("auto_dim_delay_seconds")
    val THEME_PRESET = stringPreferencesKey("theme_preset")
    val CUSTOM_BG = longPreferencesKey("custom_bg")
    val CUSTOM_FG = longPreferencesKey("custom_fg")
    val CUSTOM_ACCENT = longPreferencesKey("custom_accent")
    val CUSTOM_ICON = longPreferencesKey("custom_icon")
    val IDLE_DISPLAY_MODE = stringPreferencesKey("idle_display_mode")
    val IDLE_PLACEMENT = stringPreferencesKey("idle_placement")
    val IDLE_TAP_ACTION = stringPreferencesKey("idle_tap_action")
    val SELECTED_EXPRESSION = stringPreferencesKey("selected_expression")
    val AUTO_DIM_ENABLED = booleanPreferencesKey("auto_dim_enabled")
    val DIMMED_OPACITY = floatPreferencesKey("dimmed_opacity")

    val CALENDAR_ENABLED = booleanPreferencesKey("calendar_enabled")
    val WEATHER_ENABLED = booleanPreferencesKey("weather_enabled")
    val SHORTCUTS_ENABLED = booleanPreferencesKey("shortcuts_enabled")
    val TIMER_ENABLED = booleanPreferencesKey("timer_enabled")
    val STOPWATCH_ENABLED = booleanPreferencesKey("stopwatch_enabled")
    val NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
    val MEDIA_ENABLED = booleanPreferencesKey("media_enabled")
    val CALL_ENABLED = booleanPreferencesKey("call_enabled")
    val BATTERY_ENABLED = booleanPreferencesKey("battery_enabled")
    val PRAYER_ENABLED = booleanPreferencesKey("prayer_enabled")
    val HIDE_TOP_DOT_ENABLED = booleanPreferencesKey("hide_top_dot_enabled")
    val NOTIF_SHOW_PREVIEW = booleanPreferencesKey("notif_show_preview")
    val NOTIF_AUTO_EXPAND = booleanPreferencesKey("notif_auto_expand")
    val START_ON_BOOT = booleanPreferencesKey("start_on_boot")

    val CITY_NAME = stringPreferencesKey("city_name")
    val CITY_LAT = doublePreferencesKey("city_lat")
    val CITY_LON = doublePreferencesKey("city_lon")
    val USE_GPS = booleanPreferencesKey("use_gps_location")
    val PINNED_SHORTCUTS = stringPreferencesKey("pinned_shortcuts")
    val SHORTCUT_SLOTS = stringPreferencesKey("shortcut_slots")
    val QUICK_MENU_ITEMS = stringPreferencesKey("quick_menu_items")
    val WEATHER_TEMP = floatPreferencesKey("weather_temp_c")
    val WEATHER_CODE = intPreferencesKey("weather_code")
    val WEATHER_COND = stringPreferencesKey("weather_condition")
    val WEATHER_WIND = floatPreferencesKey("weather_wind_kmh")
    val WEATHER_IS_DAY = booleanPreferencesKey("weather_is_day")
    val WEATHER_UPDATED = longPreferencesKey("weather_updated_ms")
  }

  override val settings: Flow<UserSettings> =
    context.dynaDataStore.data.map { prefs ->
      val defaults = UserSettings()
      val defaultApp = defaults.appearance
      val defaultCustom = defaultApp.customColors

      val themeMode =
        prefs[Keys.DASHBOARD_THEME]?.let { raw ->
          DashboardThemeMode.entries.firstOrNull { it.name == raw }
        } ?: defaults.dashboardThemeMode

      val language =
        prefs[Keys.APP_LANGUAGE]?.let { raw ->
          AppLanguage.entries.firstOrNull { it.name == raw }
        } ?: defaults.language

      val pinned =
        prefs[Keys.PINNED_SHORTCUTS]
          ?.split("|")
          ?.map { it.trim() }
          ?.filter { it.isNotEmpty() }
          ?.distinct()
          ?.take(UserSettings.MAX_GRID_SHORTCUTS)
          .orEmpty()

      UserSettings(
        isOverlayEnabled = prefs[Keys.OVERLAY_ENABLED] ?: defaults.isOverlayEnabled,
        dashboardThemeMode = themeMode,
        language = language,
        appearance = readAppearance(prefs, defaultApp, defaultCustom),
        calendarEnabled = prefs[Keys.CALENDAR_ENABLED] ?: defaults.calendarEnabled,
        weatherEnabled = prefs[Keys.WEATHER_ENABLED] ?: defaults.weatherEnabled,
        shortcutsEnabled = prefs[Keys.SHORTCUTS_ENABLED] ?: defaults.shortcutsEnabled,
        timerActivityEnabled = prefs[Keys.TIMER_ENABLED] ?: defaults.timerActivityEnabled,
        stopwatchEnabled = prefs[Keys.STOPWATCH_ENABLED] ?: defaults.stopwatchEnabled,
        notificationActivityEnabled =
          prefs[Keys.NOTIF_ENABLED] ?: defaults.notificationActivityEnabled,
        mediaActivityEnabled = prefs[Keys.MEDIA_ENABLED] ?: defaults.mediaActivityEnabled,
        callActivityEnabled = prefs[Keys.CALL_ENABLED] ?: defaults.callActivityEnabled,
        batteryActivityEnabled = prefs[Keys.BATTERY_ENABLED] ?: defaults.batteryActivityEnabled,
        prayerAlarmEnabled = prefs[Keys.PRAYER_ENABLED] ?: defaults.prayerAlarmEnabled,
        hideTopDotEnabled = prefs[Keys.HIDE_TOP_DOT_ENABLED] ?: defaults.hideTopDotEnabled,
        showNotificationPreview =
          prefs[Keys.NOTIF_SHOW_PREVIEW] ?: defaults.showNotificationPreview,
        autoExpandOnNotification =
          prefs[Keys.NOTIF_AUTO_EXPAND] ?: defaults.autoExpandOnNotification,
        startOnBoot = prefs[Keys.START_ON_BOOT] ?: defaults.startOnBoot,
        cityName = prefs[Keys.CITY_NAME] ?: defaults.cityName,
        cityLatitude = prefs[Keys.CITY_LAT] ?: defaults.cityLatitude,
        cityLongitude = prefs[Keys.CITY_LON] ?: defaults.cityLongitude,
        useGpsLocation = prefs[Keys.USE_GPS] ?: defaults.useGpsLocation,
        pinnedShortcutPackages = pinned,
        shortcutSlots = List(UserSettings.SHORTCUT_SLOT_COUNT) { idx -> pinned.getOrNull(idx).orEmpty() },
        quickMenuItems =
          prefs[Keys.QUICK_MENU_ITEMS]
            ?.split("|")
            ?.mapNotNull { name -> QuickMenuItem.entries.firstOrNull { it.name == name } }
            ?.distinct()
            ?.takeIf { it.isNotEmpty() }
            ?: defaults.quickMenuItems,
        cachedWeatherTempC = prefs[Keys.WEATHER_TEMP],
        cachedWeatherCode = prefs[Keys.WEATHER_CODE],
        cachedWeatherCondition = prefs[Keys.WEATHER_COND],
        cachedWeatherWindKmh = prefs[Keys.WEATHER_WIND],
        cachedWeatherIsDay = prefs[Keys.WEATHER_IS_DAY] ?: defaults.cachedWeatherIsDay,
        cachedWeatherUpdatedAtMs = prefs[Keys.WEATHER_UPDATED] ?: defaults.cachedWeatherUpdatedAtMs,
      )
    }

  private fun readAppearance(
    prefs: Preferences,
    defaultApp: IslandAppearance,
    defaultCustom: IslandThemeColors,
  ): IslandAppearance {
    val preset =
      prefs[Keys.THEME_PRESET]?.let { name ->
        IslandThemePreset.entries.firstOrNull { it.name == name }
      } ?: defaultApp.themePreset
    val idleMode =
      prefs[Keys.IDLE_DISPLAY_MODE]?.let { raw -> IdleDisplayMode.fromPersisted(raw) }
        ?: defaultApp.idleDisplayMode
    val idlePlacement =
      prefs[Keys.IDLE_PLACEMENT]?.let { name ->
        IdlePlacement.entries.firstOrNull { it.name == name }
      } ?: defaultApp.idlePlacement
    val tapAction =
      prefs[Keys.IDLE_TAP_ACTION]?.let { name ->
        IdleTapAction.entries.firstOrNull { it.name == name }
      } ?: defaultApp.idleTapAction
    val expression =
      prefs[Keys.SELECTED_EXPRESSION]?.let { name ->
        ExpressionState.entries.firstOrNull { it.name == name }
      } ?: defaultApp.selectedExpression

    return IslandAppearance(
      widthDp = prefs[Keys.WIDTH_DP] ?: defaultApp.widthDp,
      heightDp = prefs[Keys.HEIGHT_DP] ?: defaultApp.heightDp,
      cornerRadiusDp = prefs[Keys.CORNER_RADIUS_DP] ?: defaultApp.cornerRadiusDp,
      topOffsetDp = prefs[Keys.TOP_OFFSET_DP] ?: defaultApp.topOffsetDp,
      horizontalOffsetDp = prefs[Keys.HORIZONTAL_OFFSET_DP] ?: defaultApp.horizontalOffsetDp,
      notificationWidthDp = prefs[Keys.NOTIF_WIDTH_DP] ?: defaultApp.notificationWidthDp,
      notificationMaxHeightDp =
        prefs[Keys.NOTIF_MAX_HEIGHT_DP] ?: defaultApp.notificationMaxHeightDp,
      notificationOpacity = prefs[Keys.NOTIF_OPACITY] ?: defaultApp.notificationOpacity,
      menuWidthDp = prefs[Keys.MENU_WIDTH_DP] ?: defaultApp.menuWidthDp,
      menuHeightDp = prefs[Keys.MENU_HEIGHT_DP] ?: defaultApp.menuHeightDp,
      animationEnabled = prefs[Keys.ANIMATION_ENABLED] ?: defaultApp.animationEnabled,
      animationSpeedMultiplier =
        prefs[Keys.ANIMATION_SPEED] ?: defaultApp.animationSpeedMultiplier,
      autoCollapseSeconds = prefs[Keys.AUTO_COLLAPSE_SECONDS] ?: defaultApp.autoCollapseSeconds,
      autoDimDelaySeconds = prefs[Keys.AUTO_DIM_DELAY_SECONDS] ?: defaultApp.autoDimDelaySeconds,
      themePreset = preset,
      customColors =
        IslandThemeColors(
          backgroundArgb = prefs[Keys.CUSTOM_BG] ?: defaultCustom.backgroundArgb,
          foregroundArgb = prefs[Keys.CUSTOM_FG] ?: defaultCustom.foregroundArgb,
          accentArgb = prefs[Keys.CUSTOM_ACCENT] ?: defaultCustom.accentArgb,
          iconArgb = prefs[Keys.CUSTOM_ICON] ?: defaultCustom.iconArgb,
        ),
      idleDisplayMode = idleMode,
      idlePlacement = idlePlacement,
      idleTapAction = tapAction,
      selectedExpression = expression,
      autoDimEnabled = prefs[Keys.AUTO_DIM_ENABLED] ?: defaultApp.autoDimEnabled,
      dimmedOpacity = prefs[Keys.DIMMED_OPACITY] ?: defaultApp.dimmedOpacity,
    )
  }

  override suspend fun setOverlayEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.OVERLAY_ENABLED] = enabled }
  }

  override suspend fun setDashboardThemeMode(mode: DashboardThemeMode) {
    context.dynaDataStore.edit { it[Keys.DASHBOARD_THEME] = mode.name }
  }

  override suspend fun setLanguage(language: AppLanguage) {
    context.dynaDataStore.edit { it[Keys.APP_LANGUAGE] = language.name }
  }

  override suspend fun updateAppearance(transform: (IslandAppearance) -> IslandAppearance) {
    context.dynaDataStore.edit { prefs ->
      val defaultApp = IslandAppearance()
      val current = readAppearance(prefs, defaultApp, defaultApp.customColors)
      val updated = transform(current)
      prefs[Keys.WIDTH_DP] = updated.widthDp
      prefs[Keys.HEIGHT_DP] = updated.heightDp
      prefs[Keys.CORNER_RADIUS_DP] = updated.cornerRadiusDp
      prefs[Keys.TOP_OFFSET_DP] = updated.topOffsetDp
      prefs[Keys.HORIZONTAL_OFFSET_DP] = updated.horizontalOffsetDp
      prefs[Keys.NOTIF_WIDTH_DP] = updated.notificationWidthDp
      prefs[Keys.NOTIF_MAX_HEIGHT_DP] = updated.notificationMaxHeightDp
      prefs[Keys.NOTIF_OPACITY] = updated.notificationOpacity
      prefs[Keys.MENU_WIDTH_DP] = updated.menuWidthDp
      prefs[Keys.MENU_HEIGHT_DP] = updated.menuHeightDp
      prefs[Keys.ANIMATION_ENABLED] = updated.animationEnabled
      prefs[Keys.ANIMATION_SPEED] = updated.animationSpeedMultiplier
      prefs[Keys.AUTO_COLLAPSE_SECONDS] = updated.autoCollapseSeconds
      prefs[Keys.AUTO_DIM_DELAY_SECONDS] = updated.autoDimDelaySeconds
      prefs[Keys.THEME_PRESET] = updated.themePreset.name
      prefs[Keys.IDLE_DISPLAY_MODE] = updated.idleDisplayMode.name
      prefs[Keys.IDLE_PLACEMENT] = updated.idlePlacement.name
      prefs[Keys.IDLE_TAP_ACTION] = updated.idleTapAction.name
      prefs[Keys.SELECTED_EXPRESSION] = updated.selectedExpression.name
      prefs[Keys.AUTO_DIM_ENABLED] = updated.autoDimEnabled
      prefs[Keys.DIMMED_OPACITY] = updated.dimmedOpacity
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

  override suspend fun setCalendarEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.CALENDAR_ENABLED] = enabled }
  }

  override suspend fun setWeatherEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.WEATHER_ENABLED] = enabled }
  }

  override suspend fun setShortcutsEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.SHORTCUTS_ENABLED] = enabled }
  }

  override suspend fun setTimerActivityEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.TIMER_ENABLED] = enabled }
  }

  override suspend fun setStopwatchEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.STOPWATCH_ENABLED] = enabled }
  }

  override suspend fun setNotificationActivityEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.NOTIF_ENABLED] = enabled }
  }

  override suspend fun setMediaActivityEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.MEDIA_ENABLED] = enabled }
  }

  override suspend fun setCallActivityEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.CALL_ENABLED] = enabled }
  }

  override suspend fun setBatteryActivityEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.BATTERY_ENABLED] = enabled }
  }

  override suspend fun setPrayerAlarmEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.PRAYER_ENABLED] = enabled }
  }

  override suspend fun setHideTopDotEnabled(enabled: Boolean) {
    context.dynaDataStore.edit { it[Keys.HIDE_TOP_DOT_ENABLED] = enabled }
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

  override suspend fun updateLocation(
    cityName: String,
    latitude: Double,
    longitude: Double,
    useGpsLocation: Boolean,
  ) {
    context.dynaDataStore.edit { prefs ->
      prefs[Keys.CITY_NAME] = cityName
      prefs[Keys.CITY_LAT] = latitude
      prefs[Keys.CITY_LON] = longitude
      prefs[Keys.USE_GPS] = useGpsLocation
    }
  }

  override suspend fun setPinnedShortcuts(packages: List<String>) {
    context.dynaDataStore.edit { prefs ->
      val cleaned = packages.map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(UserSettings.MAX_GRID_SHORTCUTS)
      prefs[Keys.PINNED_SHORTCUTS] = cleaned.joinToString("|")
      prefs[Keys.SHORTCUT_SLOTS] = cleaned.take(UserSettings.SHORTCUT_SLOT_COUNT).joinToString("|")
    }
  }

  override suspend fun addShortcut(packageName: String) {
    val clean = packageName.trim()
    if (clean.isEmpty()) return
    context.dynaDataStore.edit { prefs ->
      val current = readPinnedList(prefs).toMutableList()
      if (!current.contains(clean) && current.size < UserSettings.MAX_GRID_SHORTCUTS) {
        current.add(clean)
        prefs[Keys.PINNED_SHORTCUTS] = current.joinToString("|")
      }
    }
  }

  override suspend fun removeShortcut(packageName: String) {
    context.dynaDataStore.edit { prefs ->
      val updated = readPinnedList(prefs).filterNot { it == packageName }
      prefs[Keys.PINNED_SHORTCUTS] = updated.joinToString("|")
    }
  }

  override suspend fun reorderShortcut(fromIndex: Int, toIndex: Int) {
    context.dynaDataStore.edit { prefs ->
      val current = readPinnedList(prefs).toMutableList()
      if (fromIndex !in current.indices || toIndex !in current.indices || fromIndex == toIndex) {
        return@edit
      }
      val item = current.removeAt(fromIndex)
      current.add(toIndex, item)
      prefs[Keys.PINNED_SHORTCUTS] = current.joinToString("|")
    }
  }

  override suspend fun setShortcutSlot(slotIndex: Int, packageName: String?) {
    if (slotIndex !in 0 until UserSettings.MAX_GRID_SHORTCUTS) return
    context.dynaDataStore.edit { prefs ->
      val current = readPinnedList(prefs).toMutableList()
      val cleanPkg = packageName?.trim().orEmpty()
      if (cleanPkg.isEmpty()) {
        if (slotIndex in current.indices) {
          current.removeAt(slotIndex)
        }
      } else {
        current.removeAll { it == cleanPkg }
        val targetIdx = slotIndex.coerceIn(0, current.size)
        if (current.size < UserSettings.MAX_GRID_SHORTCUTS) {
          current.add(targetIdx, cleanPkg)
        }
      }
      prefs[Keys.PINNED_SHORTCUTS] = current.joinToString("|")
    }
  }

  private fun readPinnedList(prefs: Preferences): List<String> =
    prefs[Keys.PINNED_SHORTCUTS]
      ?.split("|")
      ?.map { it.trim() }
      ?.filter { it.isNotEmpty() }
      ?.distinct()
      ?.take(UserSettings.MAX_GRID_SHORTCUTS)
      .orEmpty()

  override suspend fun setQuickMenuItems(items: List<QuickMenuItem>) {
    val sanitized = items.distinct().ifEmpty { QuickMenuItem.defaultItems }
    context.dynaDataStore.edit { prefs ->
      prefs[Keys.QUICK_MENU_ITEMS] = sanitized.joinToString("|") { it.name }
    }
  }

  override suspend fun cacheWeather(weather: WeatherSnapshot) {
    context.dynaDataStore.edit { prefs ->
      weather.temperatureC?.let { prefs[Keys.WEATHER_TEMP] = it }
      weather.weatherCode?.let { prefs[Keys.WEATHER_CODE] = it }
      prefs[Keys.WEATHER_COND] = weather.conditionLabel
      weather.windKmh?.let { prefs[Keys.WEATHER_WIND] = it }
      prefs[Keys.WEATHER_IS_DAY] = weather.isDay
      prefs[Keys.WEATHER_UPDATED] = weather.updatedAtMs
    }
  }

  override suspend fun resetAppearance() {
    val defaultApp = IslandAppearance()
    context.dynaDataStore.edit { prefs ->
      prefs[Keys.WIDTH_DP] = defaultApp.widthDp
      prefs[Keys.HEIGHT_DP] = defaultApp.heightDp
      prefs[Keys.CORNER_RADIUS_DP] = defaultApp.cornerRadiusDp
      prefs[Keys.TOP_OFFSET_DP] = defaultApp.topOffsetDp
      prefs[Keys.HORIZONTAL_OFFSET_DP] = defaultApp.horizontalOffsetDp
      prefs[Keys.NOTIF_WIDTH_DP] = defaultApp.notificationWidthDp
      prefs[Keys.NOTIF_MAX_HEIGHT_DP] = defaultApp.notificationMaxHeightDp
      prefs[Keys.NOTIF_OPACITY] = defaultApp.notificationOpacity
      prefs[Keys.MENU_WIDTH_DP] = defaultApp.menuWidthDp
      prefs[Keys.MENU_HEIGHT_DP] = defaultApp.menuHeightDp
      prefs[Keys.ANIMATION_ENABLED] = defaultApp.animationEnabled
      prefs[Keys.ANIMATION_SPEED] = defaultApp.animationSpeedMultiplier
      prefs[Keys.AUTO_COLLAPSE_SECONDS] = defaultApp.autoCollapseSeconds
      prefs[Keys.AUTO_DIM_DELAY_SECONDS] = defaultApp.autoDimDelaySeconds
      prefs[Keys.IDLE_DISPLAY_MODE] = defaultApp.idleDisplayMode.name
      prefs[Keys.IDLE_PLACEMENT] = defaultApp.idlePlacement.name
      prefs[Keys.IDLE_TAP_ACTION] = defaultApp.idleTapAction.name
      prefs[Keys.SELECTED_EXPRESSION] = defaultApp.selectedExpression.name
      prefs[Keys.AUTO_DIM_ENABLED] = defaultApp.autoDimEnabled
      prefs[Keys.DIMMED_OPACITY] = defaultApp.dimmedOpacity
    }
  }

  override suspend fun resetAllSettings() {
    context.dynaDataStore.edit { it.clear() }
  }
}
