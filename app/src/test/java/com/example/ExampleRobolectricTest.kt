package com.example

import android.app.Notification
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.core.app.ApplicationProvider
import com.example.data.activities.LiveActivityRepository
import com.example.data.notifications.LocalNotificationHistoryRepository
import com.example.domain.model.ActivityState
import com.example.domain.model.ActivityType
import com.example.domain.model.AppLanguage
import com.example.domain.model.CallSnapshot
import com.example.domain.model.DashboardThemeMode
import com.example.domain.model.ExpressionState
import com.example.domain.model.IdleDisplayMode
import com.example.domain.model.IslandAppearance
import com.example.domain.model.IslandThemeColors
import com.example.domain.model.MediaSnapshot
import com.example.domain.model.NotificationHistoryEntry
import com.example.domain.model.PermissionSnapshot
import com.example.domain.model.QuickMenuCategory
import com.example.domain.model.ReplyCapability
import com.example.domain.model.ShortcutAppItem
import com.example.domain.model.UserSettings
import com.example.platform.notifications.NotificationReplyManager
import com.example.ui.components.DynaIslandPill
import com.example.ui.navigation.DynaApp
import com.example.ui.navigation.DynaRoute
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.GayaScreen
import com.example.ui.screens.PengaturanScreen
import com.example.ui.screens.PermissionGateScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DynaPalette
import com.example.ui.theme.DynaTheme
import com.example.ui.viewmodel.DynaUiState
import java.time.LocalDate
import java.util.TimeZone
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `app name matches Dyna`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dyna", appName)
  }

  @Test
  fun `manifest declares overlay notification calendar audio and boot permissions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val info =
      context.packageManager.getPackageInfo(
        context.packageName,
        PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS,
      )
    val permissions = info.requestedPermissions?.toList().orEmpty()
    assertTrue(permissions.contains(android.Manifest.permission.SYSTEM_ALERT_WINDOW))
    assertTrue(permissions.contains(android.Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE))
    assertTrue(permissions.contains(android.Manifest.permission.READ_CALENDAR))
    assertTrue(permissions.contains(android.Manifest.permission.RECEIVE_BOOT_COMPLETED))
    assertTrue(permissions.contains(android.Manifest.permission.MODIFY_AUDIO_SETTINGS))

    val serviceNames = info.services?.map { it.name }.orEmpty()
    assertTrue(serviceNames.any { it.endsWith("DynaOverlayService") })
    assertTrue(serviceNames.any { it.endsWith("DynaNotificationListenerService") })

    val receiverNames = info.receivers?.map { it.name }.orEmpty()
    assertTrue(receiverNames.any { it.endsWith("NotificationHistoryLifecycleReceiver") })
  }

  @Test
  fun `monochrome defaults enforce dark charcoal Indonesian and zero pure black or white`() {
    val defaults = UserSettings()
    assertEquals(DashboardThemeMode.DARK, defaults.dashboardThemeMode)
    assertEquals(AppLanguage.ID, defaults.language)
    assertTrue(IslandThemeColors.Midnight.hasReadableContrast)

    val allColors =
      listOf(
        DynaPalette.LightCanvasBg,
        DynaPalette.LightSurfaceCard,
        DynaPalette.LightTextPrimary,
        DynaPalette.DarkCanvasBg,
        DynaPalette.DarkSurfaceCard,
        DynaPalette.DarkTextPrimary,
        DynaPalette.IslandBg,
        DynaPalette.IslandSurface,
        DynaPalette.IslandTextPrimary,
      )
    allColors.forEach { color ->
      assertNotEquals(0xFF000000L, color.value.toLong() and 0xFFFFFFFFL)
      assertNotEquals(0xFFFFFFFFL, color.value.toLong() and 0xFFFFFFFFL)
    }
  }

  @Test
  fun `local notification history records deletes and purges on day change or reboot`() = runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    var dayKey = "2026-11-28"
    var bootCount = 10
    val repo =
      LocalNotificationHistoryRepository(
        context = context,
        dayKeyProvider = { dayKey },
        bootCountProvider = { bootCount },
        clockMs = { 1_700_000_000_000L },
      )

    repo.clearAll()
    repo.recordNotification(
      packageName = "com.whatsapp",
      appName = "WhatsApp",
      title = "Budi",
      body = "Halo Dyna",
      isChat = true,
    )
    assertEquals(1, repo.entries.first().size)
    val firstId = repo.entries.first().first().id

    repo.deleteEntry(firstId)
    assertTrue(repo.entries.first().isEmpty())

    repo.recordNotification(
      packageName = "com.telegram",
      appName = "Telegram",
      title = "Siti",
      body = "Pesan baru",
      isChat = true,
    )
    assertEquals(1, repo.entries.first().size)

    dayKey = "2026-11-29"
    repo.purgeIfDayChangedOrRebooted()
    assertTrue(repo.entries.first().isEmpty())

    repo.recordNotification(
      packageName = "com.whatsapp",
      appName = "WhatsApp",
      title = "Andi",
      body = "Pagi",
      isChat = true,
    )
    assertEquals(1, repo.entries.first().size)

    bootCount = 11
    repo.purgeIfDayChangedOrRebooted()
    assertTrue(repo.entries.first().isEmpty())
  }

  @Test
  fun `live activity repository handles priority 6 notification interrupt and resume over call and media`() =
    runTest {
      val repo = LiveActivityRepository()
      assertEquals(ActivityType.IDLE, repo.currentActivity.first().type)

      repo.onMediaUpdated(
        MediaSnapshot(
          packageName = "com.spotify.music",
          appName = "Spotify",
          title = "Lagu Santai",
          artist = "Artis",
          isPlaying = true,
          hasFavoriteAction = true,
        )
      )
      assertEquals(ActivityType.MEDIA, repo.currentActivity.first().type)

      repo.onCallPosted(
        CallSnapshot(
          key = "call-1",
          appName = "WhatsApp",
          callerName = "Andi",
          statusLabel = "Panggilan berlangsung",
          hasAnswerAction = false,
          hasDeclineAction = true,
        )
      )
      assertEquals(ActivityType.CALL, repo.currentActivity.first().type)
      assertFalse(repo.currentActivity.first().isCallMuted)

      repo.toggleCallMute()
      assertTrue(repo.currentActivity.first().isCallMuted)
      repo.toggleCallVideo()
      assertTrue(repo.currentActivity.first().isCallVideoActive)

      val replyCap = ReplyCapability("Balas", "Balas Budi", listOf("Sebentar"))
      repo.onNotificationPosted(
        key = "notif-interrupt",
        appName = "WhatsApp",
        title = "Budi",
        text = "Jadi meeting?",
        autoExpand = true,
        replyCapability = replyCap,
        hasMarkReadAction = true,
        sourcePackageName = "com.whatsapp",
      )
      assertEquals(ActivityType.NOTIFICATION, repo.currentActivity.first().type)
      assertTrue(repo.currentActivity.first().hasMarkReadAction)
      assertEquals(ActivityType.CALL, repo.currentActivity.first().secondaryBadge?.type)

      repo.dismissActiveNotification()
      assertEquals(ActivityType.CALL, repo.currentActivity.first().type)

      repo.dismissCall()
      assertEquals(ActivityType.MEDIA, repo.currentActivity.first().type)

      repo.onMediaUpdated(null)
      assertEquals(ActivityType.IDLE, repo.currentActivity.first().type)
      assertFalse(repo.isOverlayExpanded.first())
    }

  @Test
  fun `live activity repository test simulations lockscreen and landscape behave properly`() =
    runTest {
      val repo = LiveActivityRepository()

      repo.triggerTestNotification(autoCollapseSeconds = 5)
      assertEquals(ActivityType.NOTIFICATION, repo.currentActivity.first().type)
      assertTrue(repo.isOverlayExpanded.first())
      repo.dismissActiveNotification()
      assertEquals(ActivityType.IDLE, repo.currentActivity.first().type)

      repo.triggerTestChatNotification(autoCollapseSeconds = 5)
      assertEquals(ActivityType.NOTIFICATION, repo.currentActivity.first().type)
      assertNotNull(repo.currentActivity.first().replyCapability)

      repo.setDeviceLocked(true)
      assertTrue(repo.isDeviceLocked.first())
      assertFalse(repo.isOverlayExpanded.first())
      repo.setDeviceLocked(false)

      repo.triggerTestCallAndMedia()
      assertEquals(ActivityType.CALL, repo.currentActivity.first().type)
      assertEquals(ActivityType.MEDIA, repo.currentActivity.first().secondaryBadge?.type)

      repo.swapToSecondaryActivity(ActivityType.MEDIA)
      assertEquals(ActivityType.MEDIA, repo.currentActivity.first().type)
      repo.toggleSimulatedMediaFavorite()
      assertTrue(repo.currentActivity.first().mediaIsFavorited)
      repo.toggleSimulatedMediaPlayPause()
      assertFalse(repo.currentActivity.first().mediaIsPlaying)

      repo.setLandscape(true)
      assertTrue(repo.isLandscape.first())
      repo.setLandscape(false)
    }

  @Test
  fun `notification reply manager extracts remote input sends reply and suppresses echo`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    var fakeNowMs = 1_000L
    val manager = NotificationReplyManager(clockMs = { fakeNowMs })

    val remoteInput =
      RemoteInput.Builder("key_text_reply")
        .setLabel("Balas Budi")
        .setChoices(arrayOf("Oke", "Nanti"))
        .build()
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        10,
        Intent("com.example.TEST_REPLY_ACTION"),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
      )
    val replyAction =
      Notification.Action.Builder(0, "Balas", pendingIntent).addRemoteInput(remoteInput).build()
    val notification =
      Notification.Builder(context, "test_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setContentTitle("Budi")
        .setContentText("Lagi di mana?")
        .addAction(replyAction)
        .build()

    val capability = manager.extractAndRegister("chat-1", notification)
    assertNotNull(capability)
    assertEquals("Balas", capability?.actionLabel)
    assertEquals("Balas Budi", capability?.placeholder)
    assertEquals(listOf("Oke", "Nanti"), capability?.quickChoices)

    val sent =
      manager.sendReply(
        context = context,
        key = "chat-1",
        replyText = "Lagi di rumah",
        titleSnapshot = "Budi",
        textSnapshot = "Lagi di mana?",
      )
    assertTrue(sent)

    fakeNowMs += 500L
    val echoNotification =
      Notification.Builder(context, "test_channel")
        .setSmallIcon(android.R.drawable.ic_dialog_email)
        .setContentTitle("Budi")
        .setContentText("Lagi di mana?")
        .setRemoteInputHistory(arrayOf("Lagi di rumah"))
        .build()
    assertTrue(
      manager.shouldIgnoreAsReplyEcho(
        key = "chat-1",
        notification = echoNotification,
        title = "Budi",
        text = "Lagi di mana?",
      )
    )
  }

  @Test
  fun `dyna island pill displays reply button mark read and quick reply`() {
    val chatActivity =
      ActivityState(
        id = "chat-key-1",
        type = ActivityType.NOTIFICATION,
        title = "Budi",
        subtitle = "Jadi ketemu jam 5?",
        sourceAppName = "WhatsApp",
        hasMarkReadAction = true,
        replyCapability =
          ReplyCapability(
            actionLabel = "Balas",
            placeholder = "Ketik balasan...",
            quickChoices = listOf("Jadi!"),
          ),
      )

    var startedReply = false
    var sentMessage: String? = null
    var markedRead = false

    composeTestRule.setContent {
      DynaTheme {
        DynaIslandPill(
          activity = chatActivity,
          appearance = IslandAppearance(animationEnabled = false),
          isExpanded = true,
          showNotificationPreview = true,
          isReplying = false,
          onClick = {},
          onStartReply = { startedReply = true },
          onMarkNotificationRead = { markedRead = true },
          onSendReply = { sentMessage = it },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pill_notif_mark_read").assertIsDisplayed().performClick()
    assertTrue(markedRead)

    composeTestRule.onNodeWithTag("pill_reply_button").assertIsDisplayed().performClick()
    assertTrue(startedReply)

    composeTestRule.onNodeWithTag("pill_quick_reply_0").assertIsDisplayed().performClick()
    assertEquals("Jadi!", sentMessage)
  }

  @Test
  fun `dyna island pill composer sends custom text reply`() {
    val chatActivity =
      ActivityState(
        id = "chat-key-2",
        type = ActivityType.NOTIFICATION,
        title = "Budi",
        subtitle = "Sudah sampai?",
        sourceAppName = "Telegram",
        replyCapability = ReplyCapability(actionLabel = "Balas", placeholder = "Ketik balasan..."),
      )

    var sentMessage: String? = null

    composeTestRule.setContent {
      DynaTheme {
        DynaIslandPill(
          activity = chatActivity,
          appearance = IslandAppearance(animationEnabled = false),
          isExpanded = true,
          showNotificationPreview = true,
          isReplying = true,
          onClick = {},
          onSendReply = { sentMessage = it },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pill_reply_input").performTextInput("Sebentar lagi")
    composeTestRule.onNodeWithTag("pill_reply_send_button").performClick()
    assertEquals("Sebentar lagi", sentMessage)
  }

  @Test
  fun `dyna island pill renders call mic indicator in collapsed state`() {
    val callActivity =
      ActivityState(
        id = "call-key",
        type = ActivityType.CALL,
        title = "Andi",
        subtitle = "02:14",
        sourceAppName = "WhatsApp",
        isCallMuted = false,
      )

    composeTestRule.setContent {
      DynaTheme {
        DynaIslandPill(
          activity = callActivity,
          appearance =
            IslandAppearance(
              idleDisplayMode = IdleDisplayMode.CIRCLE_FACE,
              animationEnabled = false,
            ),
          isExpanded = false,
          showNotificationPreview = true,
          onClick = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dyna_island_pill").assertIsDisplayed()
    composeTestRule.onNodeWithTag("pill_call_mic_indicator", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun `idle quick category menu calendar shows month year switchers below weekly strip`() {
    var openedDate: LocalDate? = null
    val today = LocalDate.now()

    composeTestRule.setContent {
      DynaTheme {
        DynaIslandPill(
          activity = ActivityState.Idle,
          appearance =
            IslandAppearance(
              idleDisplayMode = IdleDisplayMode.PILL_LEFT_FACE_INFO,
              selectedExpression = ExpressionState.HEART_EYES,
              animationEnabled = false,
            ),
          isExpanded = true,
          showNotificationPreview = true,
          quickMenuCategory = QuickMenuCategory.CALENDAR,
          onClick = {},
          onOpenCalendarDate = { openedDate = it },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("calendar_prev_month").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("calendar_next_year").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("calendar_day_${today.dayOfMonth}").assertIsDisplayed().performClick()
    assertNotNull(openedDate)
  }

  @Test
  fun `shortcuts panel in pill shows manage CTA when empty`() {
    var manageClicked = false

    composeTestRule.setContent {
      DynaTheme {
        DynaIslandPill(
          activity = ActivityState.Idle,
          appearance = IslandAppearance(animationEnabled = false),
          isExpanded = true,
          showNotificationPreview = true,
          quickMenuCategory = QuickMenuCategory.SHORTCUTS,
          shortcutSlots = emptyList(),
          onClick = {},
          onOpenShortcutsSettings = { manageClicked = true },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pill_open_shortcuts_settings").assertIsDisplayed().performClick()
    assertTrue(manageClicked)
  }

  @Test
  fun `shortcuts panel in pill renders 2 page grid when populated`() {
    val sampleApps =
      (1..10).map { idx ->
        ShortcutAppItem(packageName = "com.app.test$idx", label = "App $idx")
      }

    composeTestRule.setContent {
      DynaTheme {
        DynaIslandPill(
          activity = ActivityState.Idle,
          appearance = IslandAppearance(animationEnabled = false),
          isExpanded = true,
          showNotificationPreview = true,
          quickMenuCategory = QuickMenuCategory.SHORTCUTS,
          shortcutSlots = sampleApps,
          onClick = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pill_shortcuts_pager", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("pill_shortcut_item_com.app.test1").assertIsDisplayed()
  }

  @Test
  fun `splash screen auto skips to beranda when required permissions granted`() {
    var autoSkipResult: Boolean? = null

    composeTestRule.setContent {
      DynaTheme {
        SplashScreen(
          permissions =
            PermissionSnapshot(hasOverlayPermission = true, hasNotificationAccess = true),
          onSplashCompleted = { autoSkipResult = it },
        )
      }
    }

    composeTestRule.mainClock.advanceTimeBy(900L)
    composeTestRule.waitForIdle()
    assertEquals(true, autoSkipResult)
  }

  @Test
  fun `permission gate screen requires both overlay and notification permissions`() {
    composeTestRule.setContent {
      DynaTheme {
        PermissionGateScreen(
          permissions =
            PermissionSnapshot(hasOverlayPermission = true, hasNotificationAccess = false),
          onOpenOverlaySettings = {},
          onOpenNotificationSettings = {},
          onContinue = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("permission_gate_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gate_continue_button").performScrollTo().assertIsNotEnabled()
  }

  @Test
  fun `permission gate screen enables continue button once both permissions granted`() {
    var continued = false
    composeTestRule.setContent {
      DynaTheme {
        PermissionGateScreen(
          permissions =
            PermissionSnapshot(hasOverlayPermission = true, hasNotificationAccess = true),
          onOpenOverlaySettings = {},
          onOpenNotificationSettings = {},
          onContinue = { continued = true },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule
      .onNodeWithTag("gate_continue_button")
      .performScrollTo()
      .assertIsEnabled()
      .performClick()
    assertTrue(continued)
  }

  @Test
  fun `beranda screen renders shortcuts grid and daily notification history`() {
    var deletedId: String? = null
    val sampleApp = ShortcutAppItem(packageName = "com.whatsapp", label = "WhatsApp")
    val historyEntry =
      NotificationHistoryEntry(
        id = "hist-1",
        packageName = "com.whatsapp",
        appName = "WhatsApp",
        title = "Budi",
        body = "Jangan lupa rapat jam 2",
        timestampMs = 1_700_000_000_000L,
        dayKey = "2026-11-28",
        isChat = true,
      )

    composeTestRule.setContent {
      DynaTheme {
        BerandaScreen(
          uiState =
            DynaUiState(
              settings = UserSettings(pinnedShortcutPackages = listOf("com.whatsapp")),
              installedApps = listOf(sampleApp),
              notificationHistory = listOf(historyEntry),
            ),
          onAddShortcut = {},
          onRemoveShortcut = {},
          onReorderShortcut = { _, _ -> },
          onDeleteHistoryEntry = { deletedId = it },
          onClearHistory = {},
          getAppIconBitmap = { null },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("beranda_title").assertIsDisplayed()
    composeTestRule.onNodeWithTag("beranda_shortcut_tile_0").assertIsDisplayed()
    composeTestRule.onNodeWithTag("beranda_shortcut_add_button").assertIsDisplayed()
    composeTestRule.onNodeWithTag("history_lazy_list").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("history_item_hist-1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("history_delete_hist-1").performClick()
    assertEquals("hist-1", deletedId)
  }

  @Test
  fun `gaya screen triggers test buttons and switches pager slides`() {
    var testNotifCount = 0
    var testChatCount = 0
    var testCallCount = 0

    composeTestRule.setContent {
      DynaTheme {
        GayaScreen(
          uiState = DynaUiState(),
          onUpdateAppearance = {},
          onResetAppearance = {},
          onTestNotification = { testNotifCount++ },
          onTestChatNotification = { testChatCount++ },
          onTestCallAndMedia = { testCallCount++ },
          onToggleOverlayExpanded = {},
          onSelectQuickCategory = {},
          onHideIsland = {},
          onRestoreIsland = {},
          onStartTimer = {},
          onToggleTimerPause = {},
          onStopTimer = {},
          onStartStopwatch = {},
          onToggleStopwatchPause = {},
          onLapStopwatch = {},
          onStopStopwatch = {},
          onDismissNotification = {},
          onStartReply = {},
          onCancelReply = {},
          onSendSimulatedReply = {},
          onMediaPlayPause = {},
          onMediaFavorite = {},
          onCallToggleMute = {},
          onCallToggleVideo = {},
          onDismissCall = {},
          onLaunchShortcut = {},
          onOpenCalendarDate = {},
          onRefreshWeather = {},
          onSwapSecondaryActivity = {},
          getAppIconBitmap = { null },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gaya_preview_card").assertIsDisplayed()
    composeTestRule.onNodeWithTag("gaya_test_notif").performClick()
    composeTestRule.onNodeWithTag("gaya_test_chat").performClick()
    composeTestRule.onNodeWithTag("gaya_test_call_media").performClick()
    assertEquals(1, testNotifCount)
    assertEquals(1, testChatCount)
    assertEquals(1, testCallCount)

    composeTestRule.onNodeWithTag("gaya_slide_tab_1").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(500L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gaya_controls_pager").performScrollTo()
    composeTestRule.onNodeWithTag("gaya_slider_notif_width").assertIsDisplayed()

    composeTestRule.onNodeWithTag("gaya_slide_tab_2").performScrollTo().performClick()
    composeTestRule.mainClock.advanceTimeBy(500L)
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gaya_controls_pager").performScrollTo()
    composeTestRule.onNodeWithTag("gaya_reset_dimensions").assertIsDisplayed()
  }

  @Test
  fun `pengaturan screen switches language theme and exposes extension placeholder`() {
    var selectedLang: AppLanguage? = null
    var selectedTheme: DashboardThemeMode? = null

    composeTestRule.setContent {
      DynaTheme {
        PengaturanScreen(
          uiState = DynaUiState(),
          onRefreshPermissions = {},
          onSetLanguage = { selectedLang = it },
          onSetDashboardThemeMode = { selectedTheme = it },
          onSetOverlayEnabled = {},
          onSetCalendarEnabled = {},
          onSetWeatherEnabled = {},
          onSetShortcutsEnabled = {},
          onSetNotificationActivityEnabled = {},
          onSetShowNotificationPreview = {},
          onSetMediaActivityEnabled = {},
          onSetCallActivityEnabled = {},
          onSetBatteryActivityEnabled = {},
          onSetTimerActivityEnabled = {},
          onSetStopwatchEnabled = {},
          onSetPrayerAlarmEnabled = {},
          onSetHideTopDotEnabled = {},
          onSearchCities = {},
          onClearCitySearch = {},
          onSelectCity = { _, _, _ -> },
          onUseCurrentGpsLocation = {},
          onResetAllSettings = {},
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("settings_lang_en").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("settings_theme_light").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("settings_extension_row").performScrollTo().assertIsDisplayed()
    assertEquals(AppLanguage.EN, selectedLang)
    assertEquals(DashboardThemeMode.LIGHT, selectedTheme)
  }

  @Test
  fun `dyna app displays floating icon only pill nav and switches between 3 tabs`() {
    val app = ApplicationProvider.getApplicationContext<DynaApplication>()

    composeTestRule.setContent {
      DynaApp(appContainer = app.container, initialRoute = DynaRoute.BERANDA)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("floating_bottom_nav_pill").assertIsDisplayed()
    composeTestRule.onNodeWithTag("beranda_title").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_gaya").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("gaya_title").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nav_pengaturan").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("pengaturan_title").assertIsDisplayed()
  }

  @Test
  fun `hidden top dot restores dynamic island on tap and swipe`() {
    var restoreCount = 0

    composeTestRule.setContent {
      DynaTheme {
        DynaIslandPill(
          activity = ActivityState.Idle,
          appearance = IslandAppearance(animationEnabled = false),
          isExpanded = false,
          showNotificationPreview = true,
          isIslandHidden = true,
          onClick = {},
          onRestoreIsland = { restoreCount++ },
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("dyna_hidden_top_dot").assertIsDisplayed().performClick()
    assertEquals(1, restoreCount)

    composeTestRule.onNodeWithTag("dyna_hidden_top_dot").performTouchInput { swipeDown() }
    assertTrue(restoreCount >= 2)
  }

  @Test
  fun `prayer time calculator produces ordered daily entries for Jakarta`() {
    val schedule =
      com.example.domain.prayer.PrayerTimeCalculator.computeSchedule(
        cityName = "Jakarta",
        latitude = -6.2088,
        longitude = 106.8456,
        timeZone = TimeZone.getTimeZone("Asia/Jakarta"),
      )
    assertEquals("Jakarta", schedule.cityName)
    assertEquals(5, schedule.entries.size)
    val times = schedule.entries.map { it.hour * 60 + it.minute }
    for (i in 0 until times.lastIndex) {
      assertTrue("Expected ordered prayer times but got $times", times[i] < times[i + 1])
    }
  }
}
