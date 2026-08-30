package com.glasslauncher.app.ui.settings.sections

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.glasslauncher.app.control.ControlCenterHelper
import com.glasslauncher.app.data.model.ControlCenterTile
import com.glasslauncher.app.data.model.LauncherSettings
import com.glasslauncher.app.notifications.LauncherNotificationListenerService
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.settings.SettingsButtonRow
import com.glasslauncher.app.ui.settings.SettingsSectionCard
import com.glasslauncher.app.ui.settings.SettingsSliderRow
import com.glasslauncher.app.ui.settings.SettingsSwitchRow

@Composable
fun ControlCenterSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val cc = settings.controlCenter
    val context = LocalContext.current

    SettingsSectionCard("CENTRO DI CONTROLLO") {
        SettingsSwitchRow("Attivo", cc.enabled) { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(enabled = it)) } }
        SettingsSwitchRow("Mostra slider luminosità", cc.showBrightnessSlider) { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(showBrightnessSlider = it)) } }
        SettingsSwitchRow("Mostra slider volume", cc.showVolumeSlider) { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(showVolumeSlider = it)) } }
        SettingsSliderRow("Larghezza", cc.widthFraction, 0.5f..1f, { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(widthFraction = it)) } }, valueText = { "${(it * 100).toInt()}%" })
        SettingsSliderRow("Raggio angoli pannello", cc.cornerRadiusDp, 0f..48f, { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(cornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Raggio angoli tile", cc.tileCornerRadiusDp, 0f..40f, { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(tileCornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Blur", cc.blurRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Trasparenza", cc.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(controlCenter = s.controlCenter.copy(transparency = it)) } })
    }

    SettingsSectionCard("TOGGLE ATTIVI") {
        ControlCenterTile.entries.forEach { tile ->
            SettingsSwitchRow(tileLabel(tile), tile in cc.tiles) { enabled ->
                vm.updateSettings { s ->
                    val newTiles = if (enabled) s.controlCenter.tiles + tile else s.controlCenter.tiles - tile
                    s.copy(controlCenter = s.controlCenter.copy(tiles = newTiles))
                }
            }
        }
    }

    SettingsSectionCard("PERMESSI") {
        Text(
            "Wi-Fi, Bluetooth e modalità aereo aprono il pannello di sistema: da Android non è più " +
                "possibile per un'app attivarli/disattivarli direttamente. Torcia e volume sono controlli " +
                "reali e immediati. Luminosità, rotazione e Non disturbare richiedono un permesso speciale, " +
                "una sola volta.",
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
        )
        SettingsButtonRow("Concedi modifica impostazioni di sistema (luminosità/rotazione)") {
            context.startActivity(ControlCenterHelper.requestWriteSettingsIntent(context))
        }
        SettingsButtonRow("Concedi accesso normativa notifiche (Non disturbare)") {
            context.startActivity(ControlCenterHelper.requestNotificationPolicyAccessIntent())
        }
    }
}

private fun tileLabel(tile: ControlCenterTile): String = when (tile) {
    ControlCenterTile.WIFI -> "Wi-Fi"
    ControlCenterTile.BLUETOOTH -> "Bluetooth"
    ControlCenterTile.FLASHLIGHT -> "Torcia"
    ControlCenterTile.AIRPLANE -> "Modalità aereo"
    ControlCenterTile.DND -> "Non disturbare"
    ControlCenterTile.ROTATION_LOCK -> "Blocco rotazione"
}

@Composable
fun NotificationsSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val np = settings.notificationPanel
    val context = LocalContext.current
    var accessGranted by remember { mutableStateOf(LauncherNotificationListenerService.isAccessGranted(context)) }
    val accessLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        accessGranted = LauncherNotificationListenerService.isAccessGranted(context)
    }

    SettingsSectionCard("PANNELLO NOTIFICHE") {
        SettingsSwitchRow("Attivo", np.enabled) { vm.updateSettings { s -> s.copy(notificationPanel = s.notificationPanel.copy(enabled = it)) } }
        SettingsSwitchRow("Mostra icona app", np.showAppIcon) { vm.updateSettings { s -> s.copy(notificationPanel = s.notificationPanel.copy(showAppIcon = it)) } }
        SettingsSliderRow("Larghezza", np.widthFraction, 0.5f..1f, { vm.updateSettings { s -> s.copy(notificationPanel = s.notificationPanel.copy(widthFraction = it)) } }, valueText = { "${(it * 100).toInt()}%" })
        SettingsSliderRow("Raggio angoli pannello", np.cornerRadiusDp, 0f..48f, { vm.updateSettings { s -> s.copy(notificationPanel = s.notificationPanel.copy(cornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Raggio angoli notifica", np.itemCornerRadiusDp, 0f..32f, { vm.updateSettings { s -> s.copy(notificationPanel = s.notificationPanel.copy(itemCornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Blur", np.blurRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(notificationPanel = s.notificationPanel.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Trasparenza", np.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(notificationPanel = s.notificationPanel.copy(transparency = it)) } })
    }

    SettingsSectionCard("ACCESSO") {
        Text(
            if (accessGranted) "Accesso alle notifiche concesso." else "Serve il permesso di accesso alle notifiche per mostrarle nel pannello.",
            color = if (accessGranted) Color(0xFF8BE28B) else Color(0xFFFFC069),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
        )
        if (!accessGranted) {
            SettingsButtonRow("Concedi accesso alle notifiche") {
                accessLauncher.launch(LauncherNotificationListenerService.requestAccessIntent())
            }
        }
    }
}
