package com.glasslauncher.app.data.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Manifest-registered fallback: keeps the app list correct even if the launcher process
 * was restarted between installs. Live updates while running come from
 * [AppRepository]'s LauncherApps.Callback instead.
 */
class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppRepository.notifyExternalChange()
    }
}
