package com.glasslauncher.app.ui.home

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glasslauncher.app.data.model.AppInfo
import com.glasslauncher.app.data.model.ClockWidgetSettings
import com.glasslauncher.app.data.model.GridItem
import com.glasslauncher.app.data.model.HomeGridSettings
import com.glasslauncher.app.data.model.HomePage
import com.glasslauncher.app.data.model.WeatherWidgetSettings
import com.glasslauncher.app.data.model.WidgetKind
import com.glasslauncher.app.data.weather.WeatherRepository
import com.glasslauncher.app.glass.IconTile
import com.glasslauncher.app.ui.folder.FolderClosedTile
import com.glasslauncher.app.ui.widgets.ClockWidget
import com.glasslauncher.app.ui.widgets.ThirdPartyWidgetView
import com.glasslauncher.app.ui.widgets.WeatherWidget
import com.glasslauncher.app.widgethost.LauncherWidgetHost
import kotlin.math.roundToInt

/**
 * One Home page. Every [GridItem] is placed at an absolute pixel offset derived from its
 * (column, row); dragging an item (only while [editMode] is active) reparents it onto the
 * nearest free cell, or merges it into a folder / creates one when dropped on another icon.
 */
@Composable
fun HomeGrid(
    page: HomePage,
    grid: HomeGridSettings,
    apps: List<AppInfo>,
    editMode: Boolean,
    clockSettings: ClockWidgetSettings,
    weatherSettings: WeatherWidgetSettings,
    weatherRepository: WeatherRepository,
    widgetHost: LauncherWidgetHost,
    modifier: Modifier = Modifier,
    onLaunch: (AppInfo) -> Unit,
    onOpenFolder: (String) -> Unit,
    onLongPressEmptySpace: () -> Unit,
    onMove: (itemId: String, column: Int, row: Int) -> Unit,
    onCombine: (draggedId: String, targetId: String) -> Unit,
    onLongPressItem: (GridItem) -> Unit,
) {
    BoxWithConstraints(
        modifier
            .padding(horizontal = grid.marginHorizontalDp.dp)
            .pointerInput(editMode) {
                detectTapGestures(onLongPress = { if (!editMode) onLongPressEmptySpace() })
            },
    ) {
        val cellWidth = maxWidth / grid.columns
        val cellHeight = maxHeight / grid.rows
        val density = LocalDensity.current
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val cellHeightPx = with(density) { cellHeight.toPx() }

        page.items.forEach { item ->
            var dragOffset by remember(item.id) { mutableStateOf(Offset.Zero) }
            var dragging by remember(item.id) { mutableStateOf(false) }
            val spanCols = (item as? GridItem.Widget)?.spanColumns ?: 1
            val spanRows = (item as? GridItem.Widget)?.spanRows ?: 1

            Box(
                Modifier
                    .width(cellWidth * spanCols)
                    .height(cellHeight * spanRows)
                    .offset {
                        val baseX = (item.column * cellWidthPx).roundToInt()
                        val baseY = (item.row * cellHeightPx).roundToInt()
                        IntOffset(
                            baseX + dragOffset.x.roundToInt(),
                            baseY + dragOffset.y.roundToInt(),
                        )
                    }
                    .then(
                        if (editMode) {
                            Modifier.pointerInput(item.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { dragging = true },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOffset += amount
                                    },
                                    onDragEnd = {
                                        dragging = false
                                        val newCol = ((item.column * cellWidthPx + dragOffset.x) / cellWidthPx)
                                            .roundToInt().coerceIn(0, grid.columns - 1)
                                        val newRow = ((item.row * cellHeightPx + dragOffset.y) / cellHeightPx)
                                            .roundToInt().coerceIn(0, grid.rows - 1)
                                        dragOffset = Offset.Zero
                                        val overlapping = page.items.firstOrNull {
                                            it.id != item.id && it.column == newCol && it.row == newRow
                                        }
                                        if (overlapping != null) {
                                            onCombine(item.id, overlapping.id)
                                        } else {
                                            onMove(item.id, newCol, newRow)
                                        }
                                    },
                                    onDragCancel = { dragging = false; dragOffset = Offset.Zero },
                                )
                            }
                        } else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                GridItemContent(
                    item = item,
                    apps = apps,
                    grid = grid,
                    clockSettings = clockSettings,
                    weatherSettings = weatherSettings,
                    weatherRepository = weatherRepository,
                    widgetHost = widgetHost,
                    onLaunch = onLaunch,
                    onOpenFolder = onOpenFolder,
                    onLongPress = { onLongPressItem(item) },
                )
            }
        }
    }
}

@Composable
private fun GridItemContent(
    item: GridItem,
    apps: List<AppInfo>,
    grid: HomeGridSettings,
    clockSettings: ClockWidgetSettings,
    weatherSettings: WeatherWidgetSettings,
    weatherRepository: WeatherRepository,
    widgetHost: LauncherWidgetHost,
    onLaunch: (AppInfo) -> Unit,
    onOpenFolder: (String) -> Unit,
    onLongPress: () -> Unit,
) {
    when (item) {
        is GridItem.AppShortcut -> {
            val info = apps.firstOrNull { it.key == item.app }
            Box(
                Modifier.pointerInput(item.id) {
                    detectTapGestures(
                        onTap = { info?.let(onLaunch) },
                        onLongPress = { onLongPress() },
                    )
                },
                contentAlignment = Alignment.Center,
            ) {
                Box(contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.padding(top = 2.dp)) {
                        if (info != null) {
                            IconTile(
                                icon = info.icon,
                                packageName = info.packageName,
                                override = item.iconOverride,
                                tileSizeDpOverride = grid.iconSizeDp,
                            )
                        }
                    }
                }
                if (grid.showLabels) {
                    Box(Modifier.padding(top = grid.iconSizeDp.dp + 6.dp)) {
                        BasicText(
                            text = item.customLabel ?: info?.label ?: "",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = grid.labelSizeSp.sp,
                                textAlign = TextAlign.Center,
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        is GridItem.Folder -> {
            Box(
                Modifier.pointerInput(item.id) {
                    detectTapGestures(
                        onTap = { onOpenFolder(item.id) },
                        onLongPress = { onLongPress() },
                    )
                },
            ) {
                FolderClosedTile(folder = item, apps = apps, sizeDp = grid.iconSizeDp * 1.15f)
            }
        }
        is GridItem.Widget -> {
            Box(
                Modifier.fillMaxSize().pointerInput(item.id) {
                    detectTapGestures(onLongPress = { onLongPress() })
                },
                contentAlignment = Alignment.Center,
            ) {
                when (item.kind) {
                    WidgetKind.CLOCK -> ClockWidget(clockSettings)
                    WidgetKind.WEATHER -> WeatherWidget(weatherSettings, weatherRepository)
                    WidgetKind.THIRD_PARTY -> ThirdPartyWidgetView(widgetHost, item.appWidgetId, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
