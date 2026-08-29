package com.glasslauncher.app.ui.settings.sections

import androidx.compose.runtime.Composable
import com.glasslauncher.app.data.model.ClockTimeFormat
import com.glasslauncher.app.data.model.LauncherSettings
import com.glasslauncher.app.data.model.SearchTarget
import com.glasslauncher.app.data.model.WeatherSource
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.settings.SettingsChoiceRow
import com.glasslauncher.app.ui.settings.SettingsSectionCard
import com.glasslauncher.app.ui.settings.SettingsSliderRow
import com.glasslauncher.app.ui.settings.SettingsSwitchRow
import com.glasslauncher.app.ui.settings.SettingsTextFieldRow

@Composable
fun WidgetSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val c = settings.clock
    val w = settings.weather
    SettingsSectionCard("OROLOGIO") {
        SettingsSwitchRow("Widget attivo", c.enabled) { vm.updateSettings { s -> s.copy(clock = s.clock.copy(enabled = it)) } }
        SettingsSwitchRow("Mostra giorno", c.showDay) { vm.updateSettings { s -> s.copy(clock = s.clock.copy(showDay = it)) } }
        SettingsSwitchRow("Mostra data", c.showDate) { vm.updateSettings { s -> s.copy(clock = s.clock.copy(showDate = it)) } }
        SettingsChoiceRow("Formato ora", listOf("24h", "12h"), if (c.timeFormat == ClockTimeFormat.H24) 0 else 1) { index ->
            vm.updateSettings { s -> s.copy(clock = s.clock.copy(timeFormat = if (index == 0) ClockTimeFormat.H24 else ClockTimeFormat.H12)) }
        }
        SettingsSliderRow("Peso font", c.fontWeight.toFloat(), 300f..900f, { vm.updateSettings { s -> s.copy(clock = s.clock.copy(fontWeight = it.toInt())) } }, valueText = { it.toInt().toString() })
        SettingsSliderRow("Dimensione ora", c.timeSizeSp, 28f..80f, { vm.updateSettings { s -> s.copy(clock = s.clock.copy(timeSizeSp = it)) } }, valueText = { "${it.toInt()}sp" })
        SettingsSliderRow("Dimensione giorno/data", c.dayDateSizeSp, 10f..24f, { vm.updateSettings { s -> s.copy(clock = s.clock.copy(dayDateSizeSp = it)) } }, valueText = { "${it.toInt()}sp" })
        SettingsChoiceRow("Allineamento", listOf("Sinistra", "Centro", "Destra"), c.alignment) { index ->
            vm.updateSettings { s -> s.copy(clock = s.clock.copy(alignment = index)) }
        }
        SettingsSliderRow("Blur", c.blurRadiusDp, 0f..48f, { vm.updateSettings { s -> s.copy(clock = s.clock.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Trasparenza", c.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(clock = s.clock.copy(transparency = it)) } })
    }
    SettingsSectionCard("METEO") {
        SettingsSwitchRow("Widget attivo", w.enabled) { vm.updateSettings { s -> s.copy(weather = s.weather.copy(enabled = it)) } }
        SettingsChoiceRow("Fonte", listOf("Posizione GPS", "Città manuale"), if (w.source == WeatherSource.AUTO_LOCATION) 0 else 1) { index ->
            vm.updateSettings { s -> s.copy(weather = s.weather.copy(source = if (index == 0) WeatherSource.AUTO_LOCATION else WeatherSource.MANUAL_CITY)) }
        }
        SettingsTextFieldRow("Città manuale", w.manualCity) { vm.updateSettings { s -> s.copy(weather = s.weather.copy(manualCity = it)) } }
        SettingsSwitchRow("Gradi Celsius", w.useCelsius) { vm.updateSettings { s -> s.copy(weather = s.weather.copy(useCelsius = it)) } }
        SettingsSliderRow("Blur", w.blurRadiusDp, 0f..48f, { vm.updateSettings { s -> s.copy(weather = s.weather.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Trasparenza", w.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(weather = s.weather.copy(transparency = it)) } })
    }
}

@Composable
fun SearchSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val se = settings.search
    SettingsSectionCard("BARRA DI RICERCA") {
        SettingsSwitchRow("Barra attiva", se.enabled) { vm.updateSettings { s -> s.copy(search = s.search.copy(enabled = it)) } }
        SettingsChoiceRow(
            "Comportamento",
            listOf("Google", "Browser", "App", "Contatti", "Universale"),
            SearchTarget.entries.indexOf(se.target),
        ) { index -> vm.updateSettings { s -> s.copy(search = s.search.copy(target = SearchTarget.entries[index])) } }
        SettingsSwitchRow("Mostra microfono", se.showMic) { vm.updateSettings { s -> s.copy(search = s.search.copy(showMic = it)) } }
        SettingsSwitchRow("Mostra Lens", se.showLens) { vm.updateSettings { s -> s.copy(search = s.search.copy(showLens = it)) } }
        SettingsSliderRow("Altezza", se.heightDp, 40f..72f, { vm.updateSettings { s -> s.copy(search = s.search.copy(heightDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Larghezza", se.widthFraction, 0.6f..1f, { vm.updateSettings { s -> s.copy(search = s.search.copy(widthFraction = it)) } }, valueText = { "${(it * 100).toInt()}%" })
        SettingsSliderRow("Raggio angoli", se.cornerRadiusDp, 0f..40f, { vm.updateSettings { s -> s.copy(search = s.search.copy(cornerRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Blur", se.blurRadiusDp, 0f..48f, { vm.updateSettings { s -> s.copy(search = s.search.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Trasparenza", se.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(search = s.search.copy(transparency = it)) } })
    }
}

@Composable
fun AppDrawerSection(settings: LauncherSettings, vm: LauncherViewModel) {
    val ad = settings.drawer
    SettingsSectionCard("APP DRAWER") {
        SettingsSliderRow("Colonne", ad.columns.toFloat(), 3f..6f, { vm.updateSettings { s -> s.copy(drawer = s.drawer.copy(columns = it.toInt())) } }, valueText = { it.toInt().toString() })
        SettingsSwitchRow("Mostra ricerca", ad.showSearch) { vm.updateSettings { s -> s.copy(drawer = s.drawer.copy(showSearch = it)) } }
        SettingsSwitchRow("Scrollbar alfabetica", ad.alphabeticalScrollbar) { vm.updateSettings { s -> s.copy(drawer = s.drawer.copy(alphabeticalScrollbar = it)) } }
        SettingsSwitchRow("Mostra recenti", ad.showRecents) { vm.updateSettings { s -> s.copy(drawer = s.drawer.copy(showRecents = it)) } }
        SettingsSwitchRow("Categorie", ad.categoriesEnabled) { vm.updateSettings { s -> s.copy(drawer = s.drawer.copy(categoriesEnabled = it)) } }
        SettingsSliderRow("Blur", ad.blurRadiusDp, 0f..60f, { vm.updateSettings { s -> s.copy(drawer = s.drawer.copy(blurRadiusDp = it)) } }, valueText = { "${it.toInt()}dp" })
        SettingsSliderRow("Trasparenza", ad.transparency, 0f..1f, { vm.updateSettings { s -> s.copy(drawer = s.drawer.copy(transparency = it)) } })
    }
}
