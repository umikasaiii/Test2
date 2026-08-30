package com.glasslauncher.app.ui.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Shared chrome for the Notification Panel and Control Center: a full-screen scrim (tap to
 * dismiss) with the panel itself sliding down from the top edge with a spring, anchored to
 * either side depending on which half of the top strip the user swiped from.
 */
@Composable
fun TopPanelOverlay(
    visible: Boolean,
    horizontalAlignment: Alignment.Horizontal,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.32f))
                .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) },
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
                    initialOffsetY = { -it },
                ) + fadeIn(),
                exit = slideOutVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
                    targetOffsetY = { -it },
                ) + fadeOut(),
                modifier = Modifier
                    .align(if (horizontalAlignment == Alignment.Start) Alignment.TopStart else Alignment.TopEnd)
                    .statusBarsPadding(),
            ) {
                Box(
                    Modifier.pointerInput(Unit) { detectTapGestures(onTap = {}) },
                ) {
                    content()
                }
            }
        }
    }
}
