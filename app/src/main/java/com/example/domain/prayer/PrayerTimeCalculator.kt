package com.example.domain.prayer

import com.example.domain.model.PrayerName
import com.example.domain.model.PrayerSchedule
import com.example.domain.model.PrayerTimeEntry
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Pure Kotlin offline astronomical prayer time calculator (Kemenag RI / standard Shafi'i angles:
 * Fajr 20 deg, Isha 18 deg, Asr shadow factor = 1).
 */
object PrayerTimeCalculator {

  fun computeSchedule(
    cityName: String,
    latitude: Double,
    longitude: Double,
    nowMillis: Long = System.currentTimeMillis(),
    timeZone: TimeZone = TimeZone.getDefault(),
  ): PrayerSchedule {
    val cal = Calendar.getInstance(timeZone).apply { timeInMillis = nowMillis }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1
    val day = cal.get(Calendar.DAY_OF_MONTH)
    val tzOffsetHours = timeZone.getOffset(nowMillis) / 3_600_000.0

    val jd = julianDate(year, month, day) - longitude / (15.0 * 24.0)
    val (declinationDeg, eqTimeHours) = sunPosition(jd)

    val dhuhrHours = 12.0 + tzOffsetHours - longitude / 15.0 - eqTimeHours + (2.0 / 60.0)
    val subuhHours =
      dhuhrHours - sunHourAngleHours(latitude, declinationDeg, angleBelowHorizon = 20.0)
    val asharHours = dhuhrHours + asrHourAngleHours(latitude, declinationDeg, shadowFactor = 1.0)
    val maghribHours =
      dhuhrHours + sunHourAngleHours(latitude, declinationDeg, angleBelowHorizon = 0.833)
    val isyaHours =
      dhuhrHours + sunHourAngleHours(latitude, declinationDeg, angleBelowHorizon = 18.0)

    val rawTimes =
      listOf(
        PrayerName.SUBUH to subuhHours,
        PrayerName.DZUHUR to dhuhrHours,
        PrayerName.ASHAR to asharHours,
        PrayerName.MAGHRIB to maghribHours,
        PrayerName.ISYA to isyaHours,
      )

    val entries =
      rawTimes.map { (name, fractionalHours) ->
        val normalized = normalizeHours(fractionalHours)
        val totalMinutes = kotlin.math.round(normalized * 60.0).toInt().coerceIn(0, 23 * 60 + 59)
        val hour = totalMinutes / 60
        val minute = totalMinutes % 60
        val entryCal =
          (cal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
          }
        val triggerMillis = entryCal.timeInMillis
        PrayerTimeEntry(
          name = name,
          hour = hour,
          minute = minute,
          formattedTime = String.format(Locale.US, "%02d:%02d", hour, minute),
          triggerAtMillis = triggerMillis,
          isPassed = triggerMillis <= nowMillis,
        )
      }

    var nextEntry = entries.firstOrNull { !it.isPassed }
    if (nextEntry == null && entries.isNotEmpty()) {
      val tomorrowCal =
        (cal.clone() as Calendar).apply {
          add(Calendar.DAY_OF_YEAR, 1)
        }
      val tomorrowYear = tomorrowCal.get(Calendar.YEAR)
      val tomorrowMonth = tomorrowCal.get(Calendar.MONTH) + 1
      val tomorrowDay = tomorrowCal.get(Calendar.DAY_OF_MONTH)
      val tomorrowOffset = timeZone.getOffset(tomorrowCal.timeInMillis) / 3_600_000.0
      val jdTomorrow = julianDate(tomorrowYear, tomorrowMonth, tomorrowDay) - longitude / (15.0 * 24.0)
      val (decTom, eqTom) = sunPosition(jdTomorrow)
      val dhuhrTom = 12.0 + tomorrowOffset - longitude / 15.0 - eqTom + (2.0 / 60.0)
      val subuhTom = normalizeHours(dhuhrTom - sunHourAngleHours(latitude, decTom, 20.0))
      val totalMin = kotlin.math.round(subuhTom * 60.0).toInt().coerceIn(0, 23 * 60 + 59)
      val hour = totalMin / 60
      val minute = totalMin % 60
      tomorrowCal.set(Calendar.HOUR_OF_DAY, hour)
      tomorrowCal.set(Calendar.MINUTE, minute)
      tomorrowCal.set(Calendar.SECOND, 0)
      tomorrowCal.set(Calendar.MILLISECOND, 0)
      nextEntry =
        PrayerTimeEntry(
          name = PrayerName.SUBUH,
          hour = hour,
          minute = minute,
          formattedTime = String.format(Locale.US, "%02d:%02d", hour, minute),
          triggerAtMillis = tomorrowCal.timeInMillis,
          isPassed = false,
        )
    }

    val countdown =
      nextEntry?.let { entry ->
        val diffMins = ((entry.triggerAtMillis - nowMillis) / 60_000L).coerceAtLeast(0L)
        val h = diffMins / 60
        val m = diffMins % 60
        if (h > 0) "${h}j ${m}m" else "${m}m"
      }

    val dateLabel = String.format(Locale.US, "%02d/%02d/%04d", day, month, year)
    return PrayerSchedule(
      cityName = cityName,
      dateLabel = dateLabel,
      entries = entries,
      nextPrayer = nextEntry,
      countdownLabel = countdown,
    )
  }

