package com.glasslauncher.app.data.backup

import android.content.Context
import android.net.Uri
import com.glasslauncher.app.data.layout.LayoutRepository
import com.glasslauncher.app.data.settings.SettingsRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BackupBundle(
    val version: Int = 1,
    val settingsJson: String,
    val layoutJson: String,
)

/** Bundles settings + home layout into a single portable `.glasslauncher` JSON file. */
class BackupManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val layoutRepository: LayoutRepository,
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun exportToUri(uri: Uri) {
        val bundle = BackupBundle(
            settingsJson = settingsRepository.exportJson(),
            layoutJson = layoutRepository.exportJson(),
        )
        val text = json.encodeToString(BackupBundle.serializer(), bundle)
        context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
    }

    suspend fun importFromUri(uri: Uri): Boolean {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return false
        val bundle = runCatching { json.decodeFromString(BackupBundle.serializer(), text) }.getOrNull()
            ?: return false
        val settingsOk = settingsRepository.importJson(bundle.settingsJson)
        val layoutOk = layoutRepository.importJson(bundle.layoutJson)
        return settingsOk && layoutOk
    }

    suspend fun resetEverything() {
        settingsRepository.resetToPreset()
        layoutRepository.clearAll()
    }
}
