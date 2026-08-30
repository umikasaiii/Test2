package com.glasslauncher.app.ui.panels

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Invisible top-edge strip mimicking the real status bar's swipe-down area, split in half:
 * swiping down from the left half opens the Notification Panel, from the right half the
 * Control Center — the same split-shade convention as iOS. Sits above every screen (Home,
 * Drawer, Settings) so it works everywhere, like the real notification shade would.
 */
@Composable
fun TopEdgeGestureStrip(
    enabled: Boolean,
    notificationsEnabled: Boolean,
    controlCenterEnabled: Boolean,
    onOpenNotifications: () -> Unit,
    onOpenControlCenter: () -> Unit,
) {
    if (!enabled) return
    Box(Modifier.fillMaxWidth().statusBarsPadding().height(28.dp)) {
        Row(Modifier.fillMaxWidth()) {
            EdgeHalf(Modifier.weight(1f), notificationsEnabled, onOpenNotifications)
            EdgeHalf(Modifier.weight(1f), controlCenterEnabled, onOpenControlCenter)
        }
    }
}

@Composable
private fun EdgeHalf(modifier: Modifier, enabled: Boolean, onTriggered: () -> Unit) {
    if (!enabled) return
    var dragged by remember { mutableFloatStateOf(0f) }
    Box(
        modifier
            .height(28.dp)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragged = 0f },
                    onDragEnd = { if (dragged > 24f) onTriggered() },
                    onDragCancel = { dragged = 0f },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        dragged += amount
                    },
                )
            },
    )
}
