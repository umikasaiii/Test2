package com.glasslauncher.app.data.layout

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.glasslauncher.app.data.model.HomeLayout
import com.glasslauncher.app.data.model.HomePage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.layoutDataStore by preferencesDataStore(name = "glass_launcher_layout")

/**
 * Persists the Home layout: pages, grid item placement, folders, widget placements,
 * the dock contents and the hidden-apps set. Loss-less JSON so a device swap can restore it.
 */
class LayoutRepository(private val context: Context) {

    private val key = stringPreferencesKey("layout_json")

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        classDiscriminator = "type"
    }

    val layoutFlow: Flow<HomeLayout> = context.layoutDataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val raw = prefs[key]
            if (raw.isNullOrBlank()) {
                HomeLayout()
            } else {
                runCatching { json.decodeFromString<HomeLayout>(raw) }.getOrDefault(HomeLayout())
            }
        }

    suspend fun update(transform: (HomeLayout) -> HomeLayout) {
        context.layoutDataStore.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString<HomeLayout>(it) }.getOrNull()
            } ?: HomeLayout()
            prefs[key] = json.encodeToString(HomeLayout.serializer(), transform(current))
        }
    }

    suspend fun replaceAll(layout: HomeLayout) {
        context.layoutDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(HomeLayout.serializer(), layout)
        }
    }

    suspend fun setPageCount(count: Int) = update { layout ->
        val pages = layout.pages.toMutableList()
        while (pages.size < count) pages.add(HomePage())
        while (pages.size > count && pages.size > 1) pages.removeAt(pages.lastIndex)
        layout.copy(pages = pages)
    }

    suspend fun exportJson(): String = layoutFlow.first().let {
        json.encodeToString(HomeLayout.serializer(), it)
    }

    suspend fun importJson(raw: String): Boolean {
        val parsed = runCatching { json.decodeFromString<HomeLayout>(raw) }.getOrNull() ?: return false
        replaceAll(parsed)
        return true
    }

    suspend fun clearAll() = replaceAll(HomeLayout())
}
