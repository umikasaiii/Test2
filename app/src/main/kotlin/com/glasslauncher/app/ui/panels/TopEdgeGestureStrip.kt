package com.glasslauncher.app.ui.panels

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Invisible top-edge strip mimicking the real status bar's swipe-down area, split in half:
 * swiping down from the left half opens the Notification Panel, from the right half the
 * Control Center — the same split-shade convention as iOS. Sits above every screen (Home,
 * Drawer, Settings) so it works everywhere, like the real notification shade would.
 *
 * Deliberately sized to exactly the system status bar inset ([WindowInsets.statusBars]) rather
 * than an arbitrary fixed height: every other composable in the app already starts *below* that
 * inset (via `Modifier.statusBarsPadding()`), so this strip can never overlap a real tap target
 * underneath it — a fixed height picked independently of the inset previously ate into the top
 * of the search bar's touch targets on some devices.
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
    Box(Modifier.fillMaxWidth().windowInsetsTopHeight(WindowInsets.statusBars)) {
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
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
            .fillMaxHeight()
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
