package com.glasslauncher.app.ui.settings.sections

import androidx.compose.runtime.Composable
import com.glasslauncher.app.data.model.IconRenderMode
import com.glasslauncher.app.data.model.IconShape
import com.glasslauncher.app.data.model.LauncherSettings
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.settings.SettingsChoiceRow
import com.glasslauncher.app.ui.settings.SettingsSectionCard
import com.glasslauncher.app.ui.settings.SettingsSliderRow
import com.glasslauncher.app.ui.settings.SettingsSwitchRow

@Composable
fun HomeSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val g = settings.homeGrid
    SettingsSectionCard("GRIGLIA") {
        SettingsChoiceRow(
            "Preset griglia",
            listOf("4x6", "4x7", "5x7", "5x8", "6x8"),
            selectedIndex = listOf(4 to 6, 4 to 7, 5 to 7, 5 to 8, 6 to 8).indexOfFirst { it.first == g.columns && it.second == g.rows }.coerceAtLeast(-1),
        ) { index ->
            val (cols, rows) = listOf(4 to 6, 4 to 7, 5 to 7, 5 to 8, 6 to 8)[index]
            vm.updateSettings { it.copy(homeGrid = it.homeGrid.copy(columns = cols, rows = rows)) }
        }
        SettingsSliderRow("Colonne", g.columns.toFloat(), 3f..7f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(columns = it.toInt())) } }, valueText = { it.toInt().toString() })
        SettingsSliderRow("Righe", g.rows.toFloat(), 4f..9f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(rows = it.toInt())) } }, valueText = { it.toInt().toString() })
        SettingsSliderRow("Numero pagine", g.pageCount.toFloat(), 1f..8f, {
            vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(pageCount = it.toInt())) }
            vm.setPageCount(it.toInt())
        }, valueText = { it.toInt().toString() })
    }
    SettingsSectionCard("MARGINI E SPAZIATURE") {
        SettingsSliderRow("Margine superiore", g.marginTopDp, 0f..160f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(marginTopDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Margine inferiore", g.marginBottomDp, 0f..160f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(marginBottomDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Margini laterali", g.marginHorizontalDp, 0f..48f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(marginHorizontalDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Distanza orizzontale icone", g.horizontalSpacingDp, 0f..40f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(horizontalSpacingDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Distanza verticale icone", g.verticalSpacingDp, 0f..40f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(verticalSpacingDp = it)) } }, valueText = { "${it.toInt()}dp" })
    }
    SettingsSectionCard("ICONE ED ETICHETTE") {
        SettingsSliderRow("Dimensione icone", g.iconSizeDp, 32f..96f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(iconSizeDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Dimensione testo", g.labelSizeSp, 8f..20f, { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(labelSizeSp = it)) } }, valueText = { "${it.toInt()}sp" })
        SettingsSwitchRow("Mostra etichette", g.showLabels) { vm.updateSettings { s -> s.copy(homeGrid = s.homeGrid.copy(showLabels = it)) } }
    }
}

@Composable
fun IconsSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val i = settings.icons
    SettingsSectionCard("MODALITÀ ICONA PREDEFINITA") {
        SettingsChoiceRow(
            "Modalità",
            listOf("Originale", "Glass auto", "Tema custom", "Immagine manuale"),
            selectedIndex = IconRenderMode.entries.indexOf(i.defaultRenderMode),
        ) { index -> vm.updateSettings { s -> s.copy(icons = s.icons.copy(defaultRenderMode = IconRenderMode.entries[index])) } }
        SettingsChoiceRow(
            "Forma",
            listOf("Squircle", "Cerchio", "Quadrato arr.", "Teardrop"),
            selectedIndex = IconShape.entries.indexOf(i.shape),
        ) { index -> vm.updateSettings { s -> s.copy(icons = s.icons.copy(shape = IconShape.entries[index])) } }
    }
    SettingsSectionCard("DIMENSIONI") {
        SettingsSliderRow("Dimensione simbolo", i.symbolScale, 0.3f..1f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(symbolScale = it)) } })
        SettingsSliderRow("Dimensione tile", i.tileSizeDp, 32f..96f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(tileSizeDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Corner radius", i.cornerRadiusDp, 0f..48f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(cornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Scala", i.scale, 0.5f..1.5f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(scale = it)) } })
        SettingsSliderRow("Padding", i.paddingDp, 0f..24f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(paddingDp = it)) } }, valueText = { "${it.toInt()}dp" })
    }
    SettingsSectionCard("MATERIALE GLASS ICONA") {
        SettingsSliderRow("Trasparenza", i.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(transparency = it)) } })
        SettingsSliderRow("Glow", i.glow, 0f..1f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(glow = it)) } })
        SettingsSliderRow("Bordo", i.borderIntensity, 0f..1f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(borderIntensity = it)) } })
        SettingsSliderRow("Sfondo", i.backgroundTint, 0f..1f, { vm.updateSettings { s -> s.copy(icons = s.icons.copy(backgroundTint = it)) } })
    }
}
