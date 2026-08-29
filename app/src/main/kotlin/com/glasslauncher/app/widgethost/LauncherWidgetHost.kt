package com.glasslauncher.app.widgethost

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

private const val HOST_ID = 4241

/**
 * Thin wrapper around [AppWidgetHost] / [AppWidgetManager] so the launcher can host real,
 * third-party Android AppWidgets (not just our own Compose clock/weather widgets).
 */
class LauncherWidgetHost(context: Context) {
    private val appContext = context.applicationContext
    val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(appContext)
    val appWidgetHost: AppWidgetHost = AppWidgetHost(appContext, HOST_ID)

    fun start() = appWidgetHost.startListening()
    fun stop() = appWidgetHost.stopListening()

    fun allocateAppWidgetId(): Int = appWidgetHost.allocateAppWidgetId()
    fun deleteAppWidgetId(id: Int) = runCatching { appWidgetHost.deleteAppWidgetId(id) }

    fun installedProviders(): List<AppWidgetProviderInfo> = appWidgetManager.installedProviders

    fun createHostView(appWidgetId: Int, info: AppWidgetProviderInfo): AppWidgetHostView =
        appWidgetHost.createView(appContext, appWidgetId, info)

    fun infoFor(appWidgetId: Int): AppWidgetProviderInfo? = appWidgetManager.getAppWidgetInfo(appWidgetId)
}
