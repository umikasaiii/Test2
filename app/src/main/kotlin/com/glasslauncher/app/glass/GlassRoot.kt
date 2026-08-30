package com.glasslauncher.app.glass

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ContentScale
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.DpSize

/**
 * Draws the live wallpaper as the root layer of the Compose tree. Every [GlassSurface]
 * elsewhere in the tree draws its own translated, blurred crop of this same bitmap to fake a
 * "blur of what's behind it" — the real system wallpaper surface (drawn by the OS beneath our
 * translucent window) cannot be captured directly by any app, so we keep our own live copy in
 * sync via [com.glasslauncher.app.data.wallpaper.WallpaperRepository] instead.
 */
@Composable
fun GlassRoot(
    wallpaperBitmap: ImageBitmap?,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screenSize = DpSize(maxWidth, maxHeight)
        CompositionLocalProvider(
            LocalWallpaperBitmap provides wallpaperBitmap,
            LocalScreenSizeDp provides screenSize,
        ) {
            Box(Modifier.fillMaxSize()) {
                if (wallpaperBitmap != null) {
                    Image(
                        bitmap = wallpaperBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
            // Foreground UI is a sibling layer: each GlassSurface within it draws its own
            // positioned crop of the wallpaper above, rather than this layer being captured.
            Box(Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
