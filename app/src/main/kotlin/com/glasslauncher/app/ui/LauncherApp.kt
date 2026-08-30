package com.glasslauncher.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.glasslauncher.app.GlassLauncherApp
import com.glasslauncher.app.glass.GlassRoot
import com.glasslauncher.app.glass.ProvideGlassEnvironment
import com.glasslauncher.app.glass.ProvideIconEngine
import com.glasslauncher.app.ui.drawer.AppDrawer
import com.glasslauncher.app.ui.home.HomeScreen
import com.glasslauncher.app.ui.settings.SettingsScreen
import com.glasslauncher.app.widgethost.LauncherWidgetHost

/** The whole launcher UI: one Compose tree, screens swapped by [LauncherViewModel.topScreen]. */
@Composable
fun LauncherApp(viewModel: LauncherViewModel) {
    val context = LocalContext.current
    val app = remember { context.applicationContext as GlassLauncherApp }
    val widgetHost = remember { LauncherWidgetHost(context) }

    val settings by viewModel.settings.collectAsState()
    val palette by viewModel.palette.collectAsState()
    val wallpaperBitmap by viewModel.wallpaperBitmap.collectAsState()
    val topScreen by viewModel.topScreen.collectAsState()
    val pendingDockSlot by viewModel.pendingDockSlot.collectAsState()
    val layout by viewModel.layout.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val openFolderId by viewModel.openFolderId.collectAsState()
    val editMode by viewModel.editMode.collectAsState()

    LaunchedEffect(widgetHost) { widgetHost.start() }
    DisposableEffect(widgetHost) { onDispose { widgetHost.stop() } }

    BackHandler(enabled = openFolderId != null) { viewModel.closeFolder() }
    BackHandler(enabled = openFolderId == null && editMode) { viewModel.setEditMode(false) }
    BackHandler(enabled = openFolderId == null && !editMode && topScreen != TopScreen.HOME) {
        viewModel.cancelDockAssignment()
        viewModel.openHome()
    }

    ProvideGlassEnvironment(style = settings.glass, palette = palette) {
        ProvideIconEngine(settings = settings.icons) {
            GlassRoot(wallpaperBitmap = wallpaperBitmap) {
                AnimatedContent(
                    targetState = topScreen,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        (slideInVertically(initialOffsetY = { it / 4 }) + fadeIn()) togetherWith
                            (slideOutVertically(targetOffsetY = { -it / 4 }) + fadeOut())
                    },
                    label = "topScreen",
                ) { screen ->
                    when (screen) {
                        TopScreen.HOME -> HomeScreen(viewModel, widgetHost, app.weatherRepository)
                        TopScreen.DRAWER -> AppDrawer(
                            apps = apps,
                            hiddenKeys = layout.hiddenApps,
                            settings = settings.drawer,
                            onLaunch = { info ->
                                if (pendingDockSlot != null) {
                                    viewModel.setDockSlot(pendingDockSlot!!, info)
                                    viewModel.cancelDockAssignment()
                                    viewModel.openHome()
                                } else {
                                    viewModel.launchApp(info)
                                }
                            },
                            onLongPressApp = { info -> viewModel.toggleHidden(info.key) },
                        )
                        TopScreen.SETTINGS -> SettingsScreen(viewModel, onClose = { viewModel.openHome() })
                    }
                }
            }
        }
    }
}
