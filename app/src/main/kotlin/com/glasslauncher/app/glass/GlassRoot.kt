package com.glasslauncher.app.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ContentScale
import androidx.compose.ui.graphics.ImageBitmap
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Draws the live wallpaper as the root layer of the Compose tree and marks it as a
 * [dev.chrisbanes.haze.hazeSource]. Every [GlassSurface] elsewhere in the tree then blurs
 * *this* image behind it — the real system wallpaper surface (drawn by the OS beneath our
 * translucent window) cannot be captured directly by any app, so we keep our own live copy
 * in sync via [com.glasslauncher.app.data.wallpaper.WallpaperRepository].
 */
@Composable
fun GlassRoot(
    wallpaperBitmap: ImageBitmap?,
    hazeState: HazeState = remember { HazeState() },
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize().hazeSource(state = hazeState)) {
        if (wallpaperBitmap != null) {
            Image(
                bitmap = wallpaperBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(Modifier.fillMaxSize())
        }
    }
    // Foreground UI is drawn in a sibling layer so it is not itself included as haze *source*
    // content (it is instead the *consumer*, via GlassSurface's hazeEffect).
    Box(Modifier.fillMaxSize()) {
        content()
    }
}
