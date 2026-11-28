package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AppLanguage
import com.example.domain.model.ExpressionState
import com.example.domain.model.IdleDisplayMode
import com.example.domain.model.IdlePlacement
import com.example.domain.model.IslandAppearance
import com.example.domain.model.PrayerSchedule
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.QuickMenuItem
import com.example.domain.model.ShortcutAppItem
import com.example.domain.model.UserSettings
import com.example.domain.model.WeatherSnapshot
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.DynaPalette
import com.example.ui.theme.LocalDynaStrings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

fun quickMenuItemIcon(item: QuickMenuItem): ImageVector =
  when (item) {
    QuickMenuItem.CALENDAR -> Icons.Filled.CalendarMonth
    QuickMenuItem.WEATHER -> Icons.Filled.WbSunny
    QuickMenuItem.PRAYER -> Icons.Filled.Mosque
    QuickMenuItem.SHORTCUTS -> Icons.Filled.Apps
    QuickMenuItem.MOOD_PICKER -> Icons.Filled.Face
    QuickMenuItem.TIMER_1M -> Icons.Filled.Alarm
    QuickMenuItem.STOPWATCH -> Icons.Filled.Timer
    QuickMenuItem.HIDE_ISLAND -> Icons.Filled.VisibilityOff
  }

@Composable
fun BelowIslandIconMenuDock(
  items: List<QuickMenuItem>,
  bgColor: Color,
  fgColor: Color,
  accentColor: Color,
  onSelectItem: (QuickMenuItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val resolvedItems = items.ifEmpty { QuickMenuItem.defaultItems }
  Surface(
    modifier = modifier.testTag("quick_menu_dock"),
    shape = RoundedCornerShape(24.dp),
    color = bgColor.copy(alpha = 0.96f),
    border = BorderStroke(1.dp, DynaPalette.IslandBorder),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      resolvedItems.chunked(5).forEach { rowItems ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(2.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          rowItems.forEach { item ->
            val iconTint =
              if (item == QuickMenuItem.HIDE_ISLAND) DynaPalette.IslandTextSecondary
              else accentColor
            Box(
              modifier =
                Modifier.size(48.dp)
                  .clip(CircleShape)
                  .clickable { onSelectItem(item) }
                  .testTag("quick_menu_item_${item.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              Box(
                modifier =
                  Modifier.size(34.dp)
                    .clip(CircleShape)
                    .background(DynaPalette.IslandSurface)
                    .border(1.dp, DynaPalette.IslandBorder, CircleShape),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = quickMenuItemIcon(item),
                  contentDescription = item.label,
                  tint = iconTint,
                  modifier = Modifier.size(17.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun IdleQuickCategoryPanel(
  category: QuickMenuCategory,
  appearance: IslandAppearance,
  weather: WeatherSnapshot,
  prayerSchedule: PrayerSchedule,
  shortcutSlots: List<ShortcutAppItem?>,
  installedApps: List<ShortcutAppItem>,
  appIconProvider: (String) -> Bitmap? = { null },
  language: AppLanguage = AppLanguage.ID,
  hideTopDotEnabled: Boolean = true,
  fgColor: Color,
  accentColor: Color,
  onSelectCategory: (QuickMenuCategory) -> Unit,
  onCollapse: () -> Unit,
  onHideIsland: () -> Unit,
  onOpenCalendarDate: (LocalDate) -> Unit,
  onRefreshWeather: () -> Unit,
  onStartTimer: (Int) -> Unit,
  onStartStopwatch: () -> Unit,
  onLaunchShortcut: (String) -> Unit,
  onAssignShortcutSlot: (Int, String?) -> Unit,
  onOpenShortcutsSettings: () -> Unit,
  onSelectExpression: (ExpressionState) -> Unit,
  onSelectIdleMode: (IdleDisplayMode) -> Unit,
  onSelectIdlePlacement: (IdlePlacement) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier =
            Modifier.size(48.dp)
              .clip(CircleShape)
              .clickable { onSelectCategory(QuickMenuCategory.MENU) }
              .testTag("pill_menu_back"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Kembali",
            tint = fgColor,
            modifier = Modifier.size(18.dp),
          )
        }
        DynamicIslandFace(
          expression = appearance.selectedExpression,
          featureColor = fgColor,
          accentColor = accentColor,
          animationEnabled = appearance.animationEnabled,
          sizeDp = 20.dp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = category.localizedLabel(language),
          style = MaterialTheme.typography.titleMedium,
          color = fgColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (hideTopDotEnabled) {
          Box(
            modifier =
              Modifier.size(48.dp)
                .clip(CircleShape)
                .clickable(onClick = onHideIsland)
                .testTag("pill_panel_hide_island"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.VisibilityOff,
              contentDescription = LocalDynaStrings.current.pillHideIsland,
              tint = DynaPalette.IslandTextSecondary,
              modifier = Modifier.size(17.dp),
            )
          }
        }
        Box(
          modifier =
            Modifier.size(48.dp)
              .clip(CircleShape)
              .clickable(onClick = onCollapse)
              .testTag("pill_quick_menu_close"),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = LocalDynaStrings.current.closeAction,
            tint = DynaPalette.IslandTextSecondary,
            modifier = Modifier.size(18.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(4.dp))

    when (category) {
      QuickMenuCategory.CALENDAR ->
        IslandCalendarContent(
          fgColor = fgColor,
          accentColor = accentColor,
          language = language,
          onOpenCalendarDate = onOpenCalendarDate,
        )
      QuickMenuCategory.WEATHER ->
        IslandWeatherContent(
          weather = weather,
          fgColor = fgColor,
          accentColor = accentColor,
          onRefreshWeather = onRefreshWeather,
        )
      QuickMenuCategory.PRAYER ->
        IslandPrayerContent(
          schedule = prayerSchedule,
          fgColor = fgColor,
          accentColor = accentColor,
        )
      QuickMenuCategory.SHORTCUTS ->
        IslandShortcutsContent(
          shortcuts = shortcutSlots.filterNotNull(),
          appIconProvider = appIconProvider,
          fgColor = fgColor,
          onLaunchShortcut = onLaunchShortcut,
          onOpenShortcutsSettings = onOpenShortcutsSettings,
        )
      QuickMenuCategory.MOOD_PICKER ->
        IslandMoodPickerContent(
          appearance = appearance,
          language = language,
          hideTopDotEnabled = hideTopDotEnabled,
          fgColor = fgColor,
          accentColor = accentColor,
          onSelectExpression = onSelectExpression,
          onSelectIdleMode = onSelectIdleMode,
          onSelectIdlePlacement = onSelectIdlePlacement,
          onHideIsland = onHideIsland,
        )
      QuickMenuCategory.MENU,
      QuickMenuCategory.NONE -> Unit
    }
  }
}

@Composable
private fun IslandCalendarContent(
  fgColor: Color,
  accentColor: Color,
  language: AppLanguage,
  onOpenCalendarDate: (LocalDate) -> Unit,
) {
  val strings = LocalDynaStrings.current
  val today = remember { LocalDate.now() }
  var focusedDate by remember { mutableStateOf(today) }

  val locale = if (language == AppLanguage.ID) Locale("id", "ID") else Locale.ENGLISH
  val monthLabel = focusedDate.month.getDisplayName(TextStyle.FULL, locale).uppercase()
  val shortMonth = focusedDate.month.getDisplayName(TextStyle.SHORT, locale).uppercase()

  // Weekly strip: Sunday (Su) -> Saturday (Sa)
  val weekStart = focusedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
  val weekDays = List(7) { offset -> weekStart.plusDays(offset.toLong()) }
  val dayHeaders = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "$monthLabel ${focusedDate.year}",
        style = DynaMonoTypography.LabelMono.copy(fontSize = 11.sp),
        color = fgColor,
      )
      if (focusedDate != today) {
        Surface(
          modifier =
            Modifier.clip(CircleShape).clickable { focusedDate = today },
          shape = CircleShape,
          color = DynaPalette.IslandSurface,
          border = BorderStroke(1.dp, DynaPalette.IslandBorder),
        ) {
          Text(
            text = if (language == AppLanguage.ID) "Hari Ini" else "Today",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = fgColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(16.dp),
      color = DynaPalette.IslandSurface,
      border = BorderStroke(1.dp, DynaPalette.IslandBorder),
    ) {
      Column(modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
          dayHeaders.forEach { d ->
            Text(
              text = d,
              style = DynaMonoTypography.LabelMono.copy(fontSize = 10.sp),
              color = DynaPalette.IslandTextSecondary,
              textAlign = TextAlign.Center,
              modifier = Modifier.weight(1f),
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          weekDays.forEach { date ->
            val isActive = date == focusedDate || date == today && focusedDate == today
            Box(
              modifier =
                Modifier.weight(1f)
                  .heightIn(min = 38.dp)
                  .clickable {
                    focusedDate = date
                    onOpenCalendarDate(date)
                  }
                  .testTag("calendar_day_${date.dayOfMonth}"),
              contentAlignment = Alignment.Center,
            ) {
              Box(
                modifier =
                  Modifier.size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive) DynaPalette.IslandTextPrimary else Color.Transparent),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = date.dayOfMonth.toString(),
                  style =
                    DynaMonoTypography.LabelMono.copy(
                      fontSize = 12.sp,
                      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    ),
                  color = if (isActive) DynaPalette.IslandBg else fgColor,
                )
              }
            }
          }
        }
      }
    }

    // Month & Year switchers positioned BELOW the calendar strip
    Spacer(modifier = Modifier.height(8.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      CalendarStepControl(
        label = shortMonth,
        onPrev = { focusedDate = focusedDate.minusMonths(1) },
        onNext = { focusedDate = focusedDate.plusMonths(1) },
        fgColor = fgColor,
        accentColor = accentColor,
        prevCd = strings.pillCalendarMonthPrev,
        nextCd = strings.pillCalendarMonthNext,
        prevTag = "calendar_prev_month",
        nextTag = "calendar_next_month",
        modifier = Modifier.weight(1f),
      )
      CalendarStepControl(
        label = focusedDate.year.toString(),
        onPrev = { focusedDate = focusedDate.minusYears(1) },
        onNext = { focusedDate = focusedDate.plusYears(1) },
        fgColor = fgColor,
        accentColor = accentColor,
        prevCd = strings.pillCalendarYearPrev,
        nextCd = strings.pillCalendarYearNext,
        prevTag = "calendar_prev_year",
        nextTag = "calendar_next_year",
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun CalendarStepControl(
  label: String,
  onPrev: () -> Unit,
  onNext: () -> Unit,
  fgColor: Color,
  accentColor: Color,
  prevCd: String,
  nextCd: String,
  prevTag: String,
  nextTag: String,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.heightIn(min = 48.dp),
    shape = RoundedCornerShape(12.dp),
    color = DynaPalette.IslandSurface,
    border = BorderStroke(1.dp, DynaPalette.IslandBorder),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Box(
        modifier =
          Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onPrev).testTag(prevTag),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = prevCd,
          tint = accentColor,
          modifier = Modifier.size(18.dp),
        )
      }
      Text(
        text = label,
        style = DynaMonoTypography.LabelMono.copy(fontSize = 11.sp),
        color = fgColor,
        maxLines = 1,
      )
      Box(
        modifier =
          Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onNext).testTag(nextTag),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = nextCd,
          tint = accentColor,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun IslandWeatherContent(
  weather: WeatherSnapshot,
  fgColor: Color,
  accentColor: Color,
  onRefreshWeather: () -> Unit,
) {
  val strings = LocalDynaStrings.current
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = weather.cityName.uppercase(),
        style = DynaMonoTypography.LabelMono,
        color = DynaPalette.IslandTextSecondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text =
          weather.temperatureC?.let { "${it.roundToInt()}°C" }
            ?: if (weather.isLoading) "..." else "--°C",
        style = DynaMonoTypography.TabularTimer,
        color = fgColor,
      )
      Text(
        text =
          weather.errorMessage
            ?: buildString {
              append(weather.conditionLabel)
              weather.windKmh?.let { append(" • ${it.roundToInt()} km/j") }
            },
        style = MaterialTheme.typography.bodySmall,
        color = DynaPalette.IslandTextSecondary,
        maxLines = 2,
      )
    }

    Box(
      modifier =
        Modifier.size(48.dp)
          .clip(CircleShape)
          .background(DynaPalette.IslandSurface)
          .border(1.dp, DynaPalette.IslandBorder, CircleShape)
          .clickable(onClick = onRefreshWeather)
          .testTag("pill_weather_refresh"),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.Refresh,
        contentDescription = strings.pillWeatherRefresh,
        tint = accentColor,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

@Composable
private fun IslandPrayerContent(
  schedule: PrayerSchedule,
  fgColor: Color,
  accentColor: Color,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "${schedule.cityName.uppercase()} • ${schedule.dateLabel}",
        style = DynaMonoTypography.LabelMono.copy(fontSize = 10.sp),
        color = DynaPalette.IslandTextSecondary,
      )
      schedule.nextPrayer?.let { next ->
        Text(
          text = "${next.name.label} ${schedule.countdownLabel.orEmpty()}",
          style = MaterialTheme.typography.bodySmall,
          color = fgColor,
        )
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      schedule.entries.forEach { entry ->
        val isNext = schedule.nextPrayer?.name == entry.name
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          color = DynaPalette.IslandSurface,
          border =
            BorderStroke(
              1.dp,
              if (isNext) accentColor else DynaPalette.IslandBorder,
            ),
        ) {
          Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = entry.name.label.take(3).uppercase(),
              style = DynaMonoTypography.LabelMono.copy(fontSize = 9.sp),
              color = if (isNext) fgColor else DynaPalette.IslandTextSecondary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = entry.formattedTime,
              style = DynaMonoTypography.LabelCode.copy(fontSize = 11.sp),
              color = fgColor,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun IslandShortcutsContent(
  shortcuts: List<ShortcutAppItem>,
  appIconProvider: (String) -> Bitmap?,
  fgColor: Color,
  onLaunchShortcut: (String) -> Unit,
  onOpenShortcutsSettings: () -> Unit,
) {
  val strings = LocalDynaStrings.current
  val capped = shortcuts.take(UserSettings.MAX_GRID_SHORTCUTS)
  if (capped.isEmpty()) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = strings.pillShortcutsEmpty,
        style = MaterialTheme.typography.bodySmall,
        color = DynaPalette.IslandTextSecondary,
        textAlign = TextAlign.Center,
      )
      Surface(
        modifier =
          Modifier.heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOpenShortcutsSettings)
            .testTag("pill_open_shortcuts_settings"),
        shape = RoundedCornerShape(12.dp),
        color = DynaPalette.IslandSurface,
        border = BorderStroke(1.dp, DynaPalette.IslandBorder),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = fgColor,
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = strings.pillShortcutsManage,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = fgColor,
          )
        }
      }
    }
    return
  }

  val pageCount = if (capped.size > 8) 2 else 1
  val pagerState = rememberPagerState(pageCount = { pageCount })

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth().testTag("pill_shortcuts_pager"),
    ) { page ->
      val pageItems = capped.drop(page * 8).take(8)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        for (row in 0 until 2) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            for (col in 0 until 4) {
              val item = pageItems.getOrNull(row * 4 + col)
              if (item != null) {
                val iconBitmap = remember(item.packageName) { appIconProvider(item.packageName) }
                Column(
                  modifier =
                    Modifier.weight(1f)
                      .clip(RoundedCornerShape(12.dp))
                      .background(DynaPalette.IslandSurface)
                      .border(1.dp, DynaPalette.IslandBorder, RoundedCornerShape(12.dp))
                      .clickable { onLaunchShortcut(item.packageName) }
                      .padding(vertical = 8.dp, horizontal = 4.dp)
                      .testTag("pill_shortcut_item_${item.packageName}"),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  if (iconBitmap != null) {
                    Image(
                      bitmap = iconBitmap.asImageBitmap(),
                      contentDescription = item.label,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)),
                    )
                  } else {
                    Box(
                      modifier =
                        Modifier.size(28.dp)
                          .clip(RoundedCornerShape(8.dp))
                          .background(DynaPalette.IslandBg),
                      contentAlignment = Alignment.Center,
                    ) {
                      Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = item.label,
                        tint = fgColor,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = fgColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                  )
                }
              } else {
                Spacer(modifier = Modifier.weight(1f).height(62.dp))
              }
            }
          }
        }
      }
    }

    if (pageCount > 1) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        repeat(pageCount) { idx ->
          val active = pagerState.currentPage == idx
          Box(
            modifier =
              Modifier.padding(horizontal = 3.dp)
                .size(if (active) 6.dp else 5.dp)
                .clip(CircleShape)
                .background(
                  if (active) fgColor else DynaPalette.IslandTextSecondary.copy(alpha = 0.4f)
                )
          )
        }
      }
    }
  }
}

