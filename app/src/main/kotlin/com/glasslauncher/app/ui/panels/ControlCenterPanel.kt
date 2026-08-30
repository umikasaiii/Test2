package com.glasslauncher.app.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glasslauncher.app.control.ControlCenterHelper
import com.glasslauncher.app.data.model.ControlCenterSettings
import com.glasslauncher.app.data.model.ControlCenterTile
import com.glasslauncher.app.glass.GlassSurface

@Composable
fun ControlCenterPanel(settings: ControlCenterSettings) {
    val context = LocalContext.current

    GlassSurface(
        modifier = Modifier.fillMaxWidth(settings.widthFraction).padding(top = 8.dp),
        cornerRadiusDp = settings.cornerRadiusDp,
        transparencyOverride = settings.transparency,
        blurRadiusDpOverride = settings.blurRadiusDp,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Controlli rapidi", color = Color.White, fontSize = 16.sp)
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                items(settings.tiles) { tile ->
                    ControlTile(tile, settings, context)
                }
            }
            if (settings.showBrightnessSlider) {
                SliderRow(
                    icon = Icons.Filled.BrightnessMedium,
                    initialValue = { ControlCenterHelper.brightnessFraction(context) },
                    onValueChange = { ControlCenterHelper.setBrightnessFraction(context, it) },
                )
            }
            if (settings.showVolumeSlider) {
                SliderRow(
                    icon = Icons.Filled.VolumeUp,
                    initialValue = { ControlCenterHelper.volumeFraction(context) },
                    onValueChange = { ControlCenterHelper.setVolumeFraction(context, it) },
                )
            }
        }
    }
}

@Composable
private fun ControlTile(tile: ControlCenterTile, settings: ControlCenterSettings, context: android.content.Context) {
    var active by remember(tile) {
        mutableStateOf(
            when (tile) {
                ControlCenterTile.WIFI -> ControlCenterHelper.isWifiOn(context)
                ControlCenterTile.BLUETOOTH -> ControlCenterHelper.isBluetoothOn(context)
                ControlCenterTile.FLASHLIGHT -> ControlCenterHelper.isTorchOn()
                ControlCenterTile.AIRPLANE -> ControlCenterHelper.isAirplaneModeOn(context)
                ControlCenterTile.DND -> ControlCenterHelper.isDndOn(context)
                ControlCenterTile.ROTATION_LOCK -> !ControlCenterHelper.isAutoRotateOn(context)
            },
        )
    }

    val (icon, label) = tileMeta(tile)

    Column(
        Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(settings.tileCornerRadiusDp.dp))
            .background(Color.White.copy(alpha = if (active) 0.22f else 0.08f))
            .clickable {
                when (tile) {
                    ControlCenterTile.WIFI -> context.startActivity(ControlCenterHelper.wifiPanelIntent())
                    ControlCenterTile.BLUETOOTH -> context.startActivity(ControlCenterHelper.bluetoothSettingsIntent())
                    ControlCenterTile.AIRPLANE -> context.startActivity(ControlCenterHelper.airplaneModeSettingsIntent())
                    ControlCenterTile.FLASHLIGHT -> active = ControlCenterHelper.toggleTorch(context)
                    ControlCenterTile.DND -> {
                        if (ControlCenterHelper.hasNotificationPolicyAccess(context)) {
                            ControlCenterHelper.toggleDnd(context)
                            active = ControlCenterHelper.isDndOn(context)
                        } else {
                            context.startActivity(ControlCenterHelper.requestNotificationPolicyAccessIntent())
                        }
                    }
                    ControlCenterTile.ROTATION_LOCK -> {
                        if (ControlCenterHelper.canWriteSystemSettings(context)) {
                            ControlCenterHelper.toggleAutoRotate(context)
                            active = !ControlCenterHelper.isAutoRotateOn(context)
                        } else {
                            context.startActivity(ControlCenterHelper.requestWriteSettingsIntent(context))
                        }
                    }
                }
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (active) Color.White else Color.White.copy(alpha = 0.6f))
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
    }
}

private fun tileMeta(tile: ControlCenterTile): Pair<ImageVector, String> = when (tile) {
    ControlCenterTile.WIFI -> Icons.Filled.Wifi to "Wi-Fi"
    ControlCenterTile.BLUETOOTH -> Icons.Filled.Bluetooth to "Bluetooth"
    ControlCenterTile.FLASHLIGHT -> Icons.Filled.FlashlightOn to "Torcia"
    ControlCenterTile.AIRPLANE -> Icons.Filled.AirplanemodeActive to "Aereo"
    ControlCenterTile.DND -> Icons.Filled.DoNotDisturbOn to "Non disturbare"
    ControlCenterTile.ROTATION_LOCK -> Icons.Filled.ScreenLockRotation to "Rotazione"
}

@Composable
private fun SliderRow(icon: ImageVector, initialValue: () -> Float, onValueChange: (Float) -> Unit) {
    var value by remember { mutableStateOf(initialValue()) }
    Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.75f))
        Slider(
            value = value,
            onValueChange = { value = it; onValueChange(it) },
            modifier = Modifier.weight(1f).padding(start = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.8f),
                inactiveTrackColor = Color.White.copy(alpha = 0.2f),
            ),
        )
    }
}
