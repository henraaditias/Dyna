package com.example.domain.model

import android.graphics.Bitmap

data class WeatherSnapshot(
  val cityName: String = "Jakarta",
  val latitude: Double = -6.2088,
  val longitude: Double = 106.8456,
  val temperatureC: Float? = null,
  val weatherCode: Int? = null,
  val conditionLabel: String = "Belum dimuat",
  val windKmh: Float? = null,
  val isDay: Boolean = true,
  val updatedAtMs: Long = 0L,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
)

data class CitySearchResult(
  val name: String,
  val country: String,
  val admin1: String?,
  val latitude: Double,
  val longitude: Double,
) {
  val displayLabel: String
    get() = listOfNotNull(name, admin1?.takeIf { it.isNotBlank() }, country).joinToString(", ")
}

enum class PrayerName(val label: String) {
  SUBUH("Subuh"),
  DZUHUR("Dzuhur"),
  ASHAR("Ashar"),
  MAGHRIB("Maghrib"),
  ISYA("Isya"),
}

data class PrayerTimeEntry(
  val name: PrayerName,
  val hour: Int,
  val minute: Int,
  val formattedTime: String,
  val triggerAtMillis: Long,
  val isPassed: Boolean,
)

data class PrayerSchedule(
  val cityName: String = "Jakarta",
  val dateLabel: String = "",
  val entries: List<PrayerTimeEntry> = emptyList(),
  val nextPrayer: PrayerTimeEntry? = null,
  val countdownLabel: String? = null,
)

data class BatterySnapshot(
  val levelPercent: Int = 100,
  val isCharging: Boolean = false,
  val isLow: Boolean = false,
  val pluggedTypeLabel: String = "Baterai",
)

data class MediaSnapshot(
  val packageName: String,
  val appName: String,
  val title: String,
  val artist: String?,
  val isPlaying: Boolean,
  val durationMs: Long = 0L,
  val positionMs: Long = 0L,
  val hasPrevious: Boolean = true,
  val hasNext: Boolean = true,
  val albumArt: Bitmap? = null,
  val hasFavoriteAction: Boolean = false,
  val isFavorited: Boolean = false,
)

data class CallSnapshot(
  val key: String,
  val appName: String,
  val callerName: String,
  val statusLabel: String,
  val isVideoCall: Boolean = false,
  val isMuted: Boolean = false,
  val isVideoActive: Boolean = false,
  val hasAnswerAction: Boolean = false,
  val hasDeclineAction: Boolean = false,
)

data class ShortcutAppItem(
  val packageName: String,
  val label: String,
)

data class NotificationHistoryEntry(
  val id: String,
  val packageName: String,
  val appName: String,
  val title: String,
  val body: String,
  val timestampMs: Long,
  val dayKey: String,
  val isChat: Boolean = false,
)
