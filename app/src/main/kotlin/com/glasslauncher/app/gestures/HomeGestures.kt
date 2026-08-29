package com.glasslauncher.app.gestures

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.glasslauncher.app.data.model.GestureSettings
import kotlin.math.abs

/**
 * Wires the Home background's swipe up (drawer), swipe down (notifications), pinch
 * (edit mode) and double tap (lock screen) gestures, each individually toggleable via
 * [GestureSettings]. Stacked as independent `pointerInput` blocks: Compose's built-in
 * gesture detectors already yield to each other correctly (a tap detector cancels once
 * the touch-slop is exceeded, handing off to the drag/transform detector).
 */
fun Modifier.homeGestures(
    settings: GestureSettings,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onPinch: () -> Unit,
    onDoubleTap: () -> Unit,
): Modifier = this
    .pointerInput(settings.doubleTapLocksScreen) {
        if (settings.doubleTapLocksScreen) {
            detectTapGestures(onDoubleTap = { onDoubleTap() })
        }
    }
    .pointerInput(settings.swipeUpOpensDrawer, settings.swipeDownOpensNotifications) {
        if (settings.swipeUpOpensDrawer || settings.swipeDownOpensNotifications) {
            var total = 0f
            detectVerticalDragGestures(
                onDragStart = { total = 0f },
                onDragEnd = {
                    if (total < -80f && settings.swipeUpOpensDrawer) onSwipeUp()
                    if (total > 80f && settings.swipeDownOpensNotifications) onSwipeDown()
                },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    total += dragAmount
                },
            )
        }
    }
    .pointerInput(settings.pinchOpensHomeSettings) {
        if (settings.pinchOpensHomeSettings) {
            detectTransformGestures { _, _, zoom, _ ->
                if (abs(zoom - 1f) > 0.08f) onPinch()
            }
        }
    }
