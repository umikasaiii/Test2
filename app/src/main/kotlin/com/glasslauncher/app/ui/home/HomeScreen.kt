package com.glasslauncher.app.ui.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.glasslauncher.app.data.model.AppIconOverride
import com.glasslauncher.app.data.model.AppInfo
import com.glasslauncher.app.data.model.GridItem
import com.glasslauncher.app.data.model.IconRenderMode
import com.glasslauncher.app.data.model.WidgetKind
import com.glasslauncher.app.data.weather.WeatherRepository
import com.glasslauncher.app.gestures.GestureHelper
import com.glasslauncher.app.gestures.homeGestures
import com.glasslauncher.app.ui.ContextMenuAction
import com.glasslauncher.app.ui.ContextMenuOverlay
import com.glasslauncher.app.ui.LauncherViewModel
import com.glasslauncher.app.ui.dock.Dock
import com.glasslauncher.app.ui.folder.OpenFolderOverlay
import com.glasslauncher.app.ui.search.SearchBar
import com.glasslauncher.app.ui.widgets.WidgetPickerOverlay
import com.glasslauncher.app.widgethost.LauncherWidgetHost

private sealed class ContextTarget {
    data class HomeItem(val pageIndex: Int, val item: GridItem.AppShortcut) : ContextTarget()
    data class DockSlot(val slot: Int) : ContextTarget()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LauncherViewModel,
    widgetHost: LauncherWidgetHost,
    weatherRepository: WeatherRepository,
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val layout by viewModel.layout.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val editMode by viewModel.editMode.collectAsState()
    val editSheet by viewModel.editSheet.collectAsState()
    val openFolderId by viewModel.openFolderId.collectAsState()

    val pagerState = rememberPagerState(pageCount = { layout.pages.size.coerceAtLeast(1) })
    var contextTarget by remember { mutableStateOf<ContextTarget?>(null) }

