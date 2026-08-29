package com.glasslauncher.app.ui.settings.sections

import androidx.compose.runtime.Composable
import com.glasslauncher.app.data.model.LauncherSettings
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.settings.SettingsSectionCard
import com.glasslauncher.app.ui.settings.SettingsSliderRow
import com.glasslauncher.app.ui.settings.SettingsSwitchRow

@Composable
fun DockSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val d = settings.dock
    SettingsSectionCard("DOCK") {
        SettingsSwitchRow("Dock attivo", d.enabled) { vm.updateSettings { s -> s.copy(dock = s.dock.copy(enabled = it)) } }
        SettingsSliderRow("Numero icone", d.slotCount.toFloat(), 3f..7f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(slotCount = it.toInt())) } }, valueText = { it.toInt().toString() })
        SettingsSliderRow("Altezza", d.heightDp, 60f..120f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(heightDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Larghezza", d.widthFraction, 0.6f..1f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(widthFraction = it)) } }, valueText = { "${(it * 100).toInt()}%" })
        SettingsSliderRow("Margine inferiore", d.marginBottomDp, 0f..64f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(marginBottomDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Dimensione icone", d.iconSizeDp, 32f..80f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(iconSizeDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Distanza icone", d.iconSpacingDp, 4f..40f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(iconSpacingDp = it)) } }, valueText = { "${it.toInt()}dp" })
    }
    SettingsSectionCard("MATERIALE") {
        SettingsSliderRow("Blur", d.blurRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Opacità", d.opacity, 0f..1f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(opacity = it)) } })
        SettingsSliderRow("Raggio angoli", d.cornerRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(cornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Bordo", d.borderIntensity, 0f..1f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(borderIntensity = it)) } })
        SettingsSliderRow("Riflesso adattivo", d.adaptiveReflection, 0f..1f, { vm.updateSettings { s -> s.copy(dock = s.dock.copy(adaptiveReflection = it)) } })
    }
}

@Composable
fun FoldersSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val f = settings.folders
    SettingsSectionCard("GRIGLIA CARTELLA") {
        SettingsSliderRow("Anteprima (NxN)", f.previewGrid.toFloat(), 2f..4f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(previewGrid = it.toInt())) } }, valueText = { "${it.toInt()}x${it.toInt()}" })
        SettingsSliderRow("Dimensione chiusa", f.closedSizeDp, 48f..96f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(closedSizeDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Padding", f.paddingDp, 4f..32f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(paddingDp = it)) } }, valueText = { "${it.toInt()}dp" })
    }
    SettingsSectionCard("MATERIALE") {
        SettingsSliderRow("Trasparenza", f.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(transparency = it)) } })
        SettingsSliderRow("Blur", f.blurRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Raggio angoli", f.cornerRadiusDp, 0f..48f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(cornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Bordo", f.borderIntensity, 0f..1f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(borderIntensity = it)) } })
        SettingsSliderRow("Glow", f.glow, 0f..1f, { vm.updateSettings { s -> s.copy(folders = s.folders.copy(glow = it)) } })
    }
}