@Composable
private fun IslandMoodPickerContent(
  appearance: IslandAppearance,
  language: AppLanguage,
  hideTopDotEnabled: Boolean,
  fgColor: Color,
  accentColor: Color,
  onSelectExpression: (ExpressionState) -> Unit,
  onSelectIdleMode: (IdleDisplayMode) -> Unit,
  onSelectIdlePlacement: (IdlePlacement) -> Unit,
  onHideIsland: () -> Unit,
) {
  val strings = LocalDynaStrings.current
  val quickMoods =
    listOf(
      ExpressionState.SMILE,
      ExpressionState.HAPPY,
      ExpressionState.HEART_EYES,
      ExpressionState.SUNGLASSES,
      ExpressionState.WINK,
      ExpressionState.XD_LAUGH,
      ExpressionState.SLEEPY_ZZZ,
      ExpressionState.TONGUE_OUT,
      ExpressionState.DEVILISH,
      ExpressionState.KO_X,
    )

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      quickMoods.forEach { mood ->
        val selected = appearance.selectedExpression == mood
        Box(
          modifier =
            Modifier.size(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(DynaPalette.IslandSurface)
              .border(
                1.dp,
                if (selected) accentColor else DynaPalette.IslandBorder,
                RoundedCornerShape(12.dp),
              )
              .clickable { onSelectExpression(mood) }
              .testTag("pill_mood_${mood.name.lowercase()}"),
          contentAlignment = Alignment.Center,
        ) {
          DynamicIslandFace(
            expression = mood,
            featureColor = fgColor,
            accentColor = accentColor,
            animationEnabled = false,
            sizeDp = 24.dp,
          )
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      IdleDisplayMode.entries.forEach { mode ->
        val selected = appearance.idleDisplayMode == mode
        Surface(
          modifier =
            Modifier.weight(1f)
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onSelectIdleMode(mode) },
          shape = RoundedCornerShape(10.dp),
          color = DynaPalette.IslandSurface,
          border = BorderStroke(1.dp, if (selected) accentColor else DynaPalette.IslandBorder),
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
              text = mode.localizedLabel(language),
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
              color = fgColor,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
      IdlePlacement.entries.forEach { placement ->
        val selected = appearance.idlePlacement == placement
        Surface(
          modifier =
            Modifier.weight(1f)
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(10.dp))
              .clickable { onSelectIdlePlacement(placement) },
          shape = RoundedCornerShape(10.dp),
          color = DynaPalette.IslandSurface,
          border = BorderStroke(1.dp, if (selected) accentColor else DynaPalette.IslandBorder),
        ) {
          Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp)) {
            Text(
              text = placement.label,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
              color = fgColor,
              maxLines = 1,
            )
          }
        }
      }
      if (hideTopDotEnabled) {
        Surface(
          modifier =
            Modifier.weight(1f)
              .heightIn(min = 48.dp)
              .clip(RoundedCornerShape(10.dp))
              .clickable(onClick = onHideIsland)
              .testTag("pill_hide_island_button"),
          shape = RoundedCornerShape(10.dp),
          color = DynaPalette.IslandSurface,
          border = BorderStroke(1.dp, DynaPalette.IslandBorder),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.VisibilityOff,
              contentDescription = strings.pillHideIsland,
              tint = fgColor,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = strings.pillHideIsland,
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
              color = fgColor,
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}
