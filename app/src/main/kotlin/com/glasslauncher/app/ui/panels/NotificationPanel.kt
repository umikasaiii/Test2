package com.glasslauncher.app.ui.panels

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glasslauncher.app.data.model.NotificationPanelSettings
import com.glasslauncher.app.glass.GlassSurface
import com.glasslauncher.app.notifications.NotificationEntry
import com.glasslauncher.app.ui.settings.SettingsButtonRow

@Composable
fun NotificationPanel(
    settings: NotificationPanelSettings,
    entries: List<NotificationEntry>,
    accessGranted: Boolean,
    onRequestAccess: () -> Unit,
    onOpen: (NotificationEntry) -> Unit,
    onDismiss: (NotificationEntry) -> Unit,
    onClearAll: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier.fillMaxWidth(settings.widthFraction).padding(top = 8.dp),
        cornerRadiusDp = settings.cornerRadiusDp,
        transparencyOverride = settings.transparency,
        blurRadiusDpOverride = settings.blurRadiusDp,
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Notifiche", color = Color.White, fontSize = 16.sp)
                if (entries.isNotEmpty()) {
                    Text(
                        "Cancella tutte",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        modifier = Modifier.clickable { onClearAll() },
                    )
                }
            }
            when {
                !accessGranted -> {
                    Text(
                        "Consenti l'accesso alle notifiche per vederle qui.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    )
                    SettingsButtonRow("Consenti accesso") { onRequestAccess() }
                }
                entries.isEmpty() -> {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(Icons.Filled.NotificationsOff, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                        Text("Nessuna notifica", color = Color.White.copy(alpha = 0.55f), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
                else -> {
                    LazyColumn(Modifier.heightIn(max = 420.dp)) {
                        items(entries, key = { it.key }) { entry ->
                            NotificationRow(entry, settings, onOpen, onDismiss)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    entry: NotificationEntry,
    settings: NotificationPanelSettings,
    onOpen: (NotificationEntry) -> Unit,
    onDismiss: (NotificationEntry) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(settings.itemCornerRadiusDp.dp))
            .clickable { onOpen(entry) }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (settings.showAppIcon && entry.icon != null) {
            val bitmap = remember(entry.icon) { entry.icon.toBitmapOrNull(72) }
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(36.dp))
            }
        }
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(entry.title, color = Color.White, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (entry.text.isNotBlank()) {
                Text(
                    entry.text, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
            }
        }
        IconButton(onClick = { onDismiss(entry) }) {
            Icon(Icons.Filled.Close, contentDescription = "Rimuovi", tint = Color.White.copy(alpha = 0.5f))
        }
    }
}

private fun Drawable.toBitmapOrNull(sizePx: Int) = runCatching {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, sizePx, sizePx)
    draw(canvas)
    bmp.asImageBitmap()
}.getOrNull()
