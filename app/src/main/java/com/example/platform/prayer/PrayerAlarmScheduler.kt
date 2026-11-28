package com.example.platform.prayer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.domain.model.PrayerSchedule

class PrayerAlarmScheduler(private val context: Context) {

  private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

  fun scheduleNext(schedule: PrayerSchedule, enabled: Boolean) {
    val manager = alarmManager ?: return
    val next = schedule.nextPrayer
    val intent = Intent(context, PrayerAlarmReceiver::class.java)

    val existing =
      PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
      )
    if (existing != null) {
      manager.cancel(existing)
    }

    if (!enabled || next == null) return

    val alarmIntent =
      Intent(context, PrayerAlarmReceiver::class.java).apply {
        putExtra(EXTRA_PRAYER_NAME, next.name.name)
        putExtra(EXTRA_FORMATTED_TIME, next.formattedTime)
        putExtra(EXTRA_CITY_NAME, schedule.cityName)
      }
    val pendingIntent =
      PendingIntent.getBroadcast(
        context,
        REQUEST_CODE,
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    runCatching {
      manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.triggerAtMillis, pendingIntent)
    }
  }

  companion object {
    const val REQUEST_CODE = 4401
    const val EXTRA_PRAYER_NAME = "prayer_name"
    const val EXTRA_FORMATTED_TIME = "formatted_time"
    const val EXTRA_CITY_NAME = "city_name"
  }
}
