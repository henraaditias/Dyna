package com.example.platform.shortcuts

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.CalendarContract
import com.example.domain.model.ShortcutAppItem
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.ConcurrentHashMap

class AppShortcutsProvider(private val context: Context) {

  private val iconCache = ConcurrentHashMap<String, Bitmap>()

  fun queryInstalledLaunchableApps(): List<ShortcutAppItem> {
    val pm = context.packageManager
    val launcherIntent =
      Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
    val resolved = runCatching { pm.queryIntentActivities(launcherIntent, 0) }.getOrDefault(emptyList())

    return resolved
      .mapNotNull { info ->
        val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
        if (pkg == context.packageName) return@mapNotNull null
        val label = runCatching { info.loadLabel(pm).toString() }.getOrDefault(pkg)
        ShortcutAppItem(packageName = pkg, label = label)
      }
      .distinctBy { it.packageName }
      .sortedBy { it.label.lowercase() }
  }

  fun getAppIconBitmap(packageName: String): Bitmap? {
    iconCache[packageName]?.let { return it }
    val drawable =
      runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
        ?: return null
    val bitmap =
      if (drawable is BitmapDrawable && drawable.bitmap != null) {
        val raw = drawable.bitmap
        if (raw.width <= 96 && raw.height <= 96) {
          raw
        } else {
          Bitmap.createScaledBitmap(raw, 96, 96, true)
        }
      } else {
        val size = 96
        Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).also { bmp ->
          val canvas = Canvas(bmp)
          drawable.setBounds(0, 0, canvas.width, canvas.height)
          drawable.draw(canvas)
        }
      }
    iconCache[packageName] = bitmap
    return bitmap
  }

  fun launchApp(packageName: String): Boolean {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    return runCatching {
        context.startActivity(launchIntent)
        true
      }
      .getOrDefault(false)
  }

  fun openGoogleCalendarForDate(date: LocalDate): Boolean {
    val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val builder: Uri.Builder =
      CalendarContract.CONTENT_URI.buildUpon().apply {
        appendPath("time")
        appendPath(millis.toString())
      }
    val googleCalendarIntent =
      Intent(Intent.ACTION_VIEW, builder.build()).apply {
        setPackage("com.google.android.calendar")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    val genericCalendarIntent =
      Intent(Intent.ACTION_VIEW, builder.build()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }

    return runCatching {
        context.startActivity(googleCalendarIntent)
        true
      }
      .getOrElse {
        runCatching {
            context.startActivity(genericCalendarIntent)
            true
          }
          .getOrDefault(false)
      }
  }
}
