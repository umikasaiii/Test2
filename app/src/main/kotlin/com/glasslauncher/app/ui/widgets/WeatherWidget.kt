package com.glasslauncher.app.ui.widgets

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.glasslauncher.app.data.model.WeatherSource
import com.glasslauncher.app.data.model.WeatherWidgetSettings
import com.glasslauncher.app.data.weather.WeatherCodeMapper
import com.glasslauncher.app.data.weather.WeatherRepository
import com.glasslauncher.app.data.weather.WeatherResult
import com.glasslauncher.app.glass.GlassSurface
import kotlinx.coroutines.delay

/** Weather glass widget: icon, temperature, condition, city — refreshed every 15 minutes, never per-frame. */
@Composable
fun WeatherWidget(
    settings: WeatherWidgetSettings,
    repository: WeatherRepository,
    modifier: Modifier = Modifier,
) {
    if (!settings.enabled) return

    var result by remember { mutableStateOf<WeatherResult?>(null) }
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionGranted = granted
    }

    LaunchedEffect(settings.source, settings.manualCity, permissionGranted) {
        if (settings.source == WeatherSource.AUTO_LOCATION && !permissionGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        while (true) {
            val (lat, lon, label) = when (settings.source) {
                WeatherSource.AUTO_LOCATION -> {
                    val loc = repository.lastKnownLocation()
                    if (loc != null) Triple(loc.first, loc.second, "La mia posizione")
                    else Triple(settings.manualLat, settings.manualLon, settings.manualCity)
                }
                WeatherSource.MANUAL_CITY -> {
                    val geo = repository.geocodeCity(settings.manualCity)
                    if (geo != null) Triple(geo.lat, geo.lon, geo.name) else Triple(settings.manualLat, settings.manualLon, settings.manualCity)
                }
            }
            result = repository.currentWeather(lat, lon, label)
            delay(15 * 60 * 1000L)
        }
    }

    GlassSurface(
        modifier = modifier.fillMaxWidth(0.5f),
        transparencyOverride = settings.transparency,
        blurRadiusDpOverride = settings.blurRadiusDp,
        cornerRadiusDp = 28f,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val current = result
            BasicText(
                text = if (current != null) WeatherCodeMapper.symbol(current.weatherCode) else "…",
                style = TextStyle(fontSize = 28.sp),
            )
            Row {
                val tempC = current?.temperatureC
                val temp = if (tempC != null && !tempC.isNaN()) {
                    val value = if (settings.useCelsius) tempC else tempC * 9 / 5 + 32
                    "${value.toInt()}°${if (settings.useCelsius) "" else "F"}"
                } else "--°"
                Text(text = temp, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(
                text = current?.let { WeatherCodeMapper.label(it.weatherCode) } ?: "Caricamento…",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
            )
            Text(
                text = current?.cityName ?: settings.manualCity,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
            )
        }
    }
}
