package com.glasslauncher.app.ui.widgets

import android.view.View
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.glasslauncher.app.widgethost.LauncherWidgetHost

/** Hosts a real, third-party Android AppWidget (bound elsewhere) inside the Home grid. */
@Composable
fun ThirdPartyWidgetView(host: LauncherWidgetHost, appWidgetId: Int, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val info = host.infoFor(appWidgetId)
            if (info != null) host.createHostView(appWidgetId, info) else View(ctx)
        },
    )
}
