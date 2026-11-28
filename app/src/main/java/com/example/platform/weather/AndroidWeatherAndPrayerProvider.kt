package com.example.platform.weather

import com.example.domain.model.CitySearchResult
import com.example.domain.model.PrayerSchedule
import com.example.domain.model.WeatherSnapshot
import com.example.domain.prayer.PrayerTimeCalculator
import com.example.domain.repository.SettingsRepository
import com.example.domain.repository.WeatherAndPrayerProvider
import com.example.platform.location.CoarseLocationHelper
import com.example.platform.prayer.PrayerAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AndroidWeatherAndPrayerProvider(
  private val settingsRepository: SettingsRepository,
  private val weatherClient: OpenMeteoWeatherClient,
  private val locationHelper: CoarseLocationHelper,
  private val prayerAlarmScheduler: PrayerAlarmScheduler,
) : WeatherAndPrayerProvider {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

  private val _weather = MutableStateFlow(WeatherSnapshot())
  override val weather: StateFlow<WeatherSnapshot> = _weather.asStateFlow()

  private val _prayerSchedule =
    MutableStateFlow(
      PrayerTimeCalculator.computeSchedule(
        cityName = "Jakarta",
        latitude = -6.2088,
        longitude = 106.8456,
      )
    )
  override val prayerSchedule: StateFlow<PrayerSchedule> = _prayerSchedule.asStateFlow()

  init {
    scope.launch {
      settingsRepository.settings.collect { settings ->
        val schedule =
          PrayerTimeCalculator.computeSchedule(
            cityName = settings.cityName,
            latitude = settings.cityLatitude,
            longitude = settings.cityLongitude,
          )
        _prayerSchedule.value = schedule
        prayerAlarmScheduler.scheduleNext(schedule, settings.prayerAlarmEnabled)

        if (_weather.value.updatedAtMs == 0L && settings.cachedWeatherTempC != null) {
          _weather.value =
            WeatherSnapshot(
              cityName = settings.cityName,
              latitude = settings.cityLatitude,
              longitude = settings.cityLongitude,
              temperatureC = settings.cachedWeatherTempC,
              weatherCode = settings.cachedWeatherCode,
              conditionLabel =
                settings.cachedWeatherCondition
                  ?: OpenMeteoWeatherClient.describeWmoCode(settings.cachedWeatherCode),
              windKmh = settings.cachedWeatherWindKmh,
              isDay = settings.cachedWeatherIsDay,
              updatedAtMs = settings.cachedWeatherUpdatedAtMs,
            )
        } else if (_weather.value.cityName != settings.cityName && !_weather.value.isLoading) {
          _weather.value =
            _weather.value.copy(
              cityName = settings.cityName,
              latitude = settings.cityLatitude,
              longitude = settings.cityLongitude,
            )
        }
      }
    }
  }

  override fun refreshSchedule() {
    scope.launch {
      val settings = settingsRepository.settings.first()
      val schedule =
        PrayerTimeCalculator.computeSchedule(
          cityName = settings.cityName,
          latitude = settings.cityLatitude,
          longitude = settings.cityLongitude,
        )
      _prayerSchedule.value = schedule
      prayerAlarmScheduler.scheduleNext(schedule, settings.prayerAlarmEnabled)
    }
  }

  override suspend fun refreshWeather(force: Boolean) {
    val settings = settingsRepository.settings.first()
    var targetCity = settings.cityName
    var targetLat = settings.cityLatitude
    var targetLon = settings.cityLongitude

    val current = _weather.value
    val cacheAgeMs = System.currentTimeMillis() - current.updatedAtMs
    if (!force && current.temperatureC != null && cacheAgeMs < 15 * 60_000L) {
      return
    }

    _weather.value =
      current.copy(
        cityName = targetCity,
        latitude = targetLat,
        longitude = targetLon,
        isLoading = true,
        errorMessage = null,
      )

    if (settings.useGpsLocation && locationHelper.hasPermission()) {
      locationHelper.getCurrentCoarseLocation()?.let { loc ->
        targetCity = loc.cityName
        targetLat = loc.latitude
        targetLon = loc.longitude
        settingsRepository.updateLocation(
          cityName = targetCity,
          latitude = targetLat,
          longitude = targetLon,
          useGpsLocation = true,
        )
      }
    }

    runCatching {
        weatherClient.fetchCurrentWeather(
          cityName = targetCity,
          latitude = targetLat,
          longitude = targetLon,
        )
      }
      .onSuccess { fetched ->
        _weather.value = fetched
        settingsRepository.cacheWeather(fetched)
      }
      .onFailure {
        _weather.value =
          _weather.value.copy(
            isLoading = false,
            errorMessage = "Gagal memuat cuaca. Periksa koneksi internet.",
          )
      }
  }

  override suspend fun searchCity(query: String): List<CitySearchResult> {
    return runCatching { weatherClient.searchCities(query) }.getOrDefault(emptyList())
  }

  override suspend fun selectCity(
    cityName: String,
    latitude: Double,
    longitude: Double,
    isGps: Boolean,
  ) {
    settingsRepository.updateLocation(
      cityName = cityName,
      latitude = latitude,
      longitude = longitude,
      useGpsLocation = isGps,
    )
    refreshSchedule()
    refreshWeather(force = true)
  }
}
