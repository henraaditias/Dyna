package com.example

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.activities.LiveActivityRepository
import com.example.domain.model.ActivityType
import com.example.domain.model.IslandThemeColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app name matches Dyna`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Dyna", appName)
  }

  @Test
  fun `manifest declares overlay permission and notification listener service`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val info =
      context.packageManager.getPackageInfo(
        context.packageName,
        PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES,
      )
    val permissions = info.requestedPermissions?.toList().orEmpty()
    assertTrue(permissions.contains(android.Manifest.permission.SYSTEM_ALERT_WINDOW))
    assertTrue(permissions.contains(android.Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE))

    val serviceNames = info.services?.map { it.name }.orEmpty()
    assertTrue(serviceNames.any { it.endsWith("DynaOverlayService") })
    assertTrue(serviceNames.any { it.endsWith("DynaNotificationListenerService") })
  }

  @Test
  fun `preset themes preserve accessible contrast`() {
    assertTrue(IslandThemeColors.Midnight.hasReadableContrast)
    assertTrue(IslandThemeColors.Minimal.hasReadableContrast)
    assertTrue(IslandThemeColors.Aurora.hasReadableContrast)

    val lowContrast =
      IslandThemeColors(
        backgroundArgb = 0xFF141416,
        foregroundArgb = 0xFF222226,
        accentArgb = 0xFF38BDF8,
        iconArgb = 0xFF38BDF8,
      )
    assertFalse(lowContrast.hasReadableContrast)
  }

  @Test
  fun `live activity repository handles notifications timer priority and collapse`() = runTest {
    val repo = LiveActivityRepository()
    assertEquals(ActivityType.IDLE, repo.currentActivity.first().type)

    repo.onNotificationPosted(
      key = "notif-1",
      appName = "Pesan",
      title = "Budi",
      text = "Halo!",
      autoExpand = true,
    )
    val notifState = repo.currentActivity.first()
    assertEquals(ActivityType.NOTIFICATION, notifState.type)
    assertEquals("Pesan", notifState.sourceAppName)
    assertTrue(repo.isOverlayExpanded.first())

    repo.startTimer(60)
    assertEquals(ActivityType.TIMER, repo.currentActivity.first().type)

    repo.stopTimer()
    assertEquals(ActivityType.NOTIFICATION, repo.currentActivity.first().type)

    repo.onNotificationRemoved("notif-1")
    assertEquals(ActivityType.IDLE, repo.currentActivity.first().type)
    assertFalse(repo.isOverlayExpanded.first())
  }
}
