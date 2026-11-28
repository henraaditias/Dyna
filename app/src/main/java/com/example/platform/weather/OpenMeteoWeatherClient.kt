package com.example.platform.weather

import com.example.domain.model.CitySearchResult
import com.example.domain.model.WeatherSnapshot
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class OpenMeteoWeatherClient {

  suspend fun fetchCurrentWeather(
    cityName: String,
    latitude: Double,
    longitude: Double,
  ): WeatherSnapshot =
    withContext(Dispatchers.IO) {
      val urlString =
        "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,weather_code,wind_speed_10m,is_day&timezone=auto"
      val jsonText = executeGet(urlString)
      val root = JSONObject(jsonText)
      val current = root.getJSONObject("current")
      val tempC = current.optDouble("temperature_2m", Double.NaN).toFloat()
      val code = current.optInt("weather_code", -1)
      val wind = current.optDouble("wind_speed_10m", 0.0).toFloat()
      val isDay = current.optInt("is_day", 1) == 1

      WeatherSnapshot(
        cityName = cityName,
        latitude = latitude,
        longitude = longitude,
        temperatureC = if (tempC.isNaN()) null else tempC,
        weatherCode = code,
        conditionLabel = describeWmoCode(code),
        windKmh = wind,
        isDay = isDay,
        updatedAtMs = System.currentTimeMillis(),
        isLoading = false,
        errorMessage = null,
      )
    }

  suspend fun searchCities(query: String): List<CitySearchResult> =
    withContext(Dispatchers.IO) {
      val trimmed = query.trim()
      if (trimmed.length < 2) return@withContext emptyList()
      val encoded = URLEncoder.encode(trimmed, "UTF-8")
      val urlString =
        "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=6&language=id&format=json"
      val jsonText = executeGet(urlString)
      val root = JSONObject(jsonText)
      val results = root.optJSONArray("results") ?: return@withContext emptyList()

      buildList {
        for (i in 0 until results.length()) {
          val obj = results.getJSONObject(i)
          add(
            CitySearchResult(
              name = obj.optString("name"),
              country = obj.optString("country"),
              admin1 = obj.optString("admin1").takeIf { it.isNotBlank() },
              latitude = obj.getDouble("latitude"),
              longitude = obj.getDouble("longitude"),
            )
          )
        }
      }
    }

  private fun executeGet(urlString: String): String {
    val connection =
      (URL(urlString).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 8_000
        readTimeout = 8_000
        setRequestProperty("Accept", "application/json")
      }
    return try {
      val code = connection.responseCode
      val stream = if (code in 200..299) connection.inputStream else connection.errorStream
      val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
      if (code !in 200..299) {
        error("HTTP $code")
      }
      body
    } finally {
      connection.disconnect()
    }
  }

  companion object {
    fun describeWmoCode(code: Int?): String =
      when (code) {
        0 -> "Cerah"
        1, 2 -> "Cerah Berawan"
        3 -> "Berawan"
        45, 48 -> "Berkabut"
        51, 53, 55, 56, 57 -> "Gerimis"
        61, 63, 65, 66, 67, 80, 81, 82 -> "Hujan"
        71, 73, 75, 77, 85, 86 -> "Salju"
        95, 96, 99 -> "Badai Petir"
        else -> "Cuaca Terkini"
      }
  }
}
