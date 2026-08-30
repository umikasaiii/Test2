package com.glasslauncher.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.glasslauncher.app.GlassLauncherApp
import com.glasslauncher.app.glass.GlassRoot
import com.glasslauncher.app.glass.ProvideGlassEnvironment
import com.glasslauncher.app.glass.ProvideIconEngine
import com.glasslauncher.app.notifications.LauncherNotificationListenerService
import com.glasslauncher.app.ui.drawer.AppDrawer
import com.glasslauncher.app.ui.home.HomeScreen
import com.glasslauncher.app.ui.panels.ControlCenterPanel
import com.glasslauncher.app.ui.panels.NotificationPanel
import com.glasslauncher.app.ui.panels.TopEdgeGestureStrip
import com.glasslauncher.app.ui.panels.TopPanelOverlay
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
    val topPanel by viewModel.topPanel.collectAsState()
    val notifications by LauncherNotificationListenerService.notifications.collectAsState()

    LaunchedEffect(widgetHost) { widgetHost.start() }
    DisposableEffect(widgetHost) { onDispose { widgetHost.stop() } }

    BackHandler(enabled = topPanel != LauncherViewModel.TopPanel.NONE) { viewModel.closeTopPanel() }
    BackHandler(enabled = topPanel == LauncherViewModel.TopPanel.NONE && openFolderId != null) { viewModel.closeFolder() }
    BackHandler(enabled = topPanel == LauncherViewModel.TopPanel.NONE && openFolderId == null && editMode) { viewModel.setEditMode(false) }
    BackHandler(
        enabled = topPanel == LauncherViewModel.TopPanel.NONE && openFolderId == null && !editMode && topScreen != TopScreen.HOME,
    ) {
        viewModel.cancelDockAssignment()
        viewModel.openHome()
    }

    ProvideGlassEnvironment(style = settings.glass, palette = palette) {
        ProvideIconEngine(settings = settings.icons) {
            GlassRoot(wallpaperBitmap = wallpaperBitmap) {
                Box(Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = topScreen,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            val enterSpring = spring<androidx.compose.ui.unit.IntOffset>(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMediumLow,
                            )
                            val exitSpring = spring<androidx.compose.ui.unit.IntOffset>(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessHigh,
                            )
                            (slideInVertically(animationSpec = enterSpring, initialOffsetY = { it / 4 }) + fadeIn()) togetherWith
                                (slideOutVertically(animationSpec = exitSpring, targetOffsetY = { -it / 4 }) + fadeOut())
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
                                onAddToHome = { info -> viewModel.addAppToHomeAutoPlace(info); viewModel.openHome() },
                                onOpenInfo = { info -> viewModel.openAppInfo(info) },
                                onToggleHidden = { info -> viewModel.toggleHidden(info.key) },
                                onUninstall = { info -> context.startActivity(viewModel.uninstallIntent(info)) },
                            )
                            TopScreen.SETTINGS -> SettingsScreen(viewModel, onClose = { viewModel.openHome() })
                        }
                    }

                    TopEdgeGestureStrip(
                        enabled = topPanel == LauncherViewModel.TopPanel.NONE,
                        notificationsEnabled = settings.gestures.swipeDownOpensNotifications,
                        controlCenterEnabled = settings.gestures.swipeDownOpensControlCenter,
                        onOpenNotifications = { viewModel.openNotificationPanel() },
                        onOpenControlCenter = { viewModel.openControlCenter() },
                    )

                    TopPanelOverlay(
                        visible = topPanel == LauncherViewModel.TopPanel.NOTIFICATIONS,
                        horizontalAlignment = Alignment.Start,
                        onDismiss = { viewModel.closeTopPanel() },
                    ) {
                        NotificationPanel(
                            settings = settings.notificationPanel,
                            entries = notifications,
                            accessGranted = LauncherNotificationListenerService.isAccessGranted(context),
                            onRequestAccess = { context.startActivity(LauncherNotificationListenerService.requestAccessIntent()) },
                            onOpen = { entry -> LauncherNotificationListenerService.open(entry); viewModel.closeTopPanel() },
                            onDismiss = { entry -> LauncherNotificationListenerService.dismiss(entry.key) },
                            onClearAll = { LauncherNotificationListenerService.dismissAll() },
                        )
                    }

                    TopPanelOverlay(
                        visible = topPanel == LauncherViewModel.TopPanel.CONTROL_CENTER,
                        horizontalAlignment = Alignment.End,
                        onDismiss = { viewModel.closeTopPanel() },
                    ) {
                        ControlCenterPanel(settings = settings.controlCenter)
                    }
                }
            }
        }
    }
}
