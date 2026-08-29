package com.glasslauncher.app.ui.settings.sections

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.glasslauncher.app.BuildConfig
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.settings.SettingsButtonRow
import com.glasslauncher.app.ui.settings.SettingsSectionCard
import androidx.compose.foundation.layout.padding

@Composable
fun WallpaperSection(vm: LauncherViewModel) {
    val pickWallpaper = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        vm.refreshWallpaper()
    }
    SettingsSectionCard("WALLPAPER") {
        SettingsButtonRow("Scegli wallpaper di sistema") {
            runCatching { pickWallpaper.launch(Intent(Intent.ACTION_SET_WALLPAPER)) }
        }
        SettingsButtonRow("Aggiorna Adaptive Glass ora") { vm.refreshWallpaper() }
        Text(
            "Il wallpaper non fa parte del tema: puoi cambiarlo in ogni momento da qui o dalle impostazioni di sistema; i colori del vetro si adatteranno automaticamente.",
            color = Color.White.copy(alpha = 0.55f),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
        )
    }
}

@Composable
fun BackupSection(vm: LauncherViewModel) {
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportBackup(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importBackup(uri)
    }

    SettingsSectionCard("BACKUP E RIPRISTINO") {
        SettingsButtonRow("Esporta configurazione") { exportLauncher.launch("glass-launcher-backup.json") }
        SettingsButtonRow("Importa configurazione") { importLauncher.launch(arrayOf("application/json")) }
        SettingsButtonRow("Ripristina preset \"Glass Reference\"") { vm.resetToPreset() }
        SettingsButtonRow("Ripristina layout Home", destructive = true) { vm.clearHomeLayout() }
        SettingsButtonRow("Reset completo (layout + impostazioni)", destructive = true) { vm.resetEverything() }
    }
}

@Composable
fun AdvancedSection(vm: LauncherViewModel) {
    val layout by vm.layout.collectAsState()
    val apps by vm.apps.collectAsState()
    val hiddenInfos = apps.filter { it.key in layout.hiddenApps }

    SettingsSectionCard("APP") {
        SettingsButtonRow("Aggiorna elenco app installate") { vm.refreshInstalledApps() }
    }
    SettingsSectionCard("APP NASCOSTE (${hiddenInfos.size})") {
        if (hiddenInfos.isEmpty()) {
            Text(
                "Nessuna app nascosta. Tieni premuto un'app nell'App Drawer per nasconderla.",
                color = Color.White.copy(alpha = 0.55f),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            )
        } else {
            hiddenInfos.forEach { info ->
                SettingsButtonRow("Mostra di nuovo: ${info.label}") { vm.toggleHidden(info.key) }
            }
        }
    }
}

@Composable
fun InfoSection() {
    SettingsSectionCard("INFORMAZIONI") {
        Text("Glass Launcher", color = Color.White, modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp))
        Text(
            "Versione ${BuildConfig.VERSION_NAME}",
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
        )
        Text(
            "Launcher Android indipendente in stile \"Adaptive Glass\". Non è affiliato con Google " +
                "né con altri produttori di dispositivi o app citati nelle impostazioni di ricerca.",
            color = Color.White.copy(alpha = 0.55f),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
        )
    }
}
