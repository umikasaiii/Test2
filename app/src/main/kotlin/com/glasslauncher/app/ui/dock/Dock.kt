package com.glasslauncher.app.ui.dock

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.glasslauncher.app.data.model.AppInfo
import com.glasslauncher.app.data.model.DockItem
import com.glasslauncher.app.data.model.DockSettings
import com.glasslauncher.app.glass.GlassSurface
import com.glasslauncher.app.glass.IconTile

/** The bottom dock: a wide glass pill holding 3-7 pinned apps. Disabled entirely via [DockSettings.enabled]. */
@Composable
fun Dock(
    settings: DockSettings,
    items: List<DockItem>,
    apps: List<AppInfo>,
    editMode: Boolean,
    modifier: Modifier = Modifier,
    onLaunch: (AppInfo) -> Unit,
    onLongPressSlot: (Int) -> Unit,
) {
    if (!settings.enabled) return

    GlassSurface(
        modifier = modifier
            .fillMaxWidth(settings.widthFraction)
            .height(settings.heightDp.dp),
        cornerRadiusDp = settings.heightDp / 2f,
        transparencyOverride = settings.opacity,
        blurRadiusDpOverride = settings.blurRadiusDp,
    ) {
        Row(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(settings.iconSpacingDp.dp, Alignment.CenterHorizontally),
        ) {
            for (slot in 0 until settings.slotCount) {
                val dockItem = items.firstOrNull { it.slot == slot }
                val info = dockItem?.let { d -> apps.firstOrNull { it.key == d.app } }
                androidx.compose.foundation.layout.Box(
                    Modifier
                        .fillMaxHeight()
                        .pointerInput(slot, editMode) {
                            detectTapGestures(
                                onTap = { if (!editMode) info?.let(onLaunch) },
                                onLongPress = { onLongPressSlot(slot) },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    if (info != null) {
                        IconTile(
                            icon = info.icon,
                            packageName = info.packageName,
                            override = dockItem.iconOverride,
                            tileSizeDpOverride = settings.iconSizeDp,
                        )
                    }
                }
            }
        }
    }
}
