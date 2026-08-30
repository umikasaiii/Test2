package com.glasslauncher.app.ui.drawer

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glasslauncher.app.data.model.AppDrawerSettings
import com.glasslauncher.app.data.model.AppInfo
import com.glasslauncher.app.glass.GlassSurface
import com.glasslauncher.app.glass.IconTile
import com.glasslauncher.app.data.model.AppIconOverride
import com.glasslauncher.app.ui.ContextMenuAction
import com.glasslauncher.app.ui.ContextMenuOverlay

/**
 * Full-screen app list over the (blurred) Home wallpaper: search field, alphabet scrollbar,
 * and a configurable grid. Hidden apps (from [hiddenKeys]) are filtered out here. Long-pressing
 * an app opens a small action menu (open, add to Home, app info, hide, uninstall) rather than
 * doing anything destructive immediately.
 */
@Composable
fun AppDrawer(
    apps: List<AppInfo>,
    hiddenKeys: Set<com.glasslauncher.app.data.model.AppKey>,
    settings: AppDrawerSettings,
    initialQuery: String = "",
    modifier: Modifier = Modifier,
    onLaunch: (AppInfo) -> Unit,
    onAddToHome: (AppInfo) -> Unit,
    onOpenInfo: (AppInfo) -> Unit,
    onToggleHidden: (AppInfo) -> Unit,
    onUninstall: (AppInfo) -> Unit,
) {
    var query by remember { mutableStateOf(initialQuery) }
    var contextApp by remember { mutableStateOf<AppInfo?>(null) }
    val visibleApps = remember(apps, hiddenKeys, query) {
        apps.filter { it.key !in hiddenKeys }
            .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
            .sortedBy { it.label.lowercase() }
    }
    val lettersPresent = remember(visibleApps) {
        visibleApps.mapNotNull { it.label.firstOrNull()?.uppercaseChar() }.toSortedSet()
    }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(top = 72.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            if (settings.showSearch) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadiusDp = 24f,
                    transparencyOverride = settings.transparency,
                    blurRadiusDpOverride = settings.blurRadiusDp,
                    showGlow = false,
                ) {
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text("Cerca app", color = Color.White.copy(alpha = 0.55f), fontSize = 16.sp)
                            }
                            inner()
                        },
                    )
                }
            }

            Box(Modifier.fillMaxSize().padding(top = 12.dp)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(settings.columns),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(visibleApps, key = { it.key.packageName + it.key.activityClassName }) { info ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.pointerInput(info.key) {
                                detectTapGestures(
                                    onTap = { onLaunch(info) },
                                    onLongPress = { contextApp = info },
                                )
                            },
                        ) {
                            IconTile(icon = info.icon, packageName = info.packageName, override = AppIconOverride(), tileSizeDpOverride = 52f)
                            Text(
                                text = info.label,
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(64.dp).padding(top = 4.dp),
                            )
                        }
                    }
                }

                if (settings.alphabeticalScrollbar && lettersPresent.isNotEmpty()) {
                    Column(
                        Modifier.align(Alignment.CenterEnd).padding(end = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                    ) {
                        lettersPresent.forEach { letter ->
                            Text(letter.toString(), color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        val target = contextApp
        ContextMenuOverlay(
            title = target?.label,
            visible = target != null,
            onDismiss = { contextApp = null },
            actions = if (target == null) {
                emptyList()
            } else {
                listOf(
                    ContextMenuAction("Apri") { onLaunch(target) },
                    ContextMenuAction("Aggiungi alla Home") { onAddToHome(target) },
                    ContextMenuAction("Info app") { onOpenInfo(target) },
                    ContextMenuAction("Nascondi") { onToggleHidden(target) },
                    ContextMenuAction("Disinstalla", destructive = true) { onUninstall(target) },
                )
            },
        )
    }
}
