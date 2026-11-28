package com.example.platform.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

data class CoarseLocationResult(
  val cityName: String,
  val latitude: Double,
  val longitude: Double,
)

class CoarseLocationHelper(private val context: Context) {

  fun hasPermission(): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
      PackageManager.PERMISSION_GRANTED
  }

  @SuppressLint("MissingPermission")
  suspend fun getCurrentCoarseLocation(): CoarseLocationResult? {
    if (!hasPermission()) return null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

    val lastKnown =
      listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
        .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { it.time }

    val freshLocation =
      if (lastKnown != null && System.currentTimeMillis() - lastKnown.time < 30 * 60_000L) {
        lastKnown
      } else {
        requestSingleNetworkLocation(manager) ?: lastKnown
      } ?: return null

    val cityName = resolveCityName(freshLocation.latitude, freshLocation.longitude)
    return CoarseLocationResult(
      cityName = cityName,
      latitude = freshLocation.latitude,
      longitude = freshLocation.longitude,
    )
  }

  @SuppressLint("MissingPermission")
  private suspend fun requestSingleNetworkLocation(manager: LocationManager): Location? {
    val provider =
      when {
        runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }
          .getOrDefault(false) -> LocationManager.NETWORK_PROVIDER
        runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }
          .getOrDefault(false) -> LocationManager.GPS_PROVIDER
        else -> return null
      }

    return withTimeoutOrNull(6_000L) {
      suspendCancellableCoroutine { cont ->
        val cancellation = androidx.core.os.CancellationSignal()
        cont.invokeOnCancellation { cancellation.cancel() }
        runCatching {
            LocationManagerCompat.getCurrentLocation(
              manager,
              provider,
              cancellation,
              ContextCompat.getMainExecutor(context),
            ) { location ->
              if (cont.isActive) cont.resume(location)
            }
          }
          .onFailure {
            if (cont.isActive) cont.resume(null)
          }
      }
    }
  }

  private suspend fun resolveCityName(latitude: Double, longitude: Double): String =
    withContext(Dispatchers.IO) {
      if (!Geocoder.isPresent()) return@withContext "Lokasi Saat Ini"
      runCatching {
          val geocoder = Geocoder(context, Locale("id", "ID"))
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
              geocoder.getFromLocation(latitude, longitude, 1) { list ->
                val addr = list.firstOrNull()
                val city =
                  addr?.locality
                    ?: addr?.subAdminArea
                    ?: addr?.adminArea
                    ?: "Lokasi Saat Ini"
                if (cont.isActive) cont.resume(city)
              }
            }
          } else {
            @Suppress("DEPRECATION")
            val addr = geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            addr?.locality ?: addr?.subAdminArea ?: addr?.adminArea ?: "Lokasi Saat Ini"
          }
        }
        .getOrDefault("Lokasi Saat Ini")
    }
}
