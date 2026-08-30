package com.glasslauncher.app.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.settings.sections.AdaptiveGlassSection
import com.glasslauncher.app.ui.settings.sections.AdvancedSection
import com.glasslauncher.app.ui.settings.sections.AppDrawerSection
import com.glasslauncher.app.ui.settings.sections.AppearanceSection
import com.glasslauncher.app.ui.settings.sections.BackupSection
import com.glasslauncher.app.ui.settings.sections.ControlCenterSection
import com.glasslauncher.app.ui.settings.sections.DockSection
import com.glasslauncher.app.ui.settings.sections.FoldersSection
import com.glasslauncher.app.ui.settings.sections.GestureSection
import com.glasslauncher.app.ui.settings.sections.HomeSection
import com.glasslauncher.app.ui.settings.sections.IconsSection
import com.glasslauncher.app.ui.settings.sections.InfoSection
import com.glasslauncher.app.ui.settings.sections.NotificationsSection
import com.glasslauncher.app.ui.settings.sections.SearchSection
import com.glasslauncher.app.ui.settings.sections.WallpaperSection
import com.glasslauncher.app.ui.settings.sections.WidgetSection

private data class SectionEntry(val id: String, val title: String)

private val SECTIONS = listOf(
    SectionEntry("home", "HOME"),
    SectionEntry("icons", "ICONE"),
    SectionEntry("dock", "DOCK"),
    SectionEntry("folders", "CARTELLE"),
    SectionEntry("widgets", "WIDGET"),
    SectionEntry("search", "RICERCA"),
    SectionEntry("drawer", "APP DRAWER"),
    SectionEntry("controlcenter", "CENTRO DI CONTROLLO"),
    SectionEntry("notifications", "NOTIFICHE"),
    SectionEntry("gestures", "GESTURE"),
    SectionEntry("appearance", "ASPETTO"),
    SectionEntry("glass", "ADAPTIVE GLASS"),
    SectionEntry("wallpaper", "WALLPAPER"),
    SectionEntry("backup", "BACKUP"),
    SectionEntry("advanced", "AVANZATE"),
    SectionEntry("info", "INFORMAZIONI"),
)

@Composable
fun SettingsScreen(viewModel: LauncherViewModel, onClose: () -> Unit) {
    val navController = rememberNavController()
    Box(Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "list") {
            composable("list") { SettingsList(navController, onClose) }
            SECTIONS.forEach { section ->
                composable(section.id) {
                    SettingsDetail(section.title, onBack = { navController.popBackStack() }) {
                        RenderSection(section.id, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsList(navController: NavHostController, onClose: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val defaultLauncherLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
    ) {}

    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Chiudi", tint = Color.White)
            }
            Text("Impostazioni", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        LazyColumn(Modifier.fillMaxSize().padding(top = 72.dp, start = 16.dp, end = 16.dp)) {
            item {
                SettingsSectionCard {
                    SettingsButtonRow("Imposta come launcher predefinito") {
                        runCatching {
                            defaultLauncherLauncher.launch(
                                com.glasslauncher.app.launcher.DefaultLauncherHelper.requestDefaultLauncherIntent(context),
                            )
                        }
                    }
                }
            }
            items(SECTIONS) { section ->
                SettingsSectionCard {
                    SettingsButtonRow(section.title) { navController.navigate(section.id) }
                }
            }
        }
    }
}

@Composable
private fun SettingsDetail(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro", tint = Color.White)
            }
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        LazyColumn(Modifier.fillMaxSize().padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            item { content() }
        }
    }
}

@Composable
private fun RenderSection(id: String, viewModel: LauncherViewModel) {
    val settings by viewModel.settings.collectAsState()
    when (id) {
        "home" -> HomeSection(settings, viewModel)
        "icons" -> IconsSection(settings, viewModel)
        "dock" -> DockSection(settings, viewModel)
        "folders" -> FoldersSection(settings, viewModel)
        "widgets" -> WidgetSection(settings, viewModel)
        "search" -> SearchSection(settings, viewModel)
        "drawer" -> AppDrawerSection(settings, viewModel)
        "controlcenter" -> ControlCenterSection(settings, viewModel)
        "notifications" -> NotificationsSection(settings, viewModel)
        "gestures" -> GestureSection(settings, viewModel)
        "appearance" -> AppearanceSection(settings, viewModel)
        "glass" -> AdaptiveGlassSection(settings, viewModel)
        "wallpaper" -> WallpaperSection(viewModel)
        "backup" -> BackupSection(viewModel)
        "advanced" -> AdvancedSection(viewModel)
        "info" -> InfoSection()
    }
}