    val wallpaperLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.refreshWallpaper()
    }

    LaunchedEffect(pagerState.currentPage) { viewModel.setPage(pagerState.currentPage) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            SearchBar(
                settings = settings.search,
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 8.dp),
                onOpenDrawerWithQuery = { viewModel.openDrawer() },
            )

            Box(Modifier.weight(1f)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
                    val page = layout.pages.getOrNull(pageIndex) ?: return@HorizontalPager
                    HomeGrid(
                        page = page,
                        grid = settings.homeGrid,
                        apps = apps,
                        editMode = editMode,
                        clockSettings = settings.clock,
                        weatherSettings = settings.weather,
                        weatherRepository = weatherRepository,
                        widgetHost = widgetHost,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = settings.homeGrid.marginTopDp.dp, bottom = settings.homeGrid.marginBottomDp.dp)
                            .homeGestures(
                                settings = settings.gestures,
                                onSwipeUp = { viewModel.openDrawer() },
                                onSwipeDown = { GestureHelper.expandNotificationShade(context) },
                                onPinch = { viewModel.setEditMode(true) },
                                onDoubleTap = { GestureHelper.lockScreenNow(context) },
                            ),
                        onLaunch = { viewModel.launchApp(it) },
                        onOpenFolder = { viewModel.openFolder(it) },
                        onLongPressEmptySpace = { if (settings.gestures.longPressOpensEditMode) viewModel.setEditMode(true) },
                        onMove = { id, col, row -> viewModel.moveItem(pageIndex, id, col, row) },
                        onCombine = { dragged, target -> viewModel.combineIntoFolder(pageIndex, dragged, target) },
                        onLongPressItem = { item ->
                            if (item is GridItem.AppShortcut) contextTarget = ContextTarget.HomeItem(pageIndex, item)
                        },
                    )
                }
            }

            Dock(
                settings = settings.dock,
                items = layout.dock,
                apps = apps,
                editMode = editMode,
                modifier = Modifier.fillMaxWidth().padding(bottom = settings.dock.marginBottomDp.dp),
                onLaunch = { viewModel.launchApp(it) },
                onLongPressSlot = { slot -> contextTarget = ContextTarget.DockSlot(slot) },
            )
        }

        val openFolder = layout.pages.flatMap { it.items }.filterIsInstance<GridItem.Folder>().firstOrNull { it.id == openFolderId }
        OpenFolderOverlay(
            folder = openFolder,
            apps = apps,
            settings = settings.folders,
            onLaunch = { viewModel.launchApp(it) },
            onRename = { name -> openFolder?.let { f -> viewModel.renameFolder(pagerState.currentPage, f.id, name) } },
            onRemoveApp = { itemId -> openFolder?.let { f -> viewModel.removeFromFolder(pagerState.currentPage, f.id, itemId) } },
            onDismiss = { viewModel.closeFolder() },
        )

        if (editMode) {
            EditModeToolbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp),
                onWallpaper = { runCatching { wallpaperLauncher.launch(Intent(Intent.ACTION_SET_WALLPAPER)) } },
                onWidgets = { viewModel.showEditSheet(LauncherViewModel.EditSheet.WIDGETS) },
                onIcons = { viewModel.openSettings() },
                onLayout = { viewModel.openSettings() },
                onTheme = { viewModel.openSettings() },
                onEffects = { viewModel.openSettings() },
                onSettings = { viewModel.openSettings() },
                onDone = { viewModel.setEditMode(false) },
            )
        }

        WidgetPickerOverlay(
            visible = editSheet == LauncherViewModel.EditSheet.WIDGETS,
            host = widgetHost,
            onDismiss = { viewModel.showEditSheet(null) },
            onAddBuiltIn = { kind ->
                viewModel.addWidget(pagerState.currentPage, kind, column = 0, row = 0, spanColumns = settings.homeGrid.columns, spanRows = 2)
                viewModel.showEditSheet(null)
            },
            onAddThirdParty = { id, spanCols, spanRows ->
                viewModel.addWidget(pagerState.currentPage, WidgetKind.THIRD_PARTY, column = 0, row = 0, spanColumns = spanCols, spanRows = spanRows, appWidgetId = id)
                viewModel.showEditSheet(null)
            },
        )

        val target = contextTarget
        ContextMenuOverlay(
            title = null,
            visible = target != null,
            onDismiss = { contextTarget = null },
            actions = when (target) {
                is ContextTarget.HomeItem -> {
                    val info = apps.firstOrNull { it.key == target.item.app }
                    listOfNotNull(
                        info?.let { ContextMenuAction("Apri") { viewModel.launchApp(it) } },
                        info?.let { ContextMenuAction("Info app") { viewModel.openAppInfo(it) } },
                        ContextMenuAction("Icona originale") { viewModel.setIconOverride(target.pageIndex, target.item.id, AppIconOverride(mode = IconRenderMode.ORIGINAL)) },
                        ContextMenuAction("Icona glass") { viewModel.setIconOverride(target.pageIndex, target.item.id, AppIconOverride(mode = IconRenderMode.GLASS_AUTO)) },
                        ContextMenuAction("Icona tema") { viewModel.setIconOverride(target.pageIndex, target.item.id, AppIconOverride(mode = IconRenderMode.THEME_CUSTOM)) },
                        info?.let { ContextMenuAction("Nascondi") { viewModel.toggleHidden(it.key) } },
                        info?.let { ContextMenuAction("Disinstalla", destructive = true) { context.startActivity(viewModel.uninstallIntent(it)) } },
                        ContextMenuAction("Rimuovi da Home", destructive = true) { viewModel.removeItem(target.pageIndex, target.item.id) },
                    )
                }
                is ContextTarget.DockSlot -> {
                    val hasApp = layout.dock.any { it.slot == target.slot }
                    listOfNotNull(
                        ContextMenuAction(if (hasApp) "Cambia app" else "Aggiungi app") { viewModel.beginDockAssignment(target.slot) },
                        if (hasApp) ContextMenuAction("Rimuovi dal dock", destructive = true) { viewModel.clearDockSlot(target.slot) } else null,
                    )
                }
                else -> emptyList()
            },
        )
    }
}
