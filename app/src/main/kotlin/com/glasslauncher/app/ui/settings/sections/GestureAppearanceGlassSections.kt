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
import com.glasslauncher.app.data.model.GlassMode
import com.glasslauncher.app.data.model.LauncherSettings
import com.glasslauncher.app.gestures.GestureHelper
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.settings.SettingsButtonRow
import com.glasslauncher.app.ui.settings.SettingsChoiceRow
import com.glasslauncher.app.ui.settings.SettingsSectionCard
import com.glasslauncher.app.ui.settings.SettingsSliderRow
import com.glasslauncher.app.ui.settings.SettingsSwitchRow

@Composable
fun GestureSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val g = settings.gestures
    val context = LocalContext.current
    var adminActive by remember { mutableStateOf(GestureHelper.isLockAdminActive(context)) }
    val adminLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        adminActive = GestureHelper.isLockAdminActive(context)
    }

    SettingsSectionCard("GESTURE") {
        SettingsSwitchRow("Swipe up -> App drawer", g.swipeUpOpensDrawer) { vm.updateSettings { s -> s.copy(gestures = s.gestures.copy(swipeUpOpensDrawer = it)) } }
        SettingsSwitchRow("Swipe down -> Notifiche", g.swipeDownOpensNotifications) { vm.updateSettings { s -> s.copy(gestures = s.gestures.copy(swipeDownOpensNotifications = it)) } }
        SettingsSwitchRow("Pinch -> Modifica Home", g.pinchOpensHomeSettings) { vm.updateSettings { s -> s.copy(gestures = s.gestures.copy(pinchOpensHomeSettings = it)) } }
        SettingsSwitchRow("Pressione lunga -> Menu", g.longPressOpensEditMode) { vm.updateSettings { s -> s.copy(gestures = s.gestures.copy(longPressOpensEditMode = it)) } }
        SettingsSwitchRow("Doppio tap -> Blocca schermo", g.doubleTapLocksScreen) { enabled ->
            if (enabled && !adminActive) {
                adminLauncher.launch(GestureHelper.requestLockAdminActivation(context))
            }
            vm.updateSettings { s -> s.copy(gestures = s.gestures.copy(doubleTapLocksScreen = enabled)) }
        }
        if (settings.gestures.doubleTapLocksScreen && !adminActive) {
            Text(
                "Concedi i permessi di amministrazione dispositivo per attivare il blocco schermo.",
                color = Color(0xFFFFC069),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
fun AppearanceSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val glass = settings.glass
    SettingsSectionCard("MODALITÀ VETRO") {
        SettingsChoiceRow(
            "Chiaro / Scuro / Automatico",
            listOf("Chiaro", "Scuro", "Automatico"),
            GlassMode.entries.indexOf(glass.mode),
        ) { index -> vm.updateSettings { s -> s.copy(glass = s.glass.copy(mode = GlassMode.entries[index])) } }
    }
    SettingsSectionCard("PRESET") {
        SettingsButtonRow("Applica preset \"Glass Reference\"") { vm.updateSettings { LauncherSettings.GLASS_REFERENCE.copy() } }
    }
}

@Composable
fun AdaptiveGlassSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val glass = settings.glass
    SettingsSectionCard("ADAPTIVE GLASS") {
        SettingsSliderRow("Intensità blur", glass.blurRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Trasparenza", glass.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(transparency = it)) } })
        SettingsSliderRow("Opacità", glass.opacity, 0f..1f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(opacity = it)) } })
        SettingsSliderRow("Intensità riflessi", glass.reflectionIntensity, 0f..1f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(reflectionIntensity = it)) } })
        SettingsSliderRow("Intensità Adaptive Glass", glass.adaptiveGlassIntensity, 0f..1f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(adaptiveGlassIntensity = it)) } })
        SettingsSliderRow("Luminosità bordo", glass.edgeHighlightBrightness, 0f..1f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(edgeHighlightBrightness = it)) } })
        SettingsSliderRow("Ombra", glass.shadowIntensity, 0f..1f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(shadowIntensity = it)) } })
        SettingsSliderRow("Saturazione", glass.saturation, 0.5f..1.5f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(saturation = it)) } })
        SettingsSliderRow("Raggio angoli", glass.cornerRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(cornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Bagliore periferico", glass.peripheralGlow, 0f..0.6f, { vm.updateSettings { s -> s.copy(glass = s.glass.copy(peripheralGlow = it)) } })
        SettingsButtonRow("Reset Adaptive Glass") { vm.updateSettings { s -> s.copy(glass = LauncherSettings.GLASS_REFERENCE.glass) } }
    }
}
