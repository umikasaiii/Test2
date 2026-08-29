package com.glasslauncher.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.glasslauncher.app.glass.GlassSurface

data class ContextMenuAction(val label: String, val destructive: Boolean = false, val onClick: () -> Unit)

/** A small glass action sheet used for long-press context menus (Home icon, dock slot, drawer app). */
@Composable
fun ContextMenuOverlay(
    title: String?,
    actions: List<ContextMenuAction>,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 32.dp)
                        .pointerInput(Unit) { detectTapGestures(onTap = {}) },
                    cornerRadiusDp = 28f,
                ) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                        if (title != null) {
                            Text(
                                text = title,
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                        actions.forEach { action ->
                            Text(
                                text = action.label,
                                color = if (action.destructive) Color(0xFFFF8A80) else Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { action.onClick(); onDismiss() }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
