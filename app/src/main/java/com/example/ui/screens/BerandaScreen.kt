package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.NotificationHistoryEntry
import com.example.domain.model.ShortcutAppItem
import com.example.domain.model.UserSettings
import com.example.ui.components.BentoCard
import com.example.ui.components.DynaConfirmationDialog
import com.example.ui.theme.DynaMonoTypography
import com.example.ui.theme.LocalDynaStrings
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun BerandaScreen(
  uiState: DynaUiState,
  onAddShortcut: (String) -> Unit,
  onRemoveShortcut: (String) -> Unit,
  onReorderShortcut: (Int, Int) -> Unit,
  onDeleteHistoryEntry: (String) -> Unit,
  onClearHistory: () -> Unit,
  getAppIconBitmap: (String) -> Bitmap?,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current

  val orderedApps =
    remember(uiState.settings.pinnedShortcutPackages, uiState.settings.shortcutSlots, uiState.installedApps) {
      uiState.settings.orderedShortcuts().mapNotNull { pkg ->
        uiState.installedApps.firstOrNull { it.packageName == pkg }
          ?: ShortcutAppItem(packageName = pkg, label = pkg.substringAfterLast('.'))
      }
    }

  var showAddShortcutPicker by remember { mutableStateOf(false) }
  var shortcutPendingRemoval by remember { mutableStateOf<ShortcutAppItem?>(null) }
  var selectedHistoryDetail by remember { mutableStateOf<NotificationHistoryEntry?>(null) }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .statusBarsPadding()
        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = tokens.bottomNavClearance),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    Text(
      text = strings.homeTitle,
      style = MaterialTheme.typography.headlineLarge,
      color = tokens.textPrimary,
      modifier = Modifier.testTag("beranda_title"),
    )

    // Top Section: 2-Page 2x4 App Shortcuts Grid
    BentoCard(
      headerLabel = strings.shortcutsSectionTitle,
      headerAction = {
        Text(
          text = "${orderedApps.size}/${UserSettings.MAX_GRID_SHORTCUTS}",
          style = DynaMonoTypography.LabelMono,
          color = tokens.textSecondary,
        )
      },
    ) {
      AppShortcutsPagerGrid(
        orderedApps = orderedApps,
        getAppIconBitmap = getAppIconBitmap,
        onTapAdd = { showAddShortcutPicker = true },
        onTapAppToRemove = { app -> shortcutPendingRemoval = app },
        onReorderShortcut = onReorderShortcut,
      )
      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "${strings.shortcutsHint} ${strings.shortcutsReorderHint}",
        style = MaterialTheme.typography.bodySmall,
        color = tokens.textSecondary,
      )
    }

    // Bottom Section: Daily Local Notification History
    BentoCard(
      headerLabel = strings.historySectionTitle,
      headerAction = {
        if (uiState.notificationHistory.isNotEmpty()) {
          IconButton(
            onClick = onClearHistory,
            modifier = Modifier.size(48.dp).testTag("history_clear_all_button"),
          ) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = strings.historyDeleteItemCd,
              tint = tokens.textSecondary,
              modifier = Modifier.size(18.dp),
            )
          }
        }
      },
    ) {
      Text(
        text = strings.historySubtitle,
        style = MaterialTheme.typography.bodySmall,
        color = tokens.textSecondary,
      )
      Spacer(modifier = Modifier.height(12.dp))

      if (uiState.notificationHistory.isEmpty()) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 160.dp)
              .clip(RoundedCornerShape(tokens.radiusMd))
              .background(tokens.surfaceInset)
              .border(1.dp, tokens.borderHairline, RoundedCornerShape(tokens.radiusMd))
              .padding(20.dp)
              .testTag("history_empty_state"),
          contentAlignment = Alignment.Center,
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Filled.Notifications,
              contentDescription = null,
              tint = tokens.textMuted,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = strings.historyEmptyTitle,
              style = MaterialTheme.typography.titleMedium,
              color = tokens.textPrimary,
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = strings.historyEmptyDesc,
              style = MaterialTheme.typography.bodySmall,
              color = tokens.textSecondary,
              textAlign = TextAlign.Center,
            )
          }
        }
      } else {
        LazyColumn(
          modifier =
            Modifier.fillMaxWidth()
              .height(276.dp)
              .clip(RoundedCornerShape(tokens.radiusMd))
              .background(tokens.surfaceInset)
              .border(1.dp, tokens.borderHairline, RoundedCornerShape(tokens.radiusMd))
              .testTag("history_lazy_list"),
          contentPadding = PaddingValues(vertical = 4.dp),
        ) {
          items(uiState.notificationHistory, key = { it.id }) { entry ->
            NotificationHistoryItemRow(
              entry = entry,
              iconBitmap = remember(entry.packageName) { getAppIconBitmap(entry.packageName) },
              onClick = { selectedHistoryDetail = entry },
              onDelete = { onDeleteHistoryEntry(entry.id) },
            )
            HorizontalDivider(color = tokens.borderHairline, thickness = 1.dp)
          }
        }
      }
    }
  }

  if (showAddShortcutPicker) {
    val existingPackages = remember(orderedApps) { orderedApps.map { it.packageName }.toSet() }
    val candidates =
      remember(uiState.installedApps, existingPackages) {
        uiState.installedApps.filterNot { existingPackages.contains(it.packageName) }
      }
    ShortcutAppPickerDialog(
      apps = candidates,
      getAppIconBitmap = getAppIconBitmap,
      onSelectApp = { pkg ->
        onAddShortcut(pkg)
        showAddShortcutPicker = false
      },
      onDismiss = { showAddShortcutPicker = false },
    )
  }

  shortcutPendingRemoval?.let { app ->
    DynaConfirmationDialog(
      title = strings.shortcutsRemoveTitle,
      body = "${app.label}: ${strings.shortcutsRemoveConfirm}",
      confirmLabel = strings.removeAction,
      cancelLabel = strings.cancelAction,
      onConfirm = {
        onRemoveShortcut(app.packageName)
        shortcutPendingRemoval = null
      },
      onDismiss = { shortcutPendingRemoval = null },
    )
  }

  selectedHistoryDetail?.let { detail ->
    NotificationHistoryDetailDialog(
      entry = detail,
      onDismiss = { selectedHistoryDetail = null },
    )
  }
}

