package com.glasslauncher.app.ui.folder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.glasslauncher.app.data.model.AppInfo
import com.glasslauncher.app.data.model.FolderSettings
import com.glasslauncher.app.data.model.GridItem
import com.glasslauncher.app.glass.GlassSurface
import com.glasslauncher.app.glass.IconTile

/** The closed folder tile shown on the Home grid: a glass square with a mini 3x3 (or NxN) preview. */
@Composable
fun FolderClosedTile(
    folder: GridItem.Folder,
    apps: List<AppInfo>,
    sizeDp: Float,
    previewGrid: Int = 3,
) {
    GlassSurface(
        modifier = Modifier.size(sizeDp.dp),
        cornerRadiusDp = sizeDp * 0.34f,
        showGlow = false,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(previewGrid),
            modifier = Modifier.fillMaxSize().padding(sizeDp.dp * 0.12f),
            userScrollEnabled = false,
        ) {
            items(folder.items.take(previewGrid * previewGrid)) { shortcut ->
                val info = apps.firstOrNull { it.key == shortcut.app }
                Box(Modifier.aspectRatio(1f).padding(1.dp), contentAlignment = Alignment.Center) {
                    if (info != null) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(4.dp)),
                        )
                    }
                }
            }
        }
    }
}

/**
 * The full-screen open-folder overlay: a large glass panel with an editable title, a scrollable
 * grid of the folder's apps, and a close affordance (tap the scrim).
 */
@Composable
fun OpenFolderOverlay(
    folder: GridItem.Folder?,
    apps: List<AppInfo>,
    settings: FolderSettings,
    onLaunch: (AppInfo) -> Unit,
    onRename: (String) -> Unit,
    onRemoveApp: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = folder != null,
        enter = fadeIn(tween(150)) + scaleIn(
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
            initialScale = 0.88f,
        ),
        exit = fadeOut(tween(150)) + scaleOut(
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh),
            targetScale = 0.92f,
        ),
    ) {
        if (folder == null) return@AnimatedVisibility
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .pointerInput(folder.id) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.Center,
        ) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(420.dp)
                    .pointerInput(Unit) { detectTapGestures(onTap = {}) },
                cornerRadiusDp = settings.cornerRadiusDp,
                transparencyOverride = settings.transparency,
                blurRadiusDpOverride = settings.blurRadiusDp,
            ) {
                Column(Modifier.fillMaxSize().padding(settings.paddingDp.dp)) {
                    var name by remember(folder.id) { mutableStateOf(folder.name) }
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it; onRename(it) },
                        textStyle = TextStyle(color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(folder.items, key = { it.id }) { shortcut ->
                            val info = apps.firstOrNull { it.key == shortcut.app }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.pointerInput(shortcut.id) {
                                    detectTapGestures(
                                        onTap = { info?.let(onLaunch) },
                                        onLongPress = { onRemoveApp(shortcut.id) },
                                    )
                                },
                            ) {
                                if (info != null) {
                                    IconTile(icon = info.icon, packageName = info.packageName, override = shortcut.iconOverride, tileSizeDpOverride = 52f)
                                    Text(
                                        text = info.label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
