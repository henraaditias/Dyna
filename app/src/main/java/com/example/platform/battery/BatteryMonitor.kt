package com.example.platform.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import com.example.domain.model.BatterySnapshot
import com.example.domain.repository.ActivityRepository
import com.example.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BatteryMonitor(
  private val context: Context,
  private val activityRepository: ActivityRepository,
  private val settingsRepository: SettingsRepository,
  private val scope: CoroutineScope,
) {
  private var isRegistered = false

  private val receiver =
    object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        val batteryIntent =
          if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
            intent
          } else {
            this@BatteryMonitor.context.registerReceiver(
              null,
              IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            )
          }
        handleBatteryIntent(batteryIntent)
      }
    }

  fun start() {
    if (isRegistered) return
    val filter =
      IntentFilter().apply {
        addAction(Intent.ACTION_BATTERY_CHANGED)
        addAction(Intent.ACTION_POWER_CONNECTED)
        addAction(Intent.ACTION_POWER_DISCONNECTED)
        addAction(Intent.ACTION_BATTERY_LOW)
      }
    val sticky =
      ContextCompat.registerReceiver(
        context,
        receiver,
        filter,
        ContextCompat.RECEIVER_NOT_EXPORTED,
      )
    isRegistered = true
    handleBatteryIntent(sticky)
  }

  fun stop() {
    if (!isRegistered) return
    runCatching { context.unregisterReceiver(receiver) }
    isRegistered = false
  }

  private fun handleBatteryIntent(intent: Intent?) {
    if (intent == null) return
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
    val percent = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 100

    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging =
      status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    val pluggedLabel =
      when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "Pengisian Cepat AC"
        BatteryManager.BATTERY_PLUGGED_USB -> "Pengisian USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Pengisian Nirkabel"
        else -> if (isCharging) "Mengisi Daya" else "Baterai"
      }

    val snapshot =
      BatterySnapshot(
        levelPercent = percent,
        isCharging = isCharging,
        isLow = !isCharging && percent <= 15,
        pluggedTypeLabel = pluggedLabel,
      )

    scope.launch {
      val enabled = settingsRepository.settings.first().batteryActivityEnabled
      activityRepository.onBatteryUpdated(snapshot = snapshot, showChargingBanner = enabled)
    }
  }
}
