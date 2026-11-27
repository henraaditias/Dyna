package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.R
import com.example.ui.components.BentoCanvasBackground
import com.example.ui.components.DynaStatusBadge
import com.example.ui.screens.AktivitasScreen
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.KapsulScreen
import com.example.ui.screens.PengaturanScreen
import com.example.ui.theme.LocalDynaTokens
import com.example.ui.viewmodel.DynaViewModel

object DynaRoute {
  const val BERANDA = "beranda"
  const val KAPSUL = "kapsul"
  const val AKTIVITAS = "aktivitas"
  const val PENGATURAN = "pengaturan"
}

private data class DynaDestination(
  val route: String,
  val labelRes: Int,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
)

private val primaryDestinations =
  listOf(
    DynaDestination(DynaRoute.BERANDA, R.string.nav_beranda, Icons.Filled.Home, Icons.Outlined.Home),
    DynaDestination(
      DynaRoute.KAPSUL,
      R.string.nav_kapsul,
      Icons.Filled.Layers,
      Icons.Outlined.Layers,
    ),
    DynaDestination(
      DynaRoute.AKTIVITAS,
      R.string.nav_aktivitas,
      Icons.Filled.Timer,
      Icons.Outlined.Timer,
    ),
  )

private val allDestinations =
  primaryDestinations +
    DynaDestination(
      DynaRoute.PENGATURAN,
      R.string.nav_pengaturan,
      Icons.Filled.Settings,
      Icons.Outlined.Settings,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynaApp(
  appContainer: AppContainer,
  viewModel: DynaViewModel = viewModel(factory = DynaViewModel.factory(appContainer)),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val navController = rememberNavController()
  val backStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = backStackEntry?.destination?.route ?: DynaRoute.BERANDA
  val snackbarHostState = remember { SnackbarHostState() }
  val tokens = LocalDynaTokens.current

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refreshPermissions() }

  LaunchedEffect(uiState.statusMessage) {
    val msg = uiState.statusMessage
    if (msg != null) {
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  fun navigateTo(route: String) {
    if (route == currentRoute) return
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  val currentTitleRes =
    allDestinations.firstOrNull { it.route == currentRoute }?.labelRes ?: R.string.nav_beranda

  BentoCanvasBackground {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val useNavigationRail = maxWidth >= 600.dp

      Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
          TopAppBar(
            colors =
              TopAppBarDefaults.topAppBarColors(
                containerColor = tokens.surfaceCard.copy(alpha = 0.94f),
                titleContentColor = tokens.textPrimary,
              ),
            title = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text =
                    if (currentRoute == DynaRoute.BERANDA) stringResource(R.string.app_name)
                    else stringResource(currentTitleRes),
                  style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(modifier = Modifier.width(10.dp))
                DynaStatusBadge(
                  text =
                    when {
                      uiState.isOverlayAttached -> stringResource(R.string.status_overlay_active)
                      uiState.permissions.hasOverlayPermission ->
                        stringResource(R.string.status_overlay_ready)
                      else -> stringResource(R.string.status_permission_needed)
                    },
                  dotColor =
                    when {
                      uiState.isOverlayAttached -> tokens.statusOnline
                      uiState.permissions.hasOverlayPermission -> tokens.accentCyan
                      else -> tokens.textMuted
                    },
                )
              }
            },
            actions = {
              if (!useNavigationRail) {
                IconButton(
                  onClick = { navigateTo(DynaRoute.PENGATURAN) },
                  modifier = Modifier.testTag("top_bar_settings_button"),
                ) {
                  Icon(
                    imageVector =
                      if (currentRoute == DynaRoute.PENGATURAN) Icons.Filled.Settings
                      else Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.cd_open_settings),
                    tint =
                      if (currentRoute == DynaRoute.PENGATURAN) tokens.accentCyan
                      else tokens.textSecondary,
                  )
                }
              }
            },
          )
        },
        bottomBar = {
          if (!useNavigationRail) {
            Column {
              HorizontalDivider(color = tokens.surfaceBorder)
              NavigationBar(containerColor = tokens.surfaceCard) {
                primaryDestinations.forEach { dest ->
                  val selected = currentRoute == dest.route
                  NavigationBarItem(
                    selected = selected,
                    onClick = { navigateTo(dest.route) },
                    modifier = Modifier.testTag("nav_${dest.route}"),
                    icon = {
                      Icon(
                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = stringResource(dest.labelRes),
                      )
                    },
                    label = {
                      Text(
                        text = stringResource(dest.labelRes),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    },
                    colors =
                      NavigationBarItemDefaults.colors(
                        selectedIconColor = tokens.accentCyan,
                        selectedTextColor = tokens.textPrimary,
                        indicatorColor = tokens.accentGlow,
                        unselectedIconColor = tokens.textSecondary,
                        unselectedTextColor = tokens.textSecondary,
                      ),
                  )
                }
              }
            }
          }
        },
      ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
          if (useNavigationRail) {
            NavigationRail(containerColor = tokens.surfaceCard) {
              allDestinations.forEach { dest ->
                val selected = currentRoute == dest.route
                NavigationRailItem(
                  selected = selected,
                  onClick = { navigateTo(dest.route) },
                  modifier = Modifier.testTag("nav_${dest.route}"),
                  icon = {
                    Icon(
                      imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                      contentDescription = stringResource(dest.labelRes),
                    )
                  },
                  label = {
                    Text(
                      text = stringResource(dest.labelRes),
                      style = MaterialTheme.typography.bodySmall,
                      maxLines = 1,
                    )
                  },
                  colors =
                    NavigationRailItemDefaults.colors(
                      selectedIconColor = tokens.accentCyan,
                      selectedTextColor = tokens.textPrimary,
                      indicatorColor = tokens.accentGlow,
                      unselectedIconColor = tokens.textSecondary,
                      unselectedTextColor = tokens.textSecondary,
                    ),
                )
              }
            }
            VerticalDivider(color = tokens.surfaceBorder)
          }

          Box(
            modifier = Modifier.weight(1f).fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
          ) {
            NavHost(
              navController = navController,
              startDestination = DynaRoute.BERANDA,
              modifier = Modifier.fillMaxWidth().widthIn(max = 860.dp),
            ) {
              composable(DynaRoute.BERANDA) {
                BerandaScreen(
                  uiState = uiState,
                  onOverlayEnabledChange = viewModel::setOverlayEnabled,
                  onOpenOverlaySettings = viewModel::openOverlaySettings,
                  onOpenNotificationSettings = viewModel::openNotificationAccessSettings,
                  onRefreshPermissions = viewModel::refreshPermissions,
                )
              }
              composable(DynaRoute.KAPSUL) {
                KapsulScreen(
                  uiState = uiState,
                  onUpdateAppearance = viewModel::updateAppearance,
                  onResetAppearance = viewModel::resetAppearance,
                  onSelectPreset = viewModel::setThemePreset,
                  onUpdateCustomColors = viewModel::updateCustomColors,
                )
              }
              composable(DynaRoute.AKTIVITAS) {
                AktivitasScreen(
                  uiState = uiState,
                  onSetTimerActivityEnabled = viewModel::setTimerActivityEnabled,
                  onStartTimer = viewModel::startTimer,
                  onToggleTimerPause = viewModel::toggleTimerPause,
                  onStopTimer = viewModel::stopTimer,
                  onOpenNotificationSettings = viewModel::openNotificationAccessSettings,
                  onSetNotificationActivityEnabled = viewModel::setNotificationActivityEnabled,
                  onSetShowNotificationPreview = viewModel::setShowNotificationPreview,
                  onSetAutoExpandOnNotification = viewModel::setAutoExpandOnNotification,
                )
              }
              composable(DynaRoute.PENGATURAN) {
                PengaturanScreen(
                  uiState = uiState,
                  onResetAllSettings = viewModel::resetAllSettings,
                )
              }
            }
          }
        }
      }
    }
  }
}
