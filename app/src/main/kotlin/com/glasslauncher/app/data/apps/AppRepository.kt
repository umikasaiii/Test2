package com.glasslauncher.app.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import androidx.collection.LruCache
import com.glasslauncher.app.data.model.AppInfo
import com.glasslauncher.app.data.model.AppKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

/**
 * Wraps [LauncherApps] (the API a default Home app is granted, independent of
 * QUERY_ALL_PACKAGES visibility rules) to list, launch and uninstall apps, and to
 * expose live add/remove/change updates via [LauncherApps.registerCallback].
 */
class AppRepository(private val context: Context) {

    private val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    private val iconCache = LruCache<AppKey, Drawable>(256)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String?, user: UserHandle?) = refresh()
        override fun onPackageAdded(packageName: String?, user: UserHandle?) = refresh()
        override fun onPackageChanged(packageName: String?, user: UserHandle?) = refresh()
        override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
        override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) = refresh()
    }

    init {
        instance = WeakReference(this)
        launcherApps.registerCallback(callback)
        refresh()
    }

    fun refresh() {
        val iconDp = 56
        val densityPx = (context.resources.displayMetrics.density * iconDp).toInt()
        val list = mutableListOf<AppInfo>()
        for (profile in launcherApps.profiles) {
            val activities = runCatching { launcherApps.getActivityList(null, profile) }.getOrDefault(emptyList())
            for (activity in activities) {
                val key = AppKey(
                    packageName = activity.applicationInfo.packageName,
                    activityClassName = activity.componentName.className,
                    userHandleHash = profile.hashCode(),
                )
                val icon = iconCache.get(key) ?: run {
                    val drawable = runCatching {
                        activity.getBadgedIcon(densityPx.coerceAtLeast(1))
                    }.getOrElse { activity.getIcon(0) ?: context.packageManager.defaultActivityIcon }
                    iconCache.put(key, drawable)
                    drawable
                }
                list += AppInfo(
                    key = key,
                    label = activity.label?.toString() ?: activity.applicationInfo.packageName,
                    packageName = activity.applicationInfo.packageName,
                    activityClassName = activity.componentName.className,
                    userHandle = profile,
                    icon = icon,
                    isSystemApp = (activity.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0,
                )
            }
        }
        _apps.value = list.sortedBy { it.label.lowercase() }
    }

    fun findByKey(key: AppKey): AppInfo? = _apps.value.firstOrNull { it.key == key }

    fun launch(app: AppInfo, sourceBounds: android.graphics.Rect? = null) {
        val component = android.content.ComponentName(app.packageName, app.activityClassName)
        runCatching {
            launcherApps.startMainActivity(component, app.userHandle, sourceBounds, null)
        }
    }

    fun openAppInfo(app: AppInfo) {
        runCatching {
            launcherApps.startAppDetailsActivity(
                android.content.ComponentName(app.packageName, app.activityClassName),
                app.userHandle, null, null,
            )
        }
    }

    fun requestUninstall(app: AppInfo): Intent {
        return Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun getShortcuts(app: AppInfo): List<LauncherApps.ShortcutQuery> = emptyList()

    suspend fun iconFor(key: AppKey): Drawable? = withContext(Dispatchers.Default) {
        iconCache.get(key) ?: findByKey(key)?.icon
    }

    fun teardown() {
        runCatching { launcherApps.unregisterCallback(callback) }
    }

    companion object {
        private var instance: WeakReference<AppRepository>? = null
        fun notifyExternalChange() {
            instance?.get()?.refresh()
        }
    }
}