@Composable
private fun AppShortcutsPagerGrid(
  orderedApps: List<ShortcutAppItem>,
  getAppIconBitmap: (String) -> Bitmap?,
  onTapAdd: () -> Unit,
  onTapAppToRemove: (ShortcutAppItem) -> Unit,
  onReorderShortcut: (Int, Int) -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  val haptic = LocalHapticFeedback.current
  val density = LocalDensity.current

  val hasAddTile = orderedApps.size < UserSettings.MAX_GRID_SHORTCUTS
  val totalVisibleTiles = orderedApps.size + if (hasAddTile) 1 else 0
  val pageCount = if (totalVisibleTiles > 8) 2 else 1
  val pagerState = rememberPagerState(pageCount = { pageCount })

  var draggedGlobalIndex by remember { mutableStateOf<Int?>(null) }
  var dragOffsetX by remember { mutableFloatStateOf(0f) }
  var dragOffsetY by remember { mutableFloatStateOf(0f) }

  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxWidth().testTag("beranda_shortcuts_pager"),
    ) { page ->
      val pageOffset = page * 8
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        for (row in 0 until 2) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            for (col in 0 until 4) {
              val globalIndex = pageOffset + row * 4 + col
              val app = orderedApps.getOrNull(globalIndex)
              when {
                app != null -> {
                  val isDragged = draggedGlobalIndex == globalIndex
                  val iconBitmap = remember(app.packageName) { getAppIconBitmap(app.packageName) }
                  Surface(
                    modifier =
                      Modifier.weight(1f)
                        .heightIn(min = 76.dp)
                        .graphicsLayer {
                          if (isDragged) {
                            translationX = dragOffsetX
                            translationY = dragOffsetY
                            scaleX = 1.06f
                            scaleY = 1.06f
                          }
                        }
                        .clip(RoundedCornerShape(tokens.radiusMd))
                        .pointerInput(globalIndex, orderedApps.size) {
                          val cellW = with(density) { 80.dp.toPx() }
                          val cellH = with(density) { 84.dp.toPx() }
                          detectDragGesturesAfterLongPress(
                            onDragStart = {
                              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                              draggedGlobalIndex = globalIndex
                              dragOffsetX = 0f
                              dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                              change.consume()
                              dragOffsetX += dragAmount.x
                              dragOffsetY += dragAmount.y
                            },
                            onDragCancel = {
                              draggedGlobalIndex = null
                              dragOffsetX = 0f
                              dragOffsetY = 0f
                            },
                            onDragEnd = {
                              val colDelta = (dragOffsetX / cellW).roundToInt()
                              val rowDelta = (dragOffsetY / cellH).roundToInt()
                              val targetIdx =
                                (globalIndex + rowDelta * 4 + colDelta).coerceIn(
                                  0,
                                  (orderedApps.size - 1).coerceAtLeast(0),
                                )
                              if (targetIdx != globalIndex) {
                                onReorderShortcut(globalIndex, targetIdx)
                              }
                              draggedGlobalIndex = null
                              dragOffsetX = 0f
                              dragOffsetY = 0f
                            },
                          )
                        }
                        .clickable { onTapAppToRemove(app) }
                        .testTag("beranda_shortcut_tile_$globalIndex"),
                    shape = RoundedCornerShape(tokens.radiusMd),
                    color = tokens.surfaceRaised,
                    border =
                      BorderStroke(
                        1.dp,
                        if (isDragged) tokens.accentPrimary else tokens.borderHairline,
                      ),
                  ) {
                    Column(
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center,
                    ) {
                      if (iconBitmap != null) {
                        Image(
                          bitmap = iconBitmap.asImageBitmap(),
                          contentDescription = app.label,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)),
                        )
                      } else {
                        Box(
                          modifier =
                            Modifier.size(34.dp)
                              .clip(RoundedCornerShape(9.dp))
                              .background(tokens.surfaceInset),
                          contentAlignment = Alignment.Center,
                        ) {
                          Icon(
                            imageVector = Icons.Filled.Apps,
                            contentDescription = app.label,
                            tint = tokens.textPrimary,
                            modifier = Modifier.size(18.dp),
                          )
                        }
                      }
                      Spacer(modifier = Modifier.height(6.dp))
                      Text(
                        text = app.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                      )
                    }
                  }
                }
                hasAddTile && globalIndex == orderedApps.size -> {
                  Surface(
                    modifier =
                      Modifier.weight(1f)
                        .heightIn(min = 76.dp)
                        .clip(RoundedCornerShape(tokens.radiusMd))
                        .clickable(onClick = onTapAdd)
                        .testTag("beranda_shortcut_add_button"),
                    shape = RoundedCornerShape(tokens.radiusMd),
                    color = tokens.surfaceInset,
                    border = BorderStroke(1.dp, tokens.borderStrong),
                  ) {
                    Column(
                      modifier = Modifier.padding(6.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center,
                    ) {
                      Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = strings.shortcutsAddLabel,
                        tint = tokens.textPrimary,
                        modifier = Modifier.size(22.dp),
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                        text = strings.shortcutsAddLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = tokens.textSecondary,
                        maxLines = 1,
                      )
                    }
                  }
                }
                else -> {
                  Spacer(modifier = Modifier.weight(1f).height(76.dp))
                }
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
              Modifier.padding(horizontal = 4.dp)
                .size(if (active) 7.dp else 6.dp)
                .clip(CircleShape)
                .background(if (active) tokens.accentPrimary else tokens.borderStrong)
          )
        }
      }
    }
  }
}

