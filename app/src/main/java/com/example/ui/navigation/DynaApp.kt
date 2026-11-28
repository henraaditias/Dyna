package com.example.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.AppContainer
import com.example.domain.model.DashboardThemeMode
import com.example.ui.components.DynaCanvasBackground
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.GayaScreen
import com.example.ui.screens.PengaturanScreen
import com.example.ui.screens.PermissionGateScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.DynaTheme
import com.example.ui.theme.LocalDynaStrings
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaViewModel

object DynaRoute {
  const val SPLASH = "splash"
  const val GATE = "gate"
  const val BERANDA = "beranda"
  const val GAYA = "gaya"
  const val PENGATURAN = "pengaturan"
}

private data class DynaTabItem(
  val route: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val primaryTabs =
  listOf(
    DynaTabItem(
      route = DynaRoute.BERANDA,
      selectedIcon = Icons.Filled.Home,
      unselectedIcon = Icons.Outlined.Home,
      testTag = "nav_beranda",
    ),
    DynaTabItem(
      route = DynaRoute.GAYA,
      selectedIcon = Icons.Filled.Tune,
      unselectedIcon = Icons.Outlined.Tune,
      testTag = "nav_gaya",
    ),
    DynaTabItem(
      route = DynaRoute.PENGATURAN,
      selectedIcon = Icons.Filled.Settings,
      unselectedIcon = Icons.Outlined.Settings,
      testTag = "nav_pengaturan",
    ),
  )

@Composable
fun DynaApp(
  appContainer: AppContainer,
  initialRoute: String? = null,
  viewModel: DynaViewModel = viewModel(factory = DynaViewModel.factory(appContainer)),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val resolvedStartRoute =
    remember(initialRoute) {
      if (
        initialRoute in
          listOf(
            DynaRoute.SPLASH,
            DynaRoute.GATE,
            DynaRoute.BERANDA,
            DynaRoute.GAYA,
            DynaRoute.PENGATURAN,
          )
      ) {
        initialRoute!!
      } else {
        DynaRoute.SPLASH
      }
    }
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: resolvedStartRoute
  val snackbarHostState = remember { SnackbarHostState() }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }

  LaunchedEffect(uiState.statusMessage) {
    val msg = uiState.statusMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  fun navigateTab(route: String) {
    if (route == currentRoute) return
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  DynaTheme(
    darkTheme = uiState.settings.dashboardThemeMode == DashboardThemeMode.DARK,
    language = uiState.settings.language,
  ) {
    val tokens = LocalDynaTokens.current
    val strings = LocalDynaStrings.current
    val showFloatingPillNav =
      currentRoute == DynaRoute.BERANDA ||
        currentRoute == DynaRoute.GAYA ||
        currentRoute == DynaRoute.PENGATURAN

    DynaCanvasBackground {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Scaffold(
          containerColor = Color.Transparent,
          snackbarHost = {
            SnackbarHost(
              hostState = snackbarHostState,
              modifier = Modifier.padding(bottom = if (showFloatingPillNav) 96.dp else 16.dp),
            )
          },
        ) { innerPadding ->
          Box(
            modifier = Modifier.fillMaxSize().padding(top = innerPadding.calculateTopPadding()),
            contentAlignment = Alignment.TopCenter,
          ) {
            NavHost(
              navController = navController,
              startDestination = resolvedStartRoute,
              modifier = Modifier.fillMaxWidth().widthIn(max = 860.dp),
            ) {
              composable(DynaRoute.SPLASH) {
                SplashScreen(
                  permissions = uiState.permissions,
                  onSplashCompleted = { hasRequired ->
                    val nextRoute = if (hasRequired) DynaRoute.BERANDA else DynaRoute.GATE
                    navController.navigate(nextRoute) {
                      popUpTo(DynaRoute.SPLASH) { inclusive = true }
                      launchSingleTop = true
                    }
                  },
                )
              }
              composable(DynaRoute.GATE) {
                PermissionGateScreen(
                  permissions = uiState.permissions,
                  onOpenOverlaySettings = viewModel::openOverlaySettings,
                  onOpenNotificationSettings = viewModel::openNotificationAccessSettings,
                  onContinue = {
                    viewModel.setOverlayEnabled(true)
                    navController.navigate(DynaRoute.BERANDA) {
                      popUpTo(DynaRoute.GATE) { inclusive = true }
                      launchSingleTop = true
                    }
                  },
                )
              }
              composable(DynaRoute.BERANDA) {
                BerandaScreen(
                  uiState = uiState,
                  onAddShortcut = viewModel::addShortcut,
                  onRemoveShortcut = viewModel::removeShortcut,
                  onReorderShortcut = viewModel::reorderShortcut,
                  onDeleteHistoryEntry = viewModel::deleteNotificationHistoryEntry,
                  onClearHistory = viewModel::clearNotificationHistory,
                  getAppIconBitmap = viewModel::getAppIconBitmap,
                )
              }
              composable(DynaRoute.GAYA) {
                GayaScreen(
                  uiState = uiState,
                  onUpdateAppearance = viewModel::updateAppearance,
                  onResetAppearance = viewModel::resetAppearance,
                  onTestNotification = viewModel::triggerTestNotification,
                  onTestChatNotification = viewModel::triggerTestChatNotification,
                  onTestCallAndMedia = viewModel::triggerTestCallAndMedia,
                  onToggleOverlayExpanded = viewModel::togglePreviewOverlayExpanded,
                  onSelectQuickCategory = viewModel::setPreviewQuickMenuCategory,
                  onHideIsland = viewModel::hideIsland,
                  onRestoreIsland = viewModel::restoreIsland,
                  onStartTimer = viewModel::startTimer,
                  onToggleTimerPause = viewModel::toggleTimerPause,
                  onStopTimer = viewModel::stopTimer,
                  onStartStopwatch = viewModel::startStopwatch,
                  onToggleStopwatchPause = viewModel::toggleStopwatchPause,
                  onLapStopwatch = viewModel::recordStopwatchLap,
                  onStopStopwatch = viewModel::stopStopwatch,
                  onDismissNotification = viewModel::dismissActiveNotification,
                  onStartReply = viewModel::startReplyInPreview,
                  onCancelReply = viewModel::cancelReplyInPreview,
                  onSendSimulatedReply = viewModel::sendSimulatedReply,
                  onMediaPlayPause = viewModel::toggleSimulatedMediaPlayPause,
                  onMediaFavorite = viewModel::toggleSimulatedMediaFavorite,
                  onCallToggleMute = viewModel::toggleCallMute,
                  onCallToggleVideo = viewModel::toggleCallVideo,
                  onDismissCall = viewModel::dismissCall,
                  onLaunchShortcut = viewModel::launchApp,
                  onOpenCalendarDate = viewModel::openGoogleCalendarForDate,
                  onRefreshWeather = { viewModel.refreshWeather(force = true) },
                  onSwapSecondaryActivity = viewModel::swapSecondaryActivity,
                  getAppIconBitmap = viewModel::getAppIconBitmap,
                )
              }
              composable(DynaRoute.PENGATURAN) {
                PengaturanScreen(
                  uiState = uiState,
                  onRefreshPermissions = viewModel::refreshPermissions,
                  onSetLanguage = viewModel::setLanguage,
                  onSetDashboardThemeMode = viewModel::setDashboardThemeMode,
                  onSetOverlayEnabled = viewModel::setOverlayEnabled,
                  onSetCalendarEnabled = viewModel::setCalendarEnabled,
                  onSetWeatherEnabled = viewModel::setWeatherEnabled,
                  onSetShortcutsEnabled = viewModel::setShortcutsEnabled,
                  onSetNotificationActivityEnabled = viewModel::setNotificationActivityEnabled,
                  onSetShowNotificationPreview = viewModel::setShowNotificationPreview,
                  onSetMediaActivityEnabled = viewModel::setMediaActivityEnabled,
                  onSetCallActivityEnabled = viewModel::setCallActivityEnabled,
                  onSetBatteryActivityEnabled = viewModel::setBatteryActivityEnabled,
                  onSetTimerActivityEnabled = viewModel::setTimerActivityEnabled,
                  onSetStopwatchEnabled = viewModel::setStopwatchEnabled,
                  onSetPrayerAlarmEnabled = viewModel::setPrayerAlarmEnabled,
                  onSetHideTopDotEnabled = viewModel::setHideTopDotEnabled,
                  onSearchCities = viewModel::searchCities,
                  onClearCitySearch = viewModel::clearCitySearch,
                  onSelectCity = { name, lat, lon ->
                    viewModel.selectCity(name, lat, lon, isGps = false)
                  },
                  onUseCurrentGpsLocation = viewModel::useCurrentGpsLocation,
                  onResetAllSettings = viewModel::resetAllSettings,
                )
              }
            }
          }
        }

        if (showFloatingPillNav) {
          FloatingIconOnlyPillNavBar(
            currentRoute = currentRoute,
            onNavigate = ::navigateTab,
            contentDescriptions =
              mapOf(
                DynaRoute.BERANDA to strings.navHomeCd,
                DynaRoute.GAYA to strings.navStyleCd,
                DynaRoute.PENGATURAN to strings.navSettingsCd,
              ),
            modifier =
              Modifier.align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun FloatingIconOnlyPillNavBar(
  currentRoute: String,
  onNavigate: (String) -> Unit,
  contentDescriptions: Map<String, String>,
  modifier: Modifier = Modifier,
) {
  val tokens = LocalDynaTokens.current
  Surface(
    modifier = modifier.height(64.dp).testTag("floating_bottom_nav_pill"),
    shape = CircleShape,
    color = tokens.surfaceCard,
    border = BorderStroke(1.dp, tokens.borderHairline),
    shadowElevation = 0.dp,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      primaryTabs.forEach { tab ->
        val selected = currentRoute == tab.route
        Box(
          modifier =
            Modifier.size(48.dp)
              .clip(CircleShape)
              .background(if (selected) tokens.surfaceRaised else Color.Transparent)
              .clickable(role = Role.Tab) { onNavigate(tab.route) }
              .testTag(tab.testTag),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = contentDescriptions[tab.route] ?: tab.route,
            tint = if (selected) tokens.accentPrimary else tokens.textMuted,
            modifier = Modifier.size(22.dp),
          )
        }
      }
    }
  }
}
