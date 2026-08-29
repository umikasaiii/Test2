package com.glasslauncher.app.ui.widgets

import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.glasslauncher.app.data.model.WidgetKind
import com.glasslauncher.app.widgethost.LauncherWidgetHost
import com.glasslauncher.app.ui.ContextMenuAction
import com.glasslauncher.app.ui.ContextMenuOverlay

/** The "add a widget" sheet from Edit Mode: the two built-in glass widgets, plus every
 * third-party AppWidget installed on the device. */
@Composable
fun WidgetPickerOverlay(
    visible: Boolean,
    host: LauncherWidgetHost,
    onDismiss: () -> Unit,
    onAddBuiltIn: (WidgetKind) -> Unit,
    onAddThirdParty: (appWidgetId: Int, spanCols: Int, spanRows: Int) -> Unit,
) {
    val context = LocalContext.current
    var pendingWidgetId by remember { mutableStateOf(-1) }

    val bindLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK && pendingWidgetId != -1) {
            onAddThirdParty(pendingWidgetId, 4, 2)
        } else if (pendingWidgetId != -1) {
            host.deleteAppWidgetId(pendingWidgetId)
        }
        pendingWidgetId = -1
    }

    val providers = remember(visible) { if (visible) host.installedProviders() else emptyList() }

    val actions = buildList {
        add(ContextMenuAction("Orologio") { onAddBuiltIn(WidgetKind.CLOCK) })
        add(ContextMenuAction("Meteo") { onAddBuiltIn(WidgetKind.WEATHER) })
        providers.forEach { provider ->
            val label = runCatching { provider.loadLabel(context.packageManager) }.getOrDefault(provider.provider.packageName)
            add(
                ContextMenuAction(label) {
                    val id = host.allocateAppWidgetId()
                    val allowed = runCatching {
                        host.appWidgetManager.bindAppWidgetIdIfAllowed(id, provider.provider)
                    }.getOrDefault(false)
                    if (allowed) {
                        onAddThirdParty(id, 4, 2)
                    } else {
                        pendingWidgetId = id
                        bindLauncher.launch(
                            Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider.provider)
                            },
                        )
                    }
                },
            )
        }
    }

    ContextMenuOverlay(title = "Aggiungi widget", actions = actions, visible = visible, onDismiss = onDismiss)
}
