package com.glasslauncher.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.glasslauncher.app.glass.GlassSurface

/** The bottom action bar shown while Home is in Edit Mode: Wallpaper / Widget / Icone / Layout / Tema / Effetti / Impostazioni / Fatto. */
@Composable
fun EditModeToolbar(
    modifier: Modifier = Modifier,
    onWallpaper: () -> Unit,
    onWidgets: () -> Unit,
    onIcons: () -> Unit,
    onLayout: () -> Unit,
    onTheme: () -> Unit,
    onEffects: () -> Unit,
    onSettings: () -> Unit,
    onDone: () -> Unit,
) {
    GlassSurface(modifier = modifier.fillMaxWidth(0.96f), cornerRadiusDp = 30f) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            EditModeAction("Wallpaper", onWallpaper)
            EditModeAction("Widget", onWidgets)
            EditModeAction("Icone", onIcons)
            EditModeAction("Layout", onLayout)
            EditModeAction("Tema", onTheme)
            EditModeAction("Effetti", onEffects)
            EditModeAction("Impostazioni", onSettings)
            EditModeAction("Fatto", onDone)
        }
    }
}

@Composable
private fun EditModeAction(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color.White,
        modifier = Modifier.padding(vertical = 4.dp).clickable(onClick = onClick),
    )
}
