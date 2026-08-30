package com.glasslauncher.app.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.glasslauncher.app.GlassLauncherApp
import com.glasslauncher.app.data.model.AppIconOverride
import com.glasslauncher.app.data.model.AppInfo
import com.glasslauncher.app.data.model.AppKey
import com.glasslauncher.app.data.model.DockItem
import com.glasslauncher.app.data.model.GridItem
import com.glasslauncher.app.data.model.HomeLayout
import com.glasslauncher.app.data.model.HomePage
import com.glasslauncher.app.data.model.LauncherSettings
import com.glasslauncher.app.data.model.WidgetKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class TopScreen { HOME, DRAWER, SETTINGS }

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val app get() = getApplication<GlassLauncherApp>()

    val settings: StateFlow<LauncherSettings> = app.settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LauncherSettings.GLASS_REFERENCE)

    val layout: StateFlow<HomeLayout> = app.layoutRepository.layoutFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeLayout())

    val apps: StateFlow<List<AppInfo>> = app.appRepository.apps
    val palette get() = app.wallpaperRepository.palette
    val wallpaperBitmap get() = app.wallpaperRepository.bitmap

    private val _topScreen = MutableStateFlow(TopScreen.HOME)
    val topScreen: StateFlow<TopScreen> = _topScreen.asStateFlow()

    private val _openFolderId = MutableStateFlow<String?>(null)
    val openFolderId: StateFlow<String?> = _openFolderId.asStateFlow()

    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _editSheet = MutableStateFlow<EditSheet?>(null)
    val editSheet: StateFlow<EditSheet?> = _editSheet.asStateFlow()

    private val _pendingDockSlot = MutableStateFlow<Int?>(null)
    val pendingDockSlot: StateFlow<Int?> = _pendingDockSlot.asStateFlow()

    fun beginDockAssignment(slot: Int) { _pendingDockSlot.value = slot; openDrawer() }
    fun cancelDockAssignment() { _pendingDockSlot.value = null }

    enum class EditSheet { WALLPAPER, WIDGETS, ICONS, LAYOUT, THEME, EFFECTS, SETTINGS }

    enum class TopPanel { NONE, NOTIFICATIONS, CONTROL_CENTER }

    private val _topPanel = MutableStateFlow(TopPanel.NONE)
    val topPanel: StateFlow<TopPanel> = _topPanel.asStateFlow()

    fun openNotificationPanel() { _topPanel.value = TopPanel.NOTIFICATIONS }
    fun openControlCenter() { _topPanel.value = TopPanel.CONTROL_CENTER }
    fun closeTopPanel() { _topPanel.value = TopPanel.NONE }

    // --- navigation ---
    fun openDrawer() { _topScreen.value = TopScreen.DRAWER }
    fun openHome() { _topScreen.value = TopScreen.HOME }
    fun openSettings() { _topScreen.value = TopScreen.SETTINGS }
    fun openFolder(id: String) { _openFolderId.value = id }
    fun closeFolder() { _openFolderId.value = null }
    fun setPage(index: Int) { _currentPage.value = index }
    fun setEditMode(enabled: Boolean) { _editMode.value = enabled; if (!enabled) _editSheet.value = null }
    fun showEditSheet(sheet: EditSheet?) { _editSheet.value = sheet }

    fun refreshWallpaper() = app.wallpaperRepository.refreshNow()
    fun refreshInstalledApps() = app.appRepository.refresh()

    fun exportBackup(uri: android.net.Uri) = viewModelScope.launch { app.backupManager.exportToUri(uri) }
    fun importBackup(uri: android.net.Uri) = viewModelScope.launch { app.backupManager.importFromUri(uri) }
    fun resetToPreset() = viewModelScope.launch { app.settingsRepository.resetToPreset() }
    fun clearHomeLayout() = viewModelScope.launch { app.layoutRepository.clearAll() }
    fun resetEverything() = viewModelScope.launch { app.backupManager.resetEverything() }

    fun updateSettings(transform: (LauncherSettings) -> LauncherSettings) {
        viewModelScope.launch { app.settingsRepository.update(transform) }
    }

    fun updateLayout(transform: (HomeLayout) -> HomeLayout) {
        viewModelScope.launch { app.layoutRepository.update(transform) }
    }

    // --- app actions ---
    fun launchApp(info: AppInfo) = app.appRepository.launch(info)
    fun openAppInfo(info: AppInfo) = app.appRepository.openAppInfo(info)
    fun uninstallIntent(info: AppInfo): Intent = app.appRepository.requestUninstall(info)

    fun toggleHidden(key: AppKey) = updateLayout { l ->
        l.copy(hiddenApps = if (key in l.hiddenApps) l.hiddenApps - key else l.hiddenApps + key)
    }

    /** Places [appInfo] on the current Home page's first free cell (used from the App Drawer's
     * long-press menu, which has no notion of grid position). Falls back to (0,0) if the page
     * is completely full. */
    fun addAppToHomeAutoPlace(appInfo: AppInfo) {
        val grid = settings.value.homeGrid
        val pages = layout.value.pages
        if (pages.isEmpty()) return
        val pageIndex = currentPage.value.coerceIn(0, pages.lastIndex)
        val page = pages[pageIndex]
        val occupied = page.items.map { it.column to it.row }.toSet()
        for (row in 0 until grid.rows) {
            for (column in 0 until grid.columns) {
                if ((column to row) !in occupied) {
                    addAppToPage(pageIndex, appInfo, column, row)
                    return
                }
            }
        }
        addAppToPage(pageIndex, appInfo, 0, 0)
    }

    // --- home grid mutation ---
    fun addAppToPage(pageIndex: Int, app: AppInfo, column: Int, row: Int) = updateLayout { layout ->
        mutatePage(layout, pageIndex) { page ->
            page.copy(
                items = page.items + GridItem.AppShortcut(
                    id = UUID.randomUUID().toString(),
                    column = column,
                    row = row,
                    app = app.key,
                ),
            )
        }
    }

    fun moveItem(pageIndex: Int, itemId: String, column: Int, row: Int) = updateLayout { layout ->
        mutatePage(layout, pageIndex) { page ->
            page.copy(items = page.items.map { item -> if (item.id == itemId) withPosition(item, column, row) else item })
        }
    }

    fun removeItem(pageIndex: Int, itemId: String) = updateLayout { layout ->
        mutatePage(layout, pageIndex) { page -> page.copy(items = page.items.filterNot { it.id == itemId }) }
    }

    fun renameFolder(pageIndex: Int, folderId: String, newName: String) = updateLayout { layout ->
        mutatePage(layout, pageIndex) { page ->
            page.copy(items = page.items.map {
                if (it is GridItem.Folder && it.id == folderId) it.copy(name = newName) else it
            })
        }
    }

    /** Drags [draggedId] onto [targetId]: if the target is a folder, the app joins it; otherwise a new folder is created. */
    fun combineIntoFolder(pageIndex: Int, draggedId: String, targetId: String) = updateLayout { layout ->
        mutatePage(layout, pageIndex) { page ->
            val dragged = page.items.firstOrNull { it.id == draggedId } as? GridItem.AppShortcut ?: return@mutatePage page
            val target = page.items.firstOrNull { it.id == targetId } ?: return@mutatePage page
            when (target) {
                is GridItem.Folder -> page.copy(
                    items = page.items.mapNotNull { item ->
                        when {
                            item.id == draggedId -> null
                            item.id == targetId -> target.copy(items = target.items + dragged.copy(column = 0, row = 0))
                            else -> item
                        }
                    },
                )
                is GridItem.AppShortcut -> {
                    val folder = GridItem.Folder(
                        id = UUID.randomUUID().toString(),
                        column = target.column,
                        row = target.row,
                        name = "Cartella",
                        items = listOf(target.copy(column = 0, row = 0), dragged.copy(column = 1, row = 0)),
                    )
                    page.copy(items = page.items.mapNotNull { item ->
                        when (item.id) {
                            draggedId -> null
                            targetId -> folder
                            else -> item
                        }
                    })
                }
                is GridItem.Widget -> page
            }
        }
    }

    fun removeFromFolder(pageIndex: Int, folderId: String, appItemId: String) = updateLayout { layout ->
        mutatePage(layout, pageIndex) { page ->
            page.copy(items = page.items.map { item ->
                if (item is GridItem.Folder && item.id == folderId) {
                    item.copy(items = item.items.filterNot { it.id == appItemId })
                } else item
            })
        }
    }

    fun setIconOverride(pageIndex: Int, itemId: String, override: AppIconOverride) = updateLayout { layout ->
        mutatePage(layout, pageIndex) { page ->
            page.copy(items = page.items.map { item ->
                if (item is GridItem.AppShortcut && item.id == itemId) item.copy(iconOverride = override) else item
            })
        }
    }

    fun addWidget(pageIndex: Int, kind: WidgetKind, column: Int, row: Int, spanColumns: Int, spanRows: Int, appWidgetId: Int = -1) =
        updateLayout { layout ->
            mutatePage(layout, pageIndex) { page ->
                page.copy(
                    items = page.items + GridItem.Widget(
                        id = UUID.randomUUID().toString(),
                        column = column,
                        row = row,
                        spanColumns = spanColumns,
                        spanRows = spanRows,
                        kind = kind,
                        appWidgetId = appWidgetId,
                    ),
                )
            }
        }

    fun setPageCount(count: Int) = viewModelScope.launch { app.layoutRepository.setPageCount(count) }

    fun addPage() = updateLayout { it.copy(pages = it.pages + HomePage()) }

    fun removePage(index: Int) = updateLayout { layout ->
        if (layout.pages.size <= 1) layout else layout.copy(pages = layout.pages.filterIndexed { i, _ -> i != index })
    }

    // --- dock mutation ---
    fun setDockSlot(slot: Int, appInfo: AppInfo) = updateLayout { layout ->
        val newDock = layout.dock.filterNot { it.slot == slot } + DockItem(slot, appInfo.key)
        layout.copy(dock = newDock.sortedBy { it.slot })
    }

    fun clearDockSlot(slot: Int) = updateLayout { layout ->
        layout.copy(dock = layout.dock.filterNot { it.slot == slot })
    }

    private fun mutatePage(layout: HomeLayout, index: Int, transform: (HomePage) -> HomePage): HomeLayout {
        if (index !in layout.pages.indices) return layout
        val pages = layout.pages.toMutableList()
        pages[index] = transform(pages[index])
        return layout.copy(pages = pages)
    }

    private fun withPosition(item: GridItem, column: Int, row: Int): GridItem = when (item) {
        is GridItem.AppShortcut -> item.copy(column = column, row = row)
        is GridItem.Folder -> item.copy(column = column, row = row)
        is GridItem.Widget -> item.copy(column = column, row = row)
    }
}
