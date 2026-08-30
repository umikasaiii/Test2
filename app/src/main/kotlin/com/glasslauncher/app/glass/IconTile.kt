package com.glasslauncher.app.glass

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.glasslauncher.app.data.model.AppIconOverride
import com.glasslauncher.app.data.model.IconEngineSettings
import com.glasslauncher.app.data.model.IconRenderMode
import com.glasslauncher.app.data.model.IconShape
import com.glasslauncher.app.data.wallpaper.AdaptiveGlassPalette

val LocalIconEngineSettings: ProvidableCompositionLocal<IconEngineSettings> =
    compositionLocalOf { IconEngineSettings() }

@Composable
fun ProvideIconEngine(settings: IconEngineSettings, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalIconEngineSettings provides settings, content = content)
}

fun IconShape.toComposeShape(cornerRadiusDp: Float): Shape = when (this) {
    IconShape.CIRCLE -> CircleShape
    IconShape.SQUIRCLE -> RoundedCornerShape((cornerRadiusDp * 1.4f).dp)
    IconShape.ROUNDED_SQUARE -> RoundedCornerShape(cornerRadiusDp.dp)
    IconShape.TEARDROP -> RoundedCornerShape(
        topStart = (cornerRadiusDp * 1.6f).dp,
        topEnd = (cornerRadiusDp * 1.6f).dp,
        bottomEnd = 2.dp,
        bottomStart = (cornerRadiusDp * 1.6f).dp,
    )
}

private fun Drawable.toImageBitmap(sizePx: Int): ImageBitmap {
    val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, sizePx, sizePx)
    draw(canvas)
    return bmp.asImageBitmap()
}

/**
 * Renders one app icon according to its [AppIconOverride.mode]:
 *  - ORIGINAL: the app's real (already OS-masked) launcher icon, undecorated.
 *  - GLASS_AUTO: the original icon's artwork resized and centered inside a small glass chip.
 *  - THEME_CUSTOM: a bundled neutral glyph for common apps, inside the same glass chip.
 *  - MANUAL_IMAGE: a user-picked image, inside the same glass chip.
 *
 * Icon tiles intentionally use a cheap gradient-based "glass" look rather than a real
 * blur-behind effect: a Home page can have 30+ of these, and real per-tile background blur
 * would be the single biggest performance cost in the whole app.
 * Real blur is reserved for the dock, folders, widgets and the search bar.
 */
@Composable
fun IconTile(
    icon: Drawable,
    packageName: String,
    override: AppIconOverride,
    modifier: Modifier = Modifier,
    tileSizeDpOverride: Float? = null,
) {
    val settings = LocalIconEngineSettings.current
    val palette = LocalAdaptiveGlassPalette.current
    val tileSize = (tileSizeDpOverride ?: settings.tileSizeDp).dp
    val shape = settings.shape.toComposeShape(settings.cornerRadiusDp)

    val effectiveMode = when (override.mode) {
        IconRenderMode.THEME_CUSTOM -> if (ThemeIconRegistry.byId(override.themeIconId) != null ||
            ThemeIconRegistry.suggestFor(packageName) != null
        ) IconRenderMode.THEME_CUSTOM else IconRenderMode.GLASS_AUTO
        IconRenderMode.MANUAL_IMAGE -> if (override.manualImageUri != null) IconRenderMode.MANUAL_IMAGE else IconRenderMode.GLASS_AUTO
        else -> override.mode
    }

    when (effectiveMode) {
        IconRenderMode.ORIGINAL -> {
            val bitmap = remember(icon, tileSize) { icon.toImageBitmap((tileSize.value * 3).toInt().coerceAtLeast(1)) }
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = modifier.size(tileSize),
                contentScale = ContentScale.Fit,
            )
        }
        IconRenderMode.GLASS_AUTO -> {
            val bitmap = remember(icon) { icon.toImageBitmap(192) }
            GlassChip(shape, settings, palette, modifier.size(tileSize)) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((tileSize.value * (1f - settings.symbolScale) / 2f * settings.scale).coerceAtLeast(0f).dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        IconRenderMode.THEME_CUSTOM -> {
            val themeIcon = ThemeIconRegistry.byId(override.themeIconId) ?: ThemeIconRegistry.suggestFor(packageName)
            GlassChip(shape, settings, palette, modifier.size(tileSize)) {
                if (themeIcon != null) {
                    Image(
                        painter = painterResource(themeIcon.drawableRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding((tileSize.value * (1f - settings.symbolScale) / 2f).coerceAtLeast(0f).dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
        IconRenderMode.MANUAL_IMAGE -> {
            GlassChip(shape, settings, palette, modifier.size(tileSize)) {
                AsyncImage(
                    model = override.manualImageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
private fun GlassChip(
    shape: Shape,
    settings: IconEngineSettings,
    palette: AdaptiveGlassPalette,
    modifier: Modifier,
    symbol: @Composable () -> Unit,
) {
    val tint = lerp(Color(0xFF262A38), palette.primary, 0.28f)
    Box(
        modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        tint.copy(alpha = (settings.backgroundTint + 0.15f).coerceIn(0.1f, 0.95f)),
                        tint.copy(alpha = (settings.backgroundTint - 0.1f).coerceIn(0.05f, 0.9f)),
                    ),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = (0.55f * settings.borderIntensity).coerceIn(0f, 1f)),
                        Color.White.copy(alpha = 0.05f),
                    ),
                ),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Soft glow: a translucent radial highlight rather than a real blur, cheap at scale.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            palette.tertiary.copy(alpha = 0.16f * settings.glow),
                            Color.Transparent,
                        ),
                    ),
                    shape,
                ),
        )
        symbol()
    }
}