  private fun julianDate(year: Int, month: Int, day: Int): Double {
    var y = year
    var m = month
    if (m <= 2) {
      y -= 1
      m += 12
    }
    val a = floor(y / 100.0)
    val b = 2 - a + floor(a / 4.0)
    return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
  }

  private fun sunPosition(jd: Double): Pair<Double, Double> {
    val d = jd - 2451545.0
    val g = fixAngle(357.529 + 0.98560028 * d)
    val q = fixAngle(280.459 + 0.98564736 * d)
    val l = fixAngle(q + 1.915 * sinDeg(g) + 0.020 * sinDeg(2.0 * g))
    val e = 23.439 - 0.00000036 * d

    val ra = atan2Deg(cosDeg(e) * sinDeg(l), cosDeg(l)) / 15.0
    val eqTime = q / 15.0 - fixHours(ra)
    val decl = asinDeg(sinDeg(e) * sinDeg(l))
    return decl to eqTime
  }

  private fun sunHourAngleHours(
    latitude: Double,
    declinationDeg: Double,
    angleBelowHorizon: Double,
  ): Double {
    val numerator = -sinDeg(angleBelowHorizon) - sinDeg(latitude) * sinDeg(declinationDeg)
    val denominator = cosDeg(latitude) * cosDeg(declinationDeg)
    val ratio = (numerator / denominator).coerceIn(-1.0, 1.0)
    return acosDeg(ratio) / 15.0
  }

  private fun asrHourAngleHours(
    latitude: Double,
    declinationDeg: Double,
    shadowFactor: Double,
  ): Double {
    val angleBelowHorizon = -arccotDeg(shadowFactor + tanDeg(kotlin.math.abs(latitude - declinationDeg)))
    return sunHourAngleHours(latitude, declinationDeg, angleBelowHorizon)
  }

  private fun normalizeHours(hours: Double): Double {
    var result = hours % 24.0
    if (result < 0) result += 24.0
    return result
  }

  private fun fixAngle(deg: Double): Double {
    var a = deg % 360.0
    if (a < 0) a += 360.0
    return a
  }

  private fun fixHours(h: Double): Double {
    var a = h % 24.0
    if (a < 0) a += 24.0
    return a
  }

  private fun degToRad(d: Double): Double = d * PI / 180.0

  private fun radToDeg(r: Double): Double = r * 180.0 / PI

  private fun sinDeg(d: Double): Double = sin(degToRad(d))

  private fun cosDeg(d: Double): Double = cos(degToRad(d))

  private fun tanDeg(d: Double): Double = tan(degToRad(d))

  private fun asinDeg(x: Double): Double = radToDeg(asin(x.coerceIn(-1.0, 1.0)))

  private fun acosDeg(x: Double): Double = radToDeg(acos(x.coerceIn(-1.0, 1.0)))

  private fun atan2Deg(y: Double, x: Double): Double = radToDeg(atan2(y, x))

  private fun arccotDeg(x: Double): Double = radToDeg(atan(1.0 / x))
}