@Composable
private fun NotificationHistoryItemRow(
  entry: NotificationHistoryEntry,
  iconBitmap: Bitmap?,
  onClick: () -> Unit,
  onDelete: () -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  val formattedTime =
    remember(entry.timestampMs) {
      runCatching {
          Instant.ofEpochMilli(entry.timestampMs)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        .getOrDefault("--:--")
    }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 58.dp)
        .clickable(onClick = onClick)
        .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp)
        .testTag("history_item_${entry.id}"),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (iconBitmap != null) {
      Image(
        bitmap = iconBitmap.asImageBitmap(),
        contentDescription = entry.appName,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)),
      )
    } else {
      Box(
        modifier =
          Modifier.size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tokens.surfaceRaised)
            .border(1.dp, tokens.borderHairline, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Notifications,
          contentDescription = entry.appName,
          tint = tokens.textSecondary,
          modifier = Modifier.size(16.dp),
        )
      }
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = entry.appName,
          style = DynaMonoTypography.LabelMono.copy(fontSize = 11.sp),
          color = tokens.textSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = formattedTime,
          style = DynaMonoTypography.LabelMono.copy(fontSize = 11.sp),
          color = tokens.textMuted,
        )
      }
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = entry.title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = tokens.textPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (entry.body.isNotBlank()) {
        Text(
          text = entry.body,
          style = MaterialTheme.typography.bodySmall,
          color = tokens.textSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    IconButton(
      onClick = onDelete,
      modifier = Modifier.size(48.dp).testTag("history_delete_${entry.id}"),
    ) {
      Icon(
        imageVector = Icons.Filled.Close,
        contentDescription = strings.historyDeleteItemCd,
        tint = tokens.textMuted,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@Composable
private fun ShortcutAppPickerDialog(
  apps: List<ShortcutAppItem>,
  getAppIconBitmap: (String) -> Bitmap?,
  onSelectApp: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  var query by remember { mutableStateOf("") }
  val filtered =
    remember(apps, query) {
      val trimmed = query.trim()
      if (trimmed.isEmpty()) apps
      else apps.filter { it.label.contains(trimmed, ignoreCase = true) }
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = tokens.surfaceCard,
    titleContentColor = tokens.textPrimary,
    title = { Text(text = strings.shortcutsPickerTitle, style = MaterialTheme.typography.titleLarge) },
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
                text = strings.shortcutsSearchHint,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.textMuted,
              )
            }
            BasicTextField(
              value = query,
              onValueChange = { query = it },
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyMedium.copy(color = tokens.textPrimary),
              cursorBrush = SolidColor(tokens.accentPrimary),
              modifier = Modifier.fillMaxWidth().testTag("shortcut_picker_search"),
            )
          }
        }

        if (filtered.isEmpty()) {
          Text(
            text = strings.shortcutsEmptyInstalled,
            style = MaterialTheme.typography.bodySmall,
            color = tokens.textSecondary,
            modifier = Modifier.padding(vertical = 12.dp),
          )
        } else {
          LazyColumn(
            modifier = Modifier.fillMaxWidth().height(260.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            items(filtered, key = { it.packageName }) { app ->
              val bmp = remember(app.packageName) { getAppIconBitmap(app.packageName) }
              Surface(
                modifier =
                  Modifier.fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(tokens.radiusMd))
                    .clickable { onSelectApp(app.packageName) }
                    .testTag("shortcut_pick_${app.packageName}"),
                shape = RoundedCornerShape(tokens.radiusMd),
                color = tokens.surfaceRaised,
                border = BorderStroke(1.dp, tokens.borderHairline),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  if (bmp != null) {
                    Image(
                      bitmap = bmp.asImageBitmap(),
                      contentDescription = app.label,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)),
                    )
                  } else {
                    Icon(
                      imageVector = Icons.Filled.Apps,
                      contentDescription = app.label,
                      tint = tokens.textSecondary,
                      modifier = Modifier.size(22.dp),
                    )
                  }
                  Spacer(modifier = Modifier.width(12.dp))
                  Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
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

@Composable
private fun NotificationHistoryDetailDialog(
  entry: NotificationHistoryEntry,
  onDismiss: () -> Unit,
) {
  val tokens = LocalDynaTokens.current
  val strings = LocalDynaStrings.current
  val timeText =
    remember(entry.timestampMs) {
      runCatching {
          Instant.ofEpochMilli(entry.timestampMs)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        .getOrDefault("--:--")
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = tokens.surfaceCard,
    titleContentColor = tokens.textPrimary,
    textContentColor = tokens.textSecondary,
    title = {
      Column {
        Text(
          text = "${entry.appName} • $timeText",
          style = DynaMonoTypography.LabelMono,
          color = tokens.textSecondary,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = entry.title, style = MaterialTheme.typography.titleLarge)
      }
    },
    text = {
      Text(
        text = entry.body.ifBlank { strings.historyDetailTitle },
        style = MaterialTheme.typography.bodyMedium,
        color = tokens.textPrimary,
      )
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.closeAction, color = tokens.accentPrimary)
      }
    },
  )
}
