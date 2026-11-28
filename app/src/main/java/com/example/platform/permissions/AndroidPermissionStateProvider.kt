package com.example.platform.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.domain.model.PermissionSnapshot
import com.example.domain.repository.PermissionStateProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidPermissionStateProvider(private val context: Context) : PermissionStateProvider {

  private val _permissions = MutableStateFlow(readSnapshot())
  override val permissions: StateFlow<PermissionSnapshot> = _permissions.asStateFlow()

  override fun refresh() {
    _permissions.value = readSnapshot()
  }

  override fun openOverlaySettings(): Boolean {
    val packageIntent =
      Intent(
          Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
          Uri.parse("package:${context.packageName}"),
        )
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    return tryStartActivity(packageIntent)
      || tryStartActivity(
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      )
      || tryStartActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}"),
          )
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      )
  }

  override fun openNotificationAccessSettings(): Boolean {
    return tryStartActivity(
      Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
  }

  private fun tryStartActivity(intent: Intent): Boolean {
    return runCatching {
        context.startActivity(intent)
        true
      }
      .getOrDefault(false)
  }

  private fun readSnapshot(): PermissionSnapshot {
    val hasOverlay = runCatching { Settings.canDrawOverlays(context) }.getOrDefault(false)
    val hasNotifAccess =
      runCatching {
          NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
        }
        .getOrDefault(false)
    val hasPostNotif =
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
          PackageManager.PERMISSION_GRANTED
      } else {
        true
      }
    val hasLocation =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val hasCalendar =
      ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
        PackageManager.PERMISSION_GRANTED

    return PermissionSnapshot(
      hasOverlayPermission = hasOverlay,
      hasNotificationAccess = hasNotifAccess,
      hasPostNotificationsPermission = hasPostNotif,
      hasLocationPermission = hasLocation,
      hasCalendarPermission = hasCalendar,
    )
  }
}
