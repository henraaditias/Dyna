package com.example.platform.prayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.DynaApplication
import com.example.domain.model.PrayerName

class PrayerAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    val app = context?.applicationContext as? DynaApplication ?: return
    val nameStr = intent?.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: return
    val prayerName = PrayerName.entries.firstOrNull { it.name == nameStr } ?: return
    val time = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_FORMATTED_TIME) ?: "--:--"
    val city = intent.getStringExtra(PrayerAlarmScheduler.EXTRA_CITY_NAME) ?: "Jakarta"

    val container = app.container
    container.activityRepository.triggerPrayerAlarm(
      prayerName = prayerName,
      formattedTime = time,
      cityName = city,
    )
    container.weatherAndPrayerProvider.refreshSchedule()
  }
}
