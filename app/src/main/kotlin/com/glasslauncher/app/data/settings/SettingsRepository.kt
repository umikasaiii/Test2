package com.glasslauncher.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.glasslauncher.app.data.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.settingsDataStore by preferencesDataStore(name = "glass_launcher_settings")

/**
 * Persists every configurable Adaptive Glass / Home / Dock / Folder / Widget / Search /
 * Drawer / Gesture parameter as a single JSON document in DataStore.
 */
class SettingsRepository(private val context: Context) {

    private val key = stringPreferencesKey("settings_json")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    val settingsFlow: Flow<LauncherSettings> = context.settingsDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val raw = prefs[key]
            if (raw.isNullOrBlank()) {
                LauncherSettings.GLASS_REFERENCE
            } else {
                runCatching { json.decodeFromString<LauncherSettings>(raw) }
                    .getOrDefault(LauncherSettings.GLASS_REFERENCE)
            }
        }

    suspend fun update(transform: (LauncherSettings) -> LauncherSettings) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString<LauncherSettings>(it) }.getOrNull()
            } ?: LauncherSettings.GLASS_REFERENCE
            prefs[key] = json.encodeToString(LauncherSettings.serializer(), transform(current))
        }
    }

    suspend fun replaceAll(settings: LauncherSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(LauncherSettings.serializer(), settings)
        }
    }

    suspend fun resetToPreset() = replaceAll(LauncherSettings.GLASS_REFERENCE)

    suspend fun exportJson(): String = settingsFlow.first().let {
        json.encodeToString(LauncherSettings.serializer(), it)
    }

    suspend fun importJson(raw: String): Boolean {
        val parsed = runCatching { json.decodeFromString<LauncherSettings>(raw) }.getOrNull() ?: return false
        replaceAll(parsed)
        return true
    }
}
