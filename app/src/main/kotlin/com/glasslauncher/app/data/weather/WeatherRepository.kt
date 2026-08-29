package com.glasslauncher.app.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.resume

data class WeatherResult(
    val temperatureC: Double,
    val weatherCode: Int,
    val cityName: String,
)

data class GeocodedCity(val name: String, val lat: Double, val lon: Double)

/**
 * Talks to Open-Meteo (open-meteo.com): free, key-less forecast + geocoding APIs. Used instead
 * of a commercial provider so the launcher works out of the box with zero configuration; the
 * provider is isolated behind this class so it can be swapped for another in Settings later.
 */
class WeatherRepository(private val context: Context) {

    private val client = OkHttpClient()

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    suspend fun lastKnownLocation(): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return@withContext null
        val providers = lm.getProviders(true)
        var best: android.location.Location? = null
        for (provider in providers) {
            val loc = runCatching { lm.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (best == null || loc.accuracy < best!!.accuracy) best = loc
        }
        best?.let { it.latitude to it.longitude }
    }

    suspend fun geocodeCity(query: String): GeocodedCity? = withContext(Dispatchers.IO) {
        val url = "https://geocoding-api.open-meteo.com/v1/search?count=1&name=" +
            java.net.URLEncoder.encode(query, "UTF-8")
        val body = runCatching { get(url) }.getOrNull() ?: return@withContext null
        val results = JSONObject(body).optJSONArray("results") ?: return@withContext null
        if (results.length() == 0) return@withContext null
        val first = results.getJSONObject(0)
        GeocodedCity(
            name = first.optString("name", query),
            lat = first.optDouble("latitude"),
            lon = first.optDouble("longitude"),
        )
    }

    suspend fun currentWeather(lat: Double, lon: Double, cityLabel: String): WeatherResult? = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code"
        val body = runCatching { get(url) }.getOrNull() ?: return@withContext null
        val current = JSONObject(body).optJSONObject("current") ?: return@withContext null
        WeatherResult(
            temperatureC = current.optDouble("temperature_2m", Double.NaN),
            weatherCode = current.optInt("weather_code", -1),
            cityName = cityLabel,
        )
    }

    private suspend fun get(url: String): String? = suspendCancellableCoroutine { cont ->
        val call = client.newCall(Request.Builder().url(url).build())
        cont.invokeOnCancellation { call.cancel() }
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                if (cont.isActive) cont.resume(null)
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val text = response.use { it.body?.string() }
                if (cont.isActive) cont.resume(text)
            }
        })
    }
}

/** Maps Open-Meteo's WMO weather codes to a short human label + a simple symbol name. */
object WeatherCodeMapper {
    fun label(code: Int): String = when (code) {
        0 -> "Sereno"
        1, 2 -> "Poco nuvoloso"
        3 -> "Nuvoloso"
        45, 48 -> "Nebbia"
        51, 53, 55, 56, 57 -> "Pioviggine"
        61, 63, 65, 66, 67 -> "Pioggia"
        71, 73, 75, 77 -> "Neve"
        80, 81, 82 -> "Rovesci"
        85, 86 -> "Rovesci di neve"
        95, 96, 99 -> "Temporale"
        else -> "N/D"
    }

    fun symbol(code: Int): String = when (code) {
        0 -> "☀"
        1, 2 -> "⛅"
        3 -> "☁"
        45, 48 -> "🌫"
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> "🌧"
        71, 73, 75, 77, 85, 86 -> "❄"
        95, 96, 99 -> "⛈"
        else -> "☁"
    }
}
