package com.glasslauncher.app

import android.app.Application
import com.glasslauncher.app.data.apps.AppRepository
import com.glasslauncher.app.data.backup.BackupManager
import com.glasslauncher.app.data.layout.LayoutRepository
import com.glasslauncher.app.data.settings.SettingsRepository
import com.glasslauncher.app.data.wallpaper.WallpaperRepository
import com.glasslauncher.app.data.weather.WeatherRepository

/** Owns every singleton repository. A launcher process tends to live for the whole session,
 * so plain lazily-created singletons (no DI framework) keep things simple and fast to start. */
class GlassLauncherApp : Application() {

    val settingsRepository by lazy { SettingsRepository(this) }
    val layoutRepository by lazy { LayoutRepository(this) }
    val appRepository by lazy { AppRepository(this) }
    val wallpaperRepository by lazy { WallpaperRepository(this) }
    val weatherRepository by lazy { WeatherRepository(this) }
    val backupManager by lazy { BackupManager(this, settingsRepository, layoutRepository) }

    override fun onCreate() {
        super.onCreate()
        wallpaperRepository.start()
    }
}
